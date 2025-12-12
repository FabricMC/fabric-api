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

package net.fabricmc.fabric.test.base;

import java.io.Closeable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.base.event.TestEncapsulationBreaker3000;

/**
 * <h1 style="color:red">⚠️Performance Warning⚠️</h1>
 * Because it is impossible to remove listeners from events,
 * this class <b>can and will destroy your memory.</b>
 *
 * <p style="font-size:1.5em"><u>Only use this for testing!</u>
 */
public class EventScope implements Closeable {
	private boolean inScope = true;
	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
	private final List<Handler> handlers = new ArrayList<>();

	public <T> void register(Event<T> event, T callback) {
		handlers.add(new Handler(event, callback));
	}

	public <T> void registerEarlyReturn(Event<T> event, T callback, Object defaultValue) {
		handlers.add(new Handler(event, callback, defaultValue));
	}

	public <T> void registerEarlyReturn(Event<T> event, T callback, Function<Object[], Object> defaultValue) {
		handlers.add(new Handler(event, callback, defaultValue));
	}

	@Override
	public void close() {
		this.inScope = false;
	}

	public class Handler implements InvocationHandler {
		private final Object callback;
		private @Nullable Object defaultValue;
		private @Nullable Function<Object[], Object> defaultValueGetter;

		<T> Handler(Event<T> event, T callback) {
			this.callback = callback;

			Class<T> handlerClass = TestEncapsulationBreaker3000.getClassOfEvent(event);
			//noinspection unchecked
			T proxy = (T) Proxy.newProxyInstance(handlerClass.getClassLoader(), new Class<?>[]{handlerClass}, this);
			event.register(proxy);
		}

		<T> Handler(Event<T> event, T callback, Object defaultValue) {
			this(event, callback);
			this.defaultValue = defaultValue;
		}

		<T> Handler(Event<T> event, T callback, Function<Object[], Object> defaultValueGetter) {
			this(event, callback);
			this.defaultValueGetter = defaultValueGetter;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// event scopes truly are the event scopes we have at home:
			// guard if we're out-of-scope
			if (!EventScope.this.inScope) {
				if (this.defaultValueGetter != null) {
					return this.defaultValueGetter.apply(args);
				}

				return this.defaultValue;
			}

			method.setAccessible(true); // hack because it's public abstract
			return method.invoke(this.callback, args);
		}
	}
}
