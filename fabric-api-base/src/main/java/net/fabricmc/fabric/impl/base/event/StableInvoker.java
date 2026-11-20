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
import java.io.InputStream;
import java.lang.classfile.ClassFile;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VolatileCallSite;
import java.lang.reflect.Method;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

/**
 * The invoker of an event created with a {@link net.fabricmc.fabric.api.event.EventFactory#create}, which never changes.
 *
 * <p>{@link #object} is a generated class, whose method is a single {@code invokedynamic} onto a {@link VolatileCallSite}.
 * When the listeners of the event change, only the target of the call site does. The JIT treats the target of
 * a call site as a constant, and inlines it into the code that fires the event, with a dependency that
 * recompiles that code if the target ever changes.
 *
 * <p>The event itself is an instance of {@link StableArrayBackedEvent}, defined again as a hidden class, which holds
 * {@link #object} in a final field and returns it from {@code invoker()}. The JIT trusts final fields of hidden classes,
 * so when the event is stored in a {@code static final} field, {@code invoker()} is folded to the constant stable invoker.
 * That hidden class is defined once, and shared by all events.
 *
 * <p>Public only because the generated classes, which live in the package of the listener interface,
 * call {@link #bootstrap}.
 */
public final class StableInvoker<T> {
	private static final ClassDesc SELF = ClassDesc.of(StableInvoker.class.getName());
	/**
	 * The class file of {@link StableArrayBackedEvent}, next to this one.
	 */
	private static final String STABLE_EVENT_CLASS_FILE = "StableArrayBackedEvent.class";
	private static final MethodHandle STABLE_EVENT_CONSTRUCTOR = defineStableEventClass();

	/**
	 * The generated invoker, implementing the listener interface.
	 */
	final T object;
	/**
	 * Volatile, so that a listener registered on one thread is seen by all others, like with the volatile invoker field of
	 * {@link net.fabricmc.fabric.api.event.Event}. A {@link java.lang.invoke.MutableCallSite} gives no such guarantee:
	 * its {@code syncAll} is not implemented beyond a store fence.
	 */
	private final VolatileCallSite site;
	/**
	 * The listener method, with the receiver as an additional first parameter.
	 */
	private final MethodHandle virtual;
	/**
	 * What the event does without listeners, as a constant method handle: no class is needed for it.
	 */
	private final MethodHandle empty;

	/**
	 * The invoker the call site currently targets, {@code null} for the constant one of an event without listeners.
	 */
	private @Nullable T current;
	/**
	 * The loop invoker of this event, kept while listeners are registered, so the call site does not need a new target.
	 */
	private @Nullable T loop;
	/**
	 * How many times the call site got a new target, which recompiles the code that fires the event. For tests.
	 */
	int retargets;

	private StableInvoker(T object, VolatileCallSite site, MethodHandle virtual, MethodHandle empty) {
		this.object = object;
		this.site = site;
		this.virtual = virtual;
		this.empty = empty;
	}

	/**
	 * Creates an event whose {@code invoker()} returns a stable invoker, which calls the invokers of the factory.
	 */
	@SuppressWarnings("unchecked")
	static <T> ArrayBackedEvent<T> createEvent(Class<? super T> type, CombinedInvokerFactory<T> factory) {
		Method method = factory.method;
		MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
		MethodTypeDesc desc = methodType.describeConstable().orElseThrow();

		byte[] bytes = ClassFile.of().build(ClassDesc.of(type.getPackageName(), "FabricEvent$Invoker"), cb -> {
			cb.withFlags(ClassFile.ACC_FINAL | ClassFile.ACC_SYNTHETIC);
			cb.withInterfaceSymbols(type.describeConstable().orElseThrow());

			cb.withMethodBody(ConstantDescs.INIT_NAME, ConstantDescs.MTD_void, ClassFile.ACC_PUBLIC, code -> {
				code.aload(0);
				code.invokespecial(ConstantDescs.CD_Object, ConstantDescs.INIT_NAME, ConstantDescs.MTD_void);
				code.return_();
			});

			cb.withMethodBody(method.getName(), desc, ClassFile.ACC_PUBLIC, code -> {
				CombinedInvokerFactory.loadParams(code, method.getParameterTypes());
				code.invokedynamic(DynamicCallSiteDesc.of(ConstantDescs.ofCallsiteBootstrap(SELF, "bootstrap", ConstantDescs.CD_CallSite), method.getName(), desc));
				code.return_(TypeKind.from(method.getReturnType()));
			});
		});

		try {
			VolatileCallSite site = new VolatileCallSite(methodType);
			MethodHandles.Lookup hidden = factory.lookup.defineHiddenClassWithClassData(bytes, site, true);
			T object = (T) hidden.findConstructor(hidden.lookupClass(), MethodType.methodType(void.class)).invoke();
			MethodHandle virtual = factory.lookup.findVirtual(type, method.getName(), methodType);
			StableInvoker<T> stable = new StableInvoker<>(object, site, virtual, factory.emptyTarget());
			return (ArrayBackedEvent<T>) STABLE_EVENT_CONSTRUCTOR.invoke(type, factory, stable, object);
		} catch (Throwable t) {
			throw new IllegalStateException("Could not create the invoker of an event for " + type.getName(), t);
		}
	}

	/**
	 * Points the call site at the invoker for the given listeners.
	 *
	 * <p>Every new target makes the JIT throw away the code that inlined the old one. Registering listeners one by one
	 * would do that each time, so with two listeners or more, the call site targets the loop invoker of this event,
	 * and registering only replaces its listener array. The call site gets a new target again when the unrolled
	 * invoker is {@linkplain #install installed}, once registrations have settled.
	 */
	void update(T[] listeners, CombinedInvokerFactory<T> factory) {
		switch (listeners.length) {
			case 0 -> {
				if (current != null || retargets == 0) {
					target(empty, null);
				}
			}
			case 1 -> {
				if (factory.passesSingleListener()) {
					target(listeners[0]);
				} else {
					targetLoop(listeners, factory);
				}
			}
			default -> targetLoop(listeners, factory);
		}
	}

	private void targetLoop(T[] listeners, CombinedInvokerFactory<T> factory) {
		if (loop == null) {
			loop = factory.newLoop(listeners);
		} else {
			factory.setLoopListeners(loop, listeners);
		}

		target(loop);
	}

	/**
	 * Points the call site at the unrolled invoker.
	 */
	void install(T unrolled) {
		target(unrolled);
	}

	/**
	 * The invoker the call site currently targets, or the stable invoker itself for an event without listeners.
	 */
	T current() {
		return current != null ? current : object;
	}

	private void target(T invoker) {
		if (invoker != current) {
			target(virtual.bindTo(invoker), invoker);
		}
	}

	private void target(MethodHandle target, @Nullable T invoker) {
		site.setTarget(target);
		current = invoker;
		retargets++;
	}

	/**
	 * Bootstrap of the {@code invokedynamic} instruction of the generated invokers:
	 * hands out the call site that is the class data of the generated class.
	 */
	public static CallSite bootstrap(MethodHandles.Lookup lookup, String name, MethodType type) throws IllegalAccessException {
		return MethodHandles.classData(lookup, "_", CallSite.class);
	}

	/**
	 * Defines {@link StableArrayBackedEvent} again as a hidden class, from its own class file, so its final field is trusted.
	 */
	private static MethodHandle defineStableEventClass() {
		// The class is named as a string: a class literal would load the template as a regular class too.
		try (InputStream in = StableInvoker.class.getResourceAsStream(STABLE_EVENT_CLASS_FILE)) {
			if (in == null) throw new IllegalStateException("Could not find " + STABLE_EVENT_CLASS_FILE);

			MethodHandles.Lookup hidden = MethodHandles.lookup().defineHiddenClass(in.readAllBytes(), true);
			return hidden.findConstructor(hidden.lookupClass(), MethodType.methodType(void.class, Class.class, Function.class, StableInvoker.class, Object.class));
		} catch (IOException | ReflectiveOperationException e) {
			throw new IllegalStateException("Could not define the class of events with a stable invoker", e);
		}
	}
}
