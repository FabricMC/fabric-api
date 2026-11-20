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

package net.fabricmc.fabric.api.event;

import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.impl.base.event.CombinedInvokerFactory;
import net.fabricmc.fabric.impl.base.event.CombinedInvokerFactory.Mode;

/**
 * Invoker factories that combine the listeners of an event in one of a few common ways,
 * for listener interfaces with a single method. Use them instead of writing the invoker factory by hand:
 *
 * <pre>{@code
 * Event<AllowDamage> ALLOW_DAMAGE = EventFactory.createArrayBacked(AllowDamage.class, EventInvokers.stopAtFalse(AllowDamage.class));
 * }</pre>
 *
 * <p>Passed to {@link EventFactory#createArrayBacked(Class, Function)} or {@link EventFactory#createWithPhases},
 * Fabric API generates the invoker, and transparently replaces it with a faster one that calls each listener
 * from its own call site, which the JIT can inline. The behavior is always the one documented on the factory method.
 * Anywhere else, they are regular invoker factories.
 *
 * <p>The listener interface is checked when the invoker factory is created, which throws
 * {@link IllegalArgumentException} if it does not fit, or is not accessible to Fabric API.
 * An invoker factory can be shared by several events of the same listener interface.
 *
 * <p>For anything that does not fit, for example listeners that must be wrapped in a {@code try}/{@code catch},
 * keep writing your own invoker factory.
 */
public final class EventInvokers {
	private EventInvokers() { }

	/**
	 * Call every listener in registration (and phase) order. For listener methods returning {@code void}.
	 *
	 * <pre>{@code
	 * for (Listener listener : listeners) listener.call(args);
	 * }</pre>
	 *
	 * @param type The listener interface.
	 * @param <T>  The listener type.
	 * @return the invoker factory
	 */
	public static <T> Function<T[], T> callAll(Class<? super T> type) {
		return CombinedInvokerFactory.create(type, Mode.VOID, null, -1);
	}

	/**
	 * Call the listeners in order until one returns {@code false}, which is returned.
	 * Returns {@code true} if all listeners returned {@code true}, or if there are none.
	 * For listener methods returning {@code boolean}, typically "allow" events.
	 *
	 * <pre>{@code
	 * for (Listener listener : listeners) {
	 *     if (!listener.call(args)) return false;
	 * }
	 * return true;
	 * }</pre>
	 *
	 * @param type The listener interface.
	 * @param <T>  The listener type.
	 * @return the invoker factory
	 */
	public static <T> Function<T[], T> stopAtFalse(Class<? super T> type) {
		return CombinedInvokerFactory.create(type, Mode.SHORT_CIRCUIT, Boolean.TRUE, -1);
	}

	/**
	 * Call the listeners in order until one returns {@code true}, which is returned.
	 * Returns {@code false} if all listeners returned {@code false}, or if there are none.
	 * For listener methods returning {@code boolean}, typically "cancel" or "handled" events.
	 *
	 * <pre>{@code
	 * for (Listener listener : listeners) {
	 *     if (listener.call(args)) return true;
	 * }
	 * return false;
	 * }</pre>
	 *
	 * @param type The listener interface.
	 * @param <T>  The listener type.
	 * @return the invoker factory
	 */
	public static <T> Function<T[], T> stopAtTrue(Class<? super T> type) {
		return CombinedInvokerFactory.create(type, Mode.SHORT_CIRCUIT, Boolean.FALSE, -1);
	}

	/**
	 * Call <b>every</b> listener, and return whether all of them returned {@code true}
	 * ({@code true} if there are none). Unlike {@link #stopAtFalse}, listeners are still called after one returned {@code false}.
	 * For listener methods returning {@code boolean}.
	 *
	 * <pre>{@code
	 * boolean result = true;
	 * for (Listener listener : listeners) result &= listener.call(args);
	 * return result;
	 * }</pre>
	 *
	 * @param type The listener interface.
	 * @param <T>  The listener type.
	 * @return the invoker factory
	 */
	public static <T> Function<T[], T> allTrue(Class<? super T> type) {
		return CombinedInvokerFactory.create(type, Mode.ALL_MATCH, null, -1);
	}

	/**
	 * Call <b>every</b> listener, and return whether any of them returned {@code true}
	 * ({@code false} if there are none). Unlike {@link #stopAtTrue}, listeners are still called after one returned {@code true}.
	 * For listener methods returning {@code boolean}.
	 *
	 * <pre>{@code
	 * boolean result = false;
	 * for (Listener listener : listeners) result |= listener.call(args);
	 * return result;
	 * }</pre>
	 *
	 * @param type The listener interface.
	 * @param <T>  The listener type.
	 * @return the invoker factory
	 */
	public static <T> Function<T[], T> anyTrue(Class<? super T> type) {
		return CombinedInvokerFactory.create(type, Mode.ANY_MATCH, null, -1);
	}

	/**
	 * Call the listeners in order until one returns something other than {@code null}, which is returned.
	 * Returns {@code null} if all listeners returned {@code null}, or if there are none.
	 * For listener methods returning a reference type.
	 *
	 * <pre>{@code
	 * for (Listener listener : listeners) {
	 *     Result result = listener.call(args);
	 *     if (result != null) return result;
	 * }
	 * return null;
	 * }</pre>
	 *
	 * @param type The listener interface.
	 * @param <T>  The listener type.
	 * @return the invoker factory
	 */
	public static <T> Function<T[], T> firstNonNull(Class<? super T> type) {
		return CombinedInvokerFactory.create(type, Mode.SHORT_CIRCUIT, null, -1);
	}

	/**
	 * Call the listeners in order until one returns something other than {@code passValue}, which is returned.
	 * Returns {@code passValue} if all listeners returned it, or if there are none.
	 * Results are compared by identity ({@code !=}).
	 * For listener methods returning a reference type, typically with a constant like {@code InteractionResult.PASS}.
	 *
	 * <pre>{@code
	 * for (Listener listener : listeners) {
	 *     Result result = listener.call(args);
	 *     if (result != passValue) return result;
	 * }
	 * return passValue;
	 * }</pre>
	 *
	 * @param type      The listener interface.
	 * @param passValue The result that lets the next listener run.
	 * @param <T>       The listener type.
	 * @return the invoker factory
	 */
	public static <T> Function<T[], T> firstOtherThan(Class<? super T> type, @Nullable Object passValue) {
		return CombinedInvokerFactory.create(type, Mode.SHORT_CIRCUIT, passValue, -1);
	}

	/**
	 * Pass the result of each listener as one of the parameters of the next, and return the result of the last.
	 * With no listeners, that parameter is returned unchanged.
	 * For listener methods returning the same type as the parameter at the given index.
	 *
	 * <pre>{@code
	 * for (Listener listener : listeners) value = listener.call(other, value);
	 * return value;
	 * }</pre>
	 *
	 * @param type          The listener interface.
	 * @param threadedParam The index of the parameter that receives the previous result, starting at 0.
	 * @param <T>           The listener type.
	 * @return the invoker factory
	 */
	public static <T> Function<T[], T> chain(Class<? super T> type, int threadedParam) {
		return CombinedInvokerFactory.create(type, Mode.CHAIN, null, threadedParam);
	}
}
