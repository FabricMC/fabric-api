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
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.classfile.TypeKind;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.event.Combine;

/**
 * Builds the invokers of events created with a {@link Combine}, for listener interfaces with a single method.
 * The {@link Combine} is the only definition of how the listener results are combined,
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
 * <p>With a single listener, all combinations are equivalent to calling it directly, so it is used as the invoker.
 *
 * <p>Generating an unrolled class is far too slow for registration, so {@link #apply} never returns one.
 * {@link ArrayBackedEvent} asks for it separately via {@link #generateUnrolled}, off-thread.
 */
final class CombinedInvokerFactory<T> implements Function<T[], T> {
	/**
	 * The most listeners that get an unrolled invoker. The JIT does not compile methods above 8000 bytecodes,
	 * and every listener takes at most about 50 of them. Throughput of both falls steeply past about 32 listeners anyway.
	 */
	static final int MAX_UNROLLED = 128;
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
	private final Class<? super T> type;
	final Method method;
	private final Combine.Mode mode;
	private final @Nullable Object passValue;
	private final int threadedParam;
	final MethodHandles.Lookup lookup;
	private boolean unrolledBroken;

	private CombinedInvokerFactory(Class<? super T> type, Method method, Combine combine, MethodHandles.Lookup lookup) {
		this.type = type;
		this.method = method;
		this.mode = combine.mode();
		this.passValue = combine.passValue();
		this.threadedParam = combine.threadedParam();
		this.lookup = lookup;
	}

	/**
	 * Create the invoker factory of an event.
	 *
	 * @throws IllegalArgumentException if the listener interface or the combination are invalid.
	 */
	static <T> CombinedInvokerFactory<T> create(Class<? super T> type, Combine combine) {
		if (!type.isInterface()) throw new IllegalArgumentException(type.getName() + " is not an interface");

		Method found = null;

		for (Method m : type.getMethods()) {
			if (Modifier.isAbstract(m.getModifiers())) {
				if (found != null) throw new IllegalArgumentException(type.getName() + " has more than one abstract method, use an invoker factory instead of a Combine");
				found = m;
			}
		}

		if (found == null) throw new IllegalArgumentException(type.getName() + " has no abstract method");
		validate(type, found, combine);

		try {
			return new CombinedInvokerFactory<>(type, found, combine, MethodHandles.privateLookupIn(type, MethodHandles.lookup()));
		} catch (IllegalAccessException | RuntimeException e) {
			throw new IllegalArgumentException(type.getName() + " is not accessible to Fabric API, for example because its module does not open its package", e);
		}
	}

	private static void validate(Class<?> type, Method method, Combine combine) {
		Class<?> ret = method.getReturnType();
		Class<?>[] params = method.getParameterTypes();
		String where = type.getName() + "." + method.getName();

		switch (combine.mode()) {
			case VOID -> {
				if (ret != void.class) throw new IllegalArgumentException(where + " returns " + ret.getName() + ", but Combine.CALL_ALL needs void");
			}
			case ALL_MATCH -> {
				if (ret != boolean.class) throw new IllegalArgumentException(where + " returns " + ret.getName() + ", but Combine.ALL_TRUE needs boolean");
			}
			case CHAIN -> {
				int p = combine.threadedParam();

				if (ret == void.class || p >= params.length || params[p] != ret) {
					throw new IllegalArgumentException(where + " needs a parameter " + p + " of its return type " + ret.getName() + " for Combine.chain");
				}
			}
			case SHORT_CIRCUIT -> {
				Object pass = combine.passValue();

				if (ret == boolean.class) {
					if (!(pass instanceof Boolean)) throw new IllegalArgumentException(where + " returns boolean, use Combine.STOP_AT_FALSE or Combine.STOP_AT_TRUE");
				} else if (ret.isPrimitive()) {
					throw new IllegalArgumentException(where + " returns " + ret.getName() + ", which is not supported by this Combine");
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
		if (listeners.length == 1) return listeners[0];

		return newLoop(listeners);
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

	private LoopClass loopClass() throws ReflectiveOperationException {
		Map<String, LoopClass> classes = LOOP_CLASSES.get(type);
		LoopClass loopClass = classes.get(loopKey());

		if (loopClass == null) {
			loopClass = generateLoopClass();
			classes.put(loopKey(), loopClass);
		}

		return loopClass;
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
		if (n < 2 || n > MAX_UNROLLED || unrolledBroken) return null;

		try {
			byte[] bytes = generateUnrolledBytes(n);

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

	// ---- Class generation ----

	private boolean storesPass() {
		return mode == Combine.Mode.SHORT_CIRCUIT && passValue != null && method.getReturnType() != boolean.class;
	}

	/**
	 * Everything the bytecode of the loop invoker depends on. The pass value itself is stored in a field.
	 */
	private String loopKey() {
		String pass = !(mode == Combine.Mode.SHORT_CIRCUIT) ? "" : passValue == null ? "null" : passValue instanceof Boolean b ? b.toString() : "ref";
		return mode + "/" + threadedParam + "/" + pass;
	}

	private ClassDesc listenerDesc() {
		return type.describeConstable().orElseThrow();
	}

	private MethodTypeDesc methodDesc() {
		return MethodType.methodType(method.getReturnType(), method.getParameterTypes()).describeConstable().orElseThrow();
	}

	private static final MethodTypeDesc CONSTRUCTOR_DESC = MethodTypeDesc.of(ConstantDescs.CD_void, ConstantDescs.CD_Object.arrayType(), ConstantDescs.CD_Object);

	/**
	 * What the combination does without listeners, as a constant method handle for the call site of a stable invoker.
	 */
	MethodHandle emptyTarget() {
		MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
		Class<?> returnClass = methodType.returnType();

		return switch (mode) {
			case VOID -> MethodHandles.empty(methodType);
			case ALL_MATCH -> MethodHandles.dropArguments(MethodHandles.constant(boolean.class, true), 0, methodType.parameterList());
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
				Label top = code.newLabel();
				Label end = code.newLabel();
				Label early = code.newLabel();

				code.aload(0);
				code.getfield(self, "l", array);
				code.astore(arraySlot);
				code.loadConstant(0);
				code.istore(indexSlot);

				if (mode == Combine.Mode.ALL_MATCH) {
					// The result so far stays on the operand stack for the whole loop.
					code.loadConstant(1);
				}

				code.labelBinding(top);
				code.iload(indexSlot);
				code.aload(arraySlot);
				code.arraylength();
				code.if_icmpge(end);

				code.aload(arraySlot);
				code.iload(indexSlot);
				code.aaload();
				loadParams(code);
				code.invokeinterface(listener, method.getName(), methodDesc());

				switch (mode) {
					case VOID -> { }
					case ALL_MATCH -> code.iand();
					case CHAIN -> code.storeLocal(TypeKind.from(returnClass), code.parameterSlot(threadedParam));
					case SHORT_CIRCUIT -> {
						code.dup();
						branchIfNotPass(code, self, returnDesc, early, -1);
						code.pop();
					}
				}

				code.iinc(indexSlot, 1);
				code.goto_(top);

				code.labelBinding(end);

				switch (mode) {
					case VOID -> code.return_();
					case ALL_MATCH -> code.ireturn();
					case CHAIN -> {
						code.loadLocal(TypeKind.from(returnClass), code.parameterSlot(threadedParam));
						code.return_(TypeKind.from(returnClass));
					}
					case SHORT_CIRCUIT -> {
						pushPass(code, self, returnDesc, -1);
						code.labelBinding(early);
						code.return_(TypeKind.from(returnClass));
					}
				}
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
				switch (mode) {
					case VOID -> {
						for (int i = 0; i < n; i++) {
							invokeUnrolled(code, listener, i);
						}

						code.return_();
					}
					case ALL_MATCH -> {
						code.loadConstant(1);

						for (int i = 0; i < n; i++) {
							invokeUnrolled(code, listener, i);
							code.iand();
						}

						code.ireturn();
					}
					case CHAIN -> {
						TypeKind kind = TypeKind.from(returnClass);
						int threadedSlot = code.parameterSlot(threadedParam);

						for (int i = 0; i < n; i++) {
							invokeUnrolled(code, listener, i);
							code.storeLocal(kind, threadedSlot);
						}

						code.loadLocal(kind, threadedSlot);
						code.return_(kind);
					}
					case SHORT_CIRCUIT -> {
						Label done = code.newLabel();

						for (int i = 0; i < n; i++) {
							invokeUnrolled(code, listener, i);
							code.dup();
							branchIfNotPass(code, self, returnDesc, done, n);
							code.pop();
						}

						// Every listener returned the pass value.
						pushPass(code, self, returnDesc, n);
						code.labelBinding(done);
						code.return_(TypeKind.from(returnClass));
					}
				}
			});
		});
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

	/**
	 * Pushes listener {@code i} of an unrolled invoker, all arguments, then calls the listener.
	 */
	private void invokeUnrolled(CodeBuilder code, ClassDesc listener, int i) {
		code.ldc(classDataConstant(listener, i));
		loadParams(code);
		code.invokeinterface(listener, method.getName(), methodDesc());
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
