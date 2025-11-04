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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.MapMaker;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.event.Event;

public final class EventFactoryImpl {
	private static final Set<ArrayBackedEvent<?>> ARRAY_BACKED_EVENTS
			= Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());

	private EventFactoryImpl() { }

	public static void invalidate() {
		ARRAY_BACKED_EVENTS.forEach(ArrayBackedEvent::update);
	}

	public static <T> Event<T> createArrayBacked(Class<? super T> type, Function<T[], T> invokerFactory) {
		ArrayBackedEvent<T> event = new ArrayBackedEvent<>(type, invokerFactory);
		ARRAY_BACKED_EVENTS.add(event);
		return event;
	}

	public static void ensureContainsDefault(Identifier[] defaultPhases) {
		for (Identifier id : defaultPhases) {
			if (id.equals(Event.DEFAULT_PHASE)) {
				return;
			}
		}

		throw new IllegalArgumentException("The event phases must contain Event.DEFAULT_PHASE.");
	}

	public static void ensureNoDuplicates(Identifier[] defaultPhases) {
		for (int i = 0; i < defaultPhases.length; ++i) {
			for (int j = i+1; j < defaultPhases.length; ++j) {
				if (defaultPhases[i].equals(defaultPhases[j])) {
					throw new IllegalArgumentException("Duplicate event phase: " + defaultPhases[i]);
				}
			}
		}
	}

	// Code originally by sfPlayer1.
	// Unfortunately, it's slightly slower than just passing an empty array in the first place.
	private static <T> T buildEmptyInvoker(Class<T> handlerClass, Function<T[], T> invokerSetup) {
		// find the functional interface method
		Method funcIfMethod = null;

		for (Method m : handlerClass.getMethods()) {
			if ((m.getModifiers() & (Modifier.STRICT | Modifier.PRIVATE)) == 0) {
				if (funcIfMethod != null) {
					throw new IllegalStateException("Multiple virtual methods in " + handlerClass + "; cannot build empty invoker!");
				}

				funcIfMethod = m;
			}
		}

		if (funcIfMethod == null) {
			throw new IllegalStateException("No virtual methods in " + handlerClass + "; cannot build empty invoker!");
		}

		Object defValue = null;

		try {
			// concert to mh, determine its type without the "this" reference
			MethodHandle target = MethodHandles.lookup().unreflect(funcIfMethod);
			MethodType type = target.type().dropParameterTypes(0, 1);

			if (type.returnType() != void.class) {
				// determine default return value by invoking invokerSetup.apply(T[0]) with all-jvm-default args (null for refs, false for boolean, etc.)
				// explicitCastArguments is being used to cast Object=null to the jvm default value for the correct type

				// construct method desc (TLjava/lang/Object;Ljava/lang/Object;...)R where T = invoker ref ("this"), R = invoker ret type and args 1+ are Object for each non-"this" invoker arg
				MethodType objTargetType = MethodType.genericMethodType(type.parameterCount()).changeReturnType(type.returnType()).insertParameterTypes(0, target.type().parameterType(0));
				// explicit cast to translate to the invoker args from Object to their real type, inferring jvm default values
				MethodHandle objTarget = MethodHandles.explicitCastArguments(target, objTargetType);

				// build invocation args with 0 = "this", 1+ = null
				Object[] args = new Object[target.type().parameterCount()];
				//noinspection unchecked
				args[0] = invokerSetup.apply((T[]) Array.newInstance(handlerClass, 0));

				// retrieve default by invoking invokerSetup.apply(T[0]).targetName(def,def,...)
				defValue = objTarget.invokeWithArguments(args);
			}
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}

		final Object returnValue = defValue;
		//noinspection unchecked
		return (T) Proxy.newProxyInstance(EventFactoryImpl.class.getClassLoader(), new Class[]{handlerClass},
			(proxy, method, args) -> returnValue);
	}
}
