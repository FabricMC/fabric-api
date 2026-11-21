/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.impl.base.event;

import java.io.IOException;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.classfile.MethodModel;
import java.lang.classfile.TypeKind;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.event.EventFactory.ExceptionHandling;

/**
 * The invoker factories of {@link net.fabricmc.fabric.api.event.EventFactory#create}, for listener interfaces with a single method.
 * The {@link Mode} is the only definition of how the listener results are combined,
 * every kind of invoker below is generated from it.
 *
 * <p>There are two kinds of invokers, with identical behavior:
 * <ul>
 *     <li>The <b>loop</b> invoker is used right after registration. A class is generated once for each listener
 *     interface and combination, and shared by all events using them. It iterates over the array of listeners, which
 *     can be replaced: an event keeps its loop invoker while listeners are registered, see {@link StableInvoker}.</li>
 *     <li>The <b>unrolled</b> invoker replaces it in the background when there are 2 to {@link #MAX_UNROLLED} listeners.
 *     A class is generated for each event and listener count, that calls every listener from its own call site:
 *     {@code l0.run(args); l1.run(args); ...}. Every call site only ever sees one listener class,
 *     so the JIT can inline it instead of going through a megamorphic interface dispatch.</li>
 * </ul>
 *
 * <p>With a single listener, all combinations are equivalent to calling it directly, so it is used as the invoker,
 * unless its exceptions are handled.
 *
 * <p>Unless exceptions {@linkplain ExceptionHandling#PROPAGATE propagate}, every listener call is wrapped in its own
 * {@code try}/{@code catch (Exception)}, and a listener that throws is skipped as if it had returned the pass value.
 * The handler costs nothing while no listener throws, it is only an entry in the exception table of the method.
 *
 * <p>Generating an unrolled class is far too slow for registration, so {@link #apply} never returns one.
 * {@link ArrayBackedEvent} asks for it separately via {@link #generateUnrolled}, off-thread.
 */
public final class CombinedInvokerFactory<T> implements Function<T[], T> {
	/**
	 * How the results of the listeners are combined, see {@link net.fabricmc.fabric.api.event.EventFactory.Behavior}.
	 */
	public enum Mode {
		VOID,
		SHORT_CIRCUIT,
		ALL_MATCH,
		ANY_MATCH,
		CHAIN
	}

	/**
	 * The most listeners that get an unrolled invoker. Throughput of both kinds of invokers falls steeply past about 32 listeners anyway.
	 */
	static final int MAX_UNROLLED = 128;
	/**
	 * The JIT does not compile methods with more bytecode than this ({@code -XX:-DontCompileHugeMethods}).
	 * An unrolled invoker that would be larger, because of many listeners with many parameters or exception handlers, is not used.
	 */
	private static final int HUGE_METHOD_LIMIT = 8000;
	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-api-base");
	private static final @Nullable Path DUMP_DIR = System.getProperty("fabric.event.dumpInvokers") != null
			? Path.of(System.getProperty("fabric.event.dumpInvokers")) : null;
	private static final AtomicInteger DUMP_COUNTER = new AtomicInteger();
	/**
	 * Loop invoker classes by listener interface, then by combination. Weak on the listener class, through {@link ClassValue}.
	 */
	private static final ClassValue<Map<String, LoopClass>> LOOP_CLASSES = new ClassValue<>() {
		@Override
		protected Map<String, LoopClass> computeValue(Class<?> type) {
			return new ConcurrentHashMap<>();
		}
	};

	/**
	 * A loop invoker class: its constructor {@code (Object[] listeners, Object pass)},
	 * and the setter of its volatile listener array {@code (Object loop, Object[] listeners)}.
	 */
	private record LoopClass(MethodHandle constructor, MethodHandle setter) { }
	final Class<? super T> type;
	final Method method;
	private final Mode mode;
	private final @Nullable Object passValue;
	private final int threadedParam;
	private final ExceptionHandling exceptions;
	final MethodHandles.Lookup lookup;
	private boolean unrolledBroken;

	private CombinedInvokerFactory(Class<? super T> type, Method method, Mode mode, @Nullable Object passValue, int threadedParam, ExceptionHandling exceptions, MethodHandles.Lookup lookup) {
		this.type = type;
		this.method = method;
		this.mode = mode;
		this.passValue = passValue;
		this.threadedParam = threadedParam;
		this.exceptions = exceptions;
		this.lookup = lookup;
	}

	/**
	 * Create the invoker factory of an event.
	 *
	 * @throws IllegalArgumentException if the listener interface or the combination are invalid.
	 */
	public static <T> CombinedInvokerFactory<T> create(Class<? super T> type, Mode mode, @Nullable Object passValue, int threadedParam) {
		return create(type, mode, passValue, threadedParam, ExceptionHandling.PROPAGATE);
	}

	/**
	 * Create the invoker factory of an event.
	 *
	 * @param exceptions what happens when a listener throws an exception.
	 * @throws IllegalArgumentException if the listener interface or the combination are invalid.
	 */
	public static <T> CombinedInvokerFactory<T> create(Class<? super T> type, Mode mode, @Nullable Object passValue, int threadedParam, ExceptionHandling exceptions) {
		Objects.requireNonNull(type, "Tried to create an invoker factory for a null listener type!");
		Objects.requireNonNull(exceptions, "Tried to create an invoker factory with a null exception handling!");
		if (!type.isInterface()) throw new IllegalArgumentException(type.getName() + " is not an interface");

		Method found = null;

		for (Method m : type.getMethods()) {
			if (Modifier.isAbstract(m.getModifiers())) {
				if (found != null) throw new IllegalArgumentException(type.getName() + " has more than one abstract method, write the invoker factory by hand");
				found = m;
			}
		}

		if (found == null) throw new IllegalArgumentException(type.getName() + " has no abstract method");
		validate(type, found, mode, passValue, threadedParam);

		try {
			return new CombinedInvokerFactory<>(type, found, mode, passValue, threadedParam, exceptions, MethodHandles.privateLookupIn(type, MethodHandles.lookup()));
		} catch (IllegalAccessException | RuntimeException e) {
			throw new IllegalArgumentException(type.getName() + " is not accessible to Fabric API, for example because its module does not open its package", e);
		}
	}

	private static void validate(Class<?> type, Method method, Mode mode, @Nullable Object pass, int threadedParam) {
		Class<?> ret = method.getReturnType();
		Class<?>[] params = method.getParameterTypes();
		String where = type.getName() + "." + method.getName();

		switch (mode) {
			case VOID -> {
				if (ret != void.class) throw new IllegalArgumentException(where + " returns " + ret.getName() + ", but INVOKE_ALL needs void");
			}
			case ALL_MATCH -> {
				if (ret != boolean.class) throw new IllegalArgumentException(where + " returns " + ret.getName() + ", but ALL_TRUE needs boolean");
			}
			case ANY_MATCH -> {
				if (ret != boolean.class) throw new IllegalArgumentException(where + " returns " + ret.getName() + ", but ANY_TRUE needs boolean");
			}
			case CHAIN -> {
				int p = threadedParam;

				if (ret == void.class || p < 0 || p >= params.length || params[p] != ret) {
					throw new IllegalArgumentException(where + " needs a parameter " + p + " of its return type " + ret.getName() + " to chain the listeners");
				}
			}
			case SHORT_CIRCUIT -> {
				if (ret == boolean.class) {
					if (!(pass instanceof Boolean)) throw new IllegalArgumentException(where + " returns boolean, use STOP_ON_FALSE or STOP_ON_TRUE");
				} else if (ret.isPrimitive()) {
					throw new IllegalArgumentException(where + " returns " + ret.getName() + ", which is not supported by this invoker factory");
				} else if (pass != null && !ret.isInstance(pass)) {
					throw new IllegalArgumentException(where + " returns " + ret.getName() + ", but the pass value is a " + pass.getClass().getName());
				}
			}
		}
	}

	/**
	 * The invoker for the current listeners, available immediately: the single listener itself, or the loop invoker.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T apply(T[] listeners) {
		if (listeners.length == 1 && passesSingleListener()) return listeners[0];

		return newLoop(listeners);
	}

	/**
	 * Whether a single listener can be its own invoker. Not when its exceptions are handled.
	 */
	boolean passesSingleListener() {
		return exceptions == ExceptionHandling.PROPAGATE;
	}

	/**
	 * The fewest listeners that get an unrolled invoker.
	 */
	int minUnrolled() {
		return passesSingleListener() ? 2 : 1;
	}

	/**
	 * Called by the generated invokers of events that catch exceptions, when a listener threw one.
	 * Only called from generated bytecode (see {@code callListener}), so it looks unused to the IDE.
	 */
	@SuppressWarnings("unused")
	public static void listenerFailed(Throwable t, Object listener, String event) {
		LOGGER.error("A listener of {} threw an exception, skipping it: {}", event, listener, t);
	}

	/**
	 * A new loop invoker for the given listeners.
	 */
	@SuppressWarnings("unchecked")
	T newLoop(T[] listeners) {
		try {
			return (T) (Object) loopClass().constructor().invokeExact((Object[]) listeners, passValue);
		} catch (Throwable t) {
			throw new IllegalStateException("Could not create the invoker of an event for " + type.getName(), t);
		}
	}

	/**
	 * Replaces the listeners of a loop invoker. Its class does not change, so neither does the code that calls it.
	 */
	void setLoopListeners(T loop, T[] listeners) {
		try {
			loopClass().setter().invokeExact((Object) loop, (Object[]) listeners);
		} catch (Throwable t) {
			throw new IllegalStateException("Could not update the invoker of an event for " + type.getName(), t);
		}
	}

	/**
	 * The loop class of this listener interface and combination. Generated once: events created concurrently must get
	 * the same class, as a loop is later updated through the setter of the cached class.
	 */
	private LoopClass loopClass() {
		return LOOP_CLASSES.get(type).computeIfAbsent(loopKey(), key -> {
			try {
				return generateLoopClass();
			} catch (ReflectiveOperationException e) {
				throw new IllegalStateException("Could not generate the loop invoker class for " + type.getName(), e);
			}
		});
	}

	/**
	 * Generates the unrolled invoker. Slow, must not be called from registration.
	 * Not thread-safe: only called from the single upgrade thread.
	 *
	 * @return the invoker, or {@code null} if the one from {@link #apply} should be kept.
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	T generateUnrolled(T[] listeners) {
		int n = listeners.length;
		if (n < minUnrolled() || n > MAX_UNROLLED || unrolledBroken) return null;

		try {
			byte[] bytes = generateUnrolledBytes(n);

			if (listenerMethodSize(bytes) > HUGE_METHOD_LIMIT) {
				return null; // would never be compiled, the loop is faster
			}

			if (DUMP_DIR != null) {
				dump(bytes, "Unrolled" + n);
			}

			// The listeners (and the pass value) are the class data of the hidden class, and the generated code loads them
			// as dynamic constants. They are true constants for the JIT, which knows the exact class of every listener.
			List<Object> data = new ArrayList<>(Arrays.asList((Object[]) listeners));

			if (storesPass()) {
				data.add(passValue);
			}

			MethodHandles.Lookup hidden = lookup.defineHiddenClassWithClassData(bytes, data, true);
			return (T) hidden.findConstructor(hidden.lookupClass(), MethodType.methodType(void.class)).invoke();
		} catch (Throwable t) {
			LOGGER.warn("Could not generate an unrolled invoker for {}, keeping the loop", type.getName(), t);
			unrolledBroken = true;
			return null;
		}
	}

	/**
	 * The bytecode size of the listener method of a generated class.
	 */
	private int listenerMethodSize(byte[] bytes) {
		for (MethodModel m : ClassFile.of().parse(bytes).methods()) {
			if (m.methodName().equalsString(method.getName())) {
				return m.findAttribute(Attributes.code()).map(CodeAttribute::codeLength).orElse(0);
			}
		}

		return 0;
	}

	// ---- Class generation ----

	private boolean storesPass() {
		return mode == Mode.SHORT_CIRCUIT && passValue != null && method.getReturnType() != boolean.class;
	}

	/**
	 * Everything the bytecode of the loop invoker depends on. The pass value itself is stored in a field.
	 */
	private String loopKey() {
		String pass = !(mode == Mode.SHORT_CIRCUIT) ? "" : passValue == null ? "null" : passValue instanceof Boolean b ? b.toString() : "ref";
		return mode + "/" + threadedParam + "/" + pass + "/" + exceptions;
	}

	private ClassDesc listenerDesc() {
		return type.describeConstable().orElseThrow();
	}

	private MethodTypeDesc methodDesc() {
		return MethodType.methodType(method.getReturnType(), method.getParameterTypes()).describeConstable().orElseThrow();
	}

	private static final MethodTypeDesc CONSTRUCTOR_DESC = MethodTypeDesc.of(ConstantDescs.CD_void, ConstantDescs.CD_Object.arrayType(), ConstantDescs.CD_Object);
	private static final ClassDesc CD_EXCEPTION = ClassDesc.of("java.lang.Exception");
	private static final ClassDesc CD_THROWABLE = ClassDesc.of("java.lang.Throwable");
	private static final ClassDesc CD_SELF = ClassDesc.of(CombinedInvokerFactory.class.getName());
	private static final MethodTypeDesc LISTENER_FAILED_DESC = MethodTypeDesc.of(ConstantDescs.CD_void, CD_THROWABLE, ConstantDescs.CD_Object, ConstantDescs.CD_String);
	private static final MethodTypeDesc ADD_SUPPRESSED_DESC = MethodTypeDesc.of(ConstantDescs.CD_void, CD_THROWABLE);

	/**
	 * What the combination does without listeners, as a constant method handle for the call site of a stable invoker.
	 */
	MethodHandle emptyTarget() {
		MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
		Class<?> returnClass = methodType.returnType();

		return switch (mode) {
			case VOID -> MethodHandles.empty(methodType);
			case ALL_MATCH -> MethodHandles.dropArguments(MethodHandles.constant(boolean.class, true), 0, methodType.parameterList());
			case ANY_MATCH -> MethodHandles.dropArguments(MethodHandles.constant(boolean.class, false), 0, methodType.parameterList());
			case SHORT_CIRCUIT -> MethodHandles.dropArguments(MethodHandles.constant(returnClass, passValue), 0, methodType.parameterList());
			case CHAIN -> MethodHandles.permuteArguments(MethodHandles.identity(returnClass), methodType, threadedParam);
		};
	}

	private LoopClass generateLoopClass() throws ReflectiveOperationException {
		ClassDesc self = ClassDesc.of(type.getPackageName(), "FabricEvent$Loop");
		ClassDesc listener = listenerDesc();
		ClassDesc array = listener.arrayType();
		ClassDesc returnDesc = method.getReturnType().describeConstable().orElseThrow();
		boolean storePass = storesPass();
		Class<?> returnClass = method.getReturnType();

		byte[] bytes = ClassFile.of().build(self, cb -> {
			cb.withFlags(ClassFile.ACC_FINAL | ClassFile.ACC_SYNTHETIC);
			cb.withInterfaceSymbols(listener);
			// Volatile and not final: registering listeners replaces the array, without changing the invoker.
			cb.withField("l", array, ClassFile.ACC_PRIVATE | ClassFile.ACC_VOLATILE);

			if (storePass) {
				cb.withField("pass", returnDesc, ClassFile.ACC_PRIVATE | ClassFile.ACC_FINAL);
			}

			cb.withMethodBody(ConstantDescs.INIT_NAME, CONSTRUCTOR_DESC, ClassFile.ACC_PUBLIC, code -> {
				code.aload(0);
				code.invokespecial(ConstantDescs.CD_Object, ConstantDescs.INIT_NAME, ConstantDescs.MTD_void);
				code.aload(0);
				code.aload(1);
				code.checkcast(array);
				code.putfield(self, "l", array);

				if (storePass) {
					code.aload(0);
					code.aload(2);
					code.checkcast(returnDesc);
					code.putfield(self, "pass", returnDesc);
				}

				code.return_();
			});

			cb.withMethodBody(method.getName(), methodDesc(), ClassFile.ACC_PUBLIC, code -> {
				int arraySlot = code.allocateLocal(TypeKind.REFERENCE);
				int indexSlot = code.allocateLocal(TypeKind.INT);
				int accSlot = accumulatorSlot(code);
				int pendingSlot = pendingSlot(code);
				List<Consumer<CodeBuilder>> handlers = new ArrayList<>();
				Label top = code.newLabel();
				Label end = code.newLabel();
				Label early = code.newLabel();

				code.aload(0);
				code.getfield(self, "l", array);
				code.astore(arraySlot);
				code.loadConstant(0);
				code.istore(indexSlot);
				startAccumulator(code, accSlot);

				code.labelBinding(top);
				code.iload(indexSlot);
				code.aload(arraySlot);
				code.arraylength();
				code.if_icmpge(end);

				callListener(code, c -> {
					c.aload(arraySlot);
					c.iload(indexSlot);
					c.aaload();
				}, c -> combineResult(c, self, returnDesc, early, -1, accSlot), pendingSlot, handlers);

				code.iinc(indexSlot, 1);
				code.goto_(top);

				code.labelBinding(end);

				switch (mode) {
					case VOID -> {
						rethrowPending(code, pendingSlot);
						code.return_();
					}
					case ALL_MATCH, ANY_MATCH -> {
						if (accSlot >= 0) code.iload(accSlot);
						rethrowPending(code, pendingSlot);
						code.ireturn();
					}
					case CHAIN -> {
						code.loadLocal(TypeKind.from(returnClass), code.parameterSlot(threadedParam));
						rethrowPending(code, pendingSlot);
						code.return_(TypeKind.from(returnClass));
					}
					case SHORT_CIRCUIT -> {
						pushPass(code, self, returnDesc, -1);
						code.labelBinding(early);
						rethrowPending(code, pendingSlot);
						code.return_(TypeKind.from(returnClass));
					}
				}

				emitHandlers(code, handlers);
			});
		});

		if (DUMP_DIR != null) {
			dump(bytes, "Loop");
		}

		MethodHandles.Lookup hidden = lookup.defineHiddenClass(bytes, true);
		MethodHandle constructor = hidden.findConstructor(hidden.lookupClass(), MethodType.methodType(void.class, Object[].class, Object.class))
				.asType(MethodType.methodType(Object.class, Object[].class, Object.class));
		MethodHandle setter = hidden.findSetter(hidden.lookupClass(), "l", type.arrayType())
				.asType(MethodType.methodType(void.class, Object.class, Object[].class));
		return new LoopClass(constructor, setter);
	}

	/**
	 * Generates the class of an unrolled invoker for {@code n} listeners. It has no state:
	 * listener {@code i} is the class data entry {@code i}, the pass value is the entry {@code n}.
	 */
	private byte[] generateUnrolledBytes(int n) {
		ClassDesc self = ClassDesc.of(type.getPackageName(), "FabricEvent$Unrolled" + n);
		ClassDesc listener = listenerDesc();
		ClassDesc returnDesc = method.getReturnType().describeConstable().orElseThrow();
		Class<?> returnClass = method.getReturnType();

		return ClassFile.of().build(self, cb -> {
			cb.withFlags(ClassFile.ACC_FINAL | ClassFile.ACC_SYNTHETIC);
			cb.withInterfaceSymbols(listener);

			cb.withMethodBody(ConstantDescs.INIT_NAME, ConstantDescs.MTD_void, ClassFile.ACC_PUBLIC, code -> {
				code.aload(0);
				code.invokespecial(ConstantDescs.CD_Object, ConstantDescs.INIT_NAME, ConstantDescs.MTD_void);
				code.return_();
			});

			cb.withMethodBody(method.getName(), methodDesc(), ClassFile.ACC_PUBLIC, code -> {
				int accSlot = accumulatorSlot(code);
				int pendingSlot = pendingSlot(code);
				List<Consumer<CodeBuilder>> handlers = new ArrayList<>();
				Label done = code.newLabel();
				startAccumulator(code, accSlot);

				for (int i = 0; i < n; i++) {
					int index = i;
					callListener(code, c -> c.ldc(classDataConstant(listener, index)), c -> combineResult(c, self, returnDesc, done, n, accSlot), pendingSlot, handlers);
				}

				switch (mode) {
					case VOID -> {
						rethrowPending(code, pendingSlot);
						code.return_();
					}
					case ALL_MATCH, ANY_MATCH -> {
						if (accSlot >= 0) code.iload(accSlot);
						rethrowPending(code, pendingSlot);
						code.ireturn();
					}
					case CHAIN -> {
						TypeKind kind = TypeKind.from(returnClass);
						code.loadLocal(kind, code.parameterSlot(threadedParam));
						rethrowPending(code, pendingSlot);
						code.return_(kind);
					}
					case SHORT_CIRCUIT -> {
						// Every listener returned the pass value.
						pushPass(code, self, returnDesc, n);
						code.labelBinding(done);
						rethrowPending(code, pendingSlot);
						code.return_(TypeKind.from(returnClass));
					}
				}

				emitHandlers(code, handlers);
			});
		});
	}

	/**
	 * The local of the result so far of {@link Mode#ALL_MATCH} and {@link Mode#ANY_MATCH}, or -1 if it stays on the operand stack.
	 * A thrown exception clears the operand stack, so it needs a local when exceptions are caught.
	 */
	private int accumulatorSlot(CodeBuilder code) {
		return exceptions != ExceptionHandling.PROPAGATE && (mode == Mode.ALL_MATCH || mode == Mode.ANY_MATCH) ? code.allocateLocal(TypeKind.INT) : -1;
	}

	/**
	 * The local of the exception to rethrow, initialized to {@code null}, or -1 unless exceptions are {@link ExceptionHandling#ADD_SUPPRESSED}.
	 */
	private int pendingSlot(CodeBuilder code) {
		if (exceptions != ExceptionHandling.ADD_SUPPRESSED) return -1;

		int slot = code.allocateLocal(TypeKind.REFERENCE);
		code.aconst_null();
		code.astore(slot);
		return slot;
	}

	/**
	 * Before a return: throws the pending exception, if any. The result to return stays on the stack.
	 */
	private static void rethrowPending(CodeBuilder code, int pendingSlot) {
		if (pendingSlot < 0) return;

		Label none = code.newLabel();
		code.aload(pendingSlot);
		code.ifnull(none);
		code.aload(pendingSlot);
		code.athrow();
		code.labelBinding(none);
	}

	private void startAccumulator(CodeBuilder code, int accSlot) {
		if (mode == Mode.ALL_MATCH || mode == Mode.ANY_MATCH) {
			code.loadConstant(mode == Mode.ALL_MATCH ? 1 : 0);
			if (accSlot >= 0) code.istore(accSlot);
		}
	}

	/**
	 * Calls a listener: pushes it with {@code loadListener}, then all arguments, invokes it,
	 * and combines its result with {@code onResult}, which must leave the operand stack as it was before the call.
	 *
	 * <p>Unless exceptions propagate, the exception of a listener that throws is handled, and execution continues after {@code onResult}.
	 * The handler is added to {@code handlers}, to be emitted after the end of the method by {@link #emitHandlers}:
	 * the code of the listeners stays in one straight line, without jumps over the handlers.
	 */
	private void callListener(CodeBuilder code, Consumer<CodeBuilder> loadListener, Consumer<CodeBuilder> onResult, int pendingSlot, List<Consumer<CodeBuilder>> handlers) {
		if (exceptions == ExceptionHandling.PROPAGATE) {
			loadListener.accept(code);
			loadParams(code);
			code.invokeinterface(listenerDesc(), method.getName(), methodDesc());
			onResult.accept(code);
			return;
		}

		Label start = code.newLabel();
		Label end = code.newLabel();
		Label handler = code.newLabel();
		Label next = code.newLabel();

		code.labelBinding(start);
		loadListener.accept(code);
		loadParams(code);
		code.invokeinterface(listenerDesc(), method.getName(), methodDesc());
		code.labelBinding(end);
		onResult.accept(code);
		code.labelBinding(next);
		code.exceptionCatch(start, end, handler, CD_EXCEPTION);

		handlers.add(c -> {
			// The exception is on the stack.
			c.labelBinding(handler);

			switch (exceptions) {
				case PROPAGATE -> throw new AssertionError();
				case SUPPRESS -> c.pop();
				case SUPPRESS_AND_LOG -> {
					loadListener.accept(c);
					c.loadConstant(type.getName());
					c.invokestatic(CD_SELF, "listenerFailed", LISTENER_FAILED_DESC);
				}
				case ADD_SUPPRESSED -> {
					// if (pending == null) pending = e; else if (pending != e) pending.addSuppressed(e);
					// The same instance can be thrown twice, for example the preallocated exceptions of the JIT,
					// and suppressing an exception in itself is an IllegalArgumentException.
					Label notFirst = c.newLabel();
					Label same = c.newLabel();
					c.aload(pendingSlot);
					c.ifnonnull(notFirst);
					c.astore(pendingSlot);
					c.goto_(next);
					c.labelBinding(notFirst);
					c.dup();
					c.aload(pendingSlot);
					c.if_acmpeq(same);
					c.aload(pendingSlot);
					c.swap();
					c.invokevirtual(CD_THROWABLE, "addSuppressed", ADD_SUPPRESSED_DESC);
					c.goto_(next);
					c.labelBinding(same);
					c.pop();
				}
			}

			c.goto_(next);
		});
	}

	/**
	 * Emits the exception handlers collected by {@link #callListener}. Only reached through the exception table.
	 */
	private static void emitHandlers(CodeBuilder code, List<Consumer<CodeBuilder>> handlers) {
		for (Consumer<CodeBuilder> handler : handlers) {
			handler.accept(code);
		}
	}

	/**
	 * Combines the result of a listener, on the stack, with the result so far.
	 *
	 * @param early     where a short-circuiting invoker jumps with the result to return.
	 * @param passIndex the class data index of the pass value in an unrolled invoker, or -1 in a loop invoker.
	 * @param accSlot   the local of the result so far of {@link Mode#ALL_MATCH} and {@link Mode#ANY_MATCH}, or -1 if it is on the stack.
	 */
	private void combineResult(CodeBuilder code, ClassDesc self, ClassDesc returnDesc, Label early, int passIndex, int accSlot) {
		switch (mode) {
			case VOID -> { }
			case ALL_MATCH, ANY_MATCH -> {
				if (accSlot >= 0) code.iload(accSlot);

				if (mode == Mode.ALL_MATCH) {
					code.iand();
				} else {
					code.ior();
				}

				if (accSlot >= 0) code.istore(accSlot);
			}
			case CHAIN -> code.storeLocal(TypeKind.from(method.getReturnType()), code.parameterSlot(threadedParam));
			case SHORT_CIRCUIT -> {
				code.dup();
				branchIfNotPass(code, self, returnDesc, early, passIndex);
				code.pop();
			}
		}
	}

	/**
	 * With the result of a listener on the stack (twice), jumps to {@code target} with one copy if it is not the pass value.
	 * Falls through with one copy left on the stack otherwise.
	 *
	 * @param passIndex the class data index of the pass value in an unrolled invoker, or -1 in a loop invoker, where it is a field.
	 */
	private void branchIfNotPass(CodeBuilder code, ClassDesc self, ClassDesc returnDesc, Label target, int passIndex) {
		if (method.getReturnType() == boolean.class) {
			if ((Boolean) passValue) {
				code.ifeq(target);
			} else {
				code.ifne(target);
			}
		} else if (passValue == null) {
			code.ifnonnull(target);
		} else {
			loadPassReference(code, self, returnDesc, passIndex);
			code.if_acmpne(target);
		}
	}

	private void pushPass(CodeBuilder code, ClassDesc self, ClassDesc returnDesc, int passIndex) {
		if (method.getReturnType() == boolean.class) {
			code.loadConstant((Boolean) passValue ? 1 : 0);
		} else if (passValue == null) {
			code.aconst_null();
		} else {
			loadPassReference(code, self, returnDesc, passIndex);
		}
	}

	private void loadPassReference(CodeBuilder code, ClassDesc self, ClassDesc returnDesc, int passIndex) {
		if (passIndex >= 0) {
			code.ldc(classDataConstant(returnDesc, passIndex));
		} else {
			code.aload(0);
			code.getfield(self, "pass", returnDesc);
		}
	}

	/**
	 * The entry {@code index} of the class data of the hidden class, as a dynamic constant.
	 */
	private static DynamicConstantDesc<?> classDataConstant(ClassDesc type, int index) {
		return DynamicConstantDesc.ofNamed(ConstantDescs.BSM_CLASS_DATA_AT, ConstantDescs.DEFAULT_NAME, type, index);
	}

	private void loadParams(CodeBuilder code) {
		loadParams(code, method.getParameterTypes());
	}

	/**
	 * Loads the parameters of an instance method with the given parameter types onto the stack.
	 */
	static void loadParams(CodeBuilder code, Class<?>[] params) {
		for (int i = 0; i < params.length; i++) {
			code.loadLocal(TypeKind.from(params[i]), code.parameterSlot(i));
		}
	}

	/**
	 * Debug aid: writes every generated class to {@code <dir>/<package>/FabricEvent$<kind>_<counter>.class},
	 * so it can be inspected with {@code javap -c -p}. Enabled with {@code -Dfabric.event.dumpInvokers=<dir>}.
	 */
	private void dump(byte[] bytes, String kind) {
		try {
			Path dir = DUMP_DIR.resolve(type.getPackageName().replace('.', '/'));
			Files.createDirectories(dir);
			Files.write(dir.resolve("FabricEvent$" + kind + "_" + DUMP_COUNTER.getAndIncrement() + ".class"), bytes);
		} catch (IOException e) {
			LOGGER.warn("Could not dump generated event invoker for {}", type.getName(), e);
		}
	}
}
