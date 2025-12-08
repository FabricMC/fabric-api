package net.fabricmc.fabric.test.base;

import java.io.Closeable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

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

	@Override
	public void close() {
		this.inScope = false;
	}

	public class Handler implements InvocationHandler {
		private final Object callback;

		<T> Handler(Event<T> event, T callback) {
			this.callback = callback;

			Class<T> handlerClass = TestEncapsulationBreaker3000.getClassOfEvent(event);
			//noinspection unchecked
			T proxy = (T) Proxy.newProxyInstance(handlerClass.getClassLoader(), new Class<?>[]{handlerClass}, this);
			event.register(proxy);
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// guard if we're out-of-scope
			if (!EventScope.this.inScope) {
				return null;
			}

			method.setAccessible(true); // hack because it's public abstract
			return method.invoke(this.callback, args);
		}
	}
}
