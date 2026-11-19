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

/**
 * The invoker of an event created with a {@link net.fabricmc.fabric.api.event.Combine}, which never changes.
 *
 * <p>{@link #object} is a generated class, whose method is a single {@code invokedynamic} onto a {@link VolatileCallSite}.
 * When the listeners of the event change, only the target of the call site does. The JIT treats the target of
 * a call site as a constant, and inlines it into the code that fires the event, with a dependency that
 * recompiles that code if the target ever changes.
 *
 * <p>The event itself is an instance of a hidden subclass of {@link ArrayBackedEvent}, which holds {@link #object}
 * in a final field and returns it from {@code invoker()}. The JIT trusts final fields of hidden classes, so when
 * the event is stored in a {@code static final} field, {@code invoker()} is folded to the constant stable invoker.
 * That class is defined once, and shared by all events.
 *
 * <p>Public only because the generated classes, which live in the package of the listener interface,
 * call {@link #bootstrap}.
 */
public final class StableInvoker<T> {
	private static final String PACKAGE = StableInvoker.class.getPackageName();
	private static final ClassDesc SELF = ClassDesc.of(StableInvoker.class.getName());
	private static final ClassDesc EVENT = ClassDesc.of(PACKAGE, "ArrayBackedEvent");
	private static final ClassDesc STABLE_EVENT = ClassDesc.of(PACKAGE, "FabricEvent$Stable");
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
	 * Makes the stable invoker do what the event does without listeners.
	 */
	void retargetEmpty() {
		site.setTarget(empty);
	}

	/**
	 * Makes the stable invoker call the given invoker from now on.
	 */
	void retarget(T invoker) {
		site.setTarget(virtual.bindTo(invoker));
	}

	/**
	 * Bootstrap of the {@code invokedynamic} instruction of the generated invokers:
	 * hands out the call site that is the class data of the generated class.
	 */
	public static CallSite bootstrap(MethodHandles.Lookup lookup, String name, MethodType type) throws IllegalAccessException {
		return MethodHandles.classData(lookup, "_", CallSite.class);
	}

	/**
	 * Defines the hidden subclass of {@link ArrayBackedEvent} that returns its stable invoker from {@code invoker()}.
	 */
	private static MethodHandle defineStableEventClass() {
		ClassDesc function = ClassDesc.of(Function.class.getName());

		byte[] bytes = ClassFile.of().build(STABLE_EVENT, cb -> {
			cb.withSuperclass(EVENT);
			cb.withFlags(ClassFile.ACC_FINAL | ClassFile.ACC_SYNTHETIC);
			cb.withField("invokerObject", ConstantDescs.CD_Object, ClassFile.ACC_PRIVATE | ClassFile.ACC_FINAL);

			cb.withMethodBody(ConstantDescs.INIT_NAME, MethodTypeDesc.of(ConstantDescs.CD_void, ConstantDescs.CD_Class, function, SELF, ConstantDescs.CD_Object), ClassFile.ACC_PUBLIC, code -> {
				code.aload(0);
				code.aload(1);
				code.aload(2);
				code.aload(3);
				code.invokespecial(EVENT, ConstantDescs.INIT_NAME, MethodTypeDesc.of(ConstantDescs.CD_void, ConstantDescs.CD_Class, function, SELF));
				code.aload(0);
				code.aload(4);
				code.putfield(STABLE_EVENT, "invokerObject", ConstantDescs.CD_Object);
				code.return_();
			});

			cb.withMethodBody("invoker", MethodTypeDesc.of(ConstantDescs.CD_Object), ClassFile.ACC_PUBLIC, code -> {
				code.aload(0);
				code.getfield(STABLE_EVENT, "invokerObject", ConstantDescs.CD_Object);
				code.areturn();
			});
		});

		try {
			MethodHandles.Lookup hidden = MethodHandles.lookup().defineHiddenClass(bytes, true);
			return hidden.findConstructor(hidden.lookupClass(), MethodType.methodType(void.class, Class.class, Function.class, StableInvoker.class, Object.class));
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Could not define the class of events with a stable invoker", e);
		}
	}
}
