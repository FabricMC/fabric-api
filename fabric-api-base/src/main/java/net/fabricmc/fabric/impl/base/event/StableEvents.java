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
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Function;

/**
 * Creates the events whose {@code invoker()} returns a {@link StableInvoker}.
 *
 * <p>They are instances of a hidden subclass of {@link ArrayBackedEvent}, that holds the stable invoker in a final field.
 * The JIT trusts final fields of hidden classes, so when such an event is stored in a {@code static final} field,
 * {@code invoker()} is folded to the constant stable invoker. The class is defined once, and shared by all events.
 */
final class StableEvents {
	private static final ClassDesc EVENT = ClassDesc.of(StableEvents.class.getPackageName(), "ArrayBackedEvent");
	private static final ClassDesc STABLE_INVOKER = ClassDesc.of(StableEvents.class.getPackageName(), "StableInvoker");
	private static final ClassDesc SELF = ClassDesc.of(StableEvents.class.getPackageName(), "FabricEvent$Stable");
	private static final MethodHandle CONSTRUCTOR = defineConstructor();

	private StableEvents() { }

	private static MethodHandle defineConstructor() {
		try {
			byte[] bytes = ClassFile.of().build(SELF, cb -> {
				cb.withSuperclass(EVENT);
				cb.withFlags(ClassFile.ACC_FINAL | ClassFile.ACC_SYNTHETIC);
				cb.withField("stable", ConstantDescs.CD_Object, ClassFile.ACC_PRIVATE | ClassFile.ACC_FINAL);

				ClassDesc function = ClassDesc.of("java.util.function.Function");

				cb.withMethodBody(ConstantDescs.INIT_NAME, MethodTypeDesc.of(ConstantDescs.CD_void, ConstantDescs.CD_Class, function, STABLE_INVOKER, ConstantDescs.CD_Object), ClassFile.ACC_PUBLIC, code -> {
					code.aload(0);
					code.aload(1);
					code.aload(2);
					code.aload(3);
					code.invokespecial(EVENT, ConstantDescs.INIT_NAME, MethodTypeDesc.of(ConstantDescs.CD_void, ConstantDescs.CD_Class, function, STABLE_INVOKER));
					code.aload(0);
					code.aload(4);
					code.putfield(SELF, "stable", ConstantDescs.CD_Object);
					code.return_();
				});

				cb.withMethodBody("invoker", MethodTypeDesc.of(ConstantDescs.CD_Object), ClassFile.ACC_PUBLIC, code -> {
					code.aload(0);
					code.getfield(SELF, "stable", ConstantDescs.CD_Object);
					code.areturn();
				});
			});

			MethodHandles.Lookup hidden = MethodHandles.lookup().defineHiddenClass(bytes, true);
			return hidden.findConstructor(hidden.lookupClass(), MethodType.methodType(void.class, Class.class, Function.class, StableInvoker.class, Object.class));
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Could not define the class of stable events", e);
		}
	}

	@SuppressWarnings("unchecked")
	static <T> ArrayBackedEvent<T> create(Class<? super T> type, Function<T[], T> invokerFactory, StableInvoker<T> stable) {
		try {
			return (ArrayBackedEvent<T>) CONSTRUCTOR.invoke(type, invokerFactory, stable, stable.object);
		} catch (Throwable t) {
			throw new IllegalStateException("Could not create a stable event", t);
		}
	}
}
