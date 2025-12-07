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

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.event.Event;

public class ScopedEvent<T> implements InvocationHandler {
	@Nullable
	private final Object defaultReturnValue;

	@Nullable
	private T activeHandler = null;

	public ScopedEvent(Event<T> event, Class<T> handlerClass, @Nullable Object defaultReturnValue) {
        this.defaultReturnValue = defaultReturnValue;

		//noinspection unchecked
		T proxy = (T) Proxy.newProxyInstance(ScopedEvent.class.getClassLoader(), new Class[]{handlerClass}, this);
		event.register(proxy);
    }

	public ScopedEvent(Event<T> event, Class<T> handlerClass) {
		this(event, handlerClass, null);
	}

	public Scope register(T handler) {
		activeHandler = handler;
		return () -> activeHandler = null;
	}

	// Needed to remove the throws
	public interface Scope extends Closeable {
		@Override
		void close();
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (activeHandler != null) {
			method.setAccessible(true);
			return method.invoke(activeHandler, args);
		}

		return defaultReturnValue;
	}
}
