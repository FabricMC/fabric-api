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

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.base.event.EventFactoryImpl;

/**
 * Helper for creating {@link Event} classes.
 */
public final class EventFactory {
	private EventFactory() { }

	/**
	 * How the listeners of an event created with {@link #create(Class, Behavior)} are called, and their results combined.
	 * Fabric API generates the invoker from the behavior and the method of the listener interface,
	 * which must be the only abstract one.
	 */
	public enum Behavior {
		/**
		 * Call every listener in order. For listener methods returning {@code void}.
		 */
		INVOKE_ALL,
		/**
		 * Call the listeners in order until one returns something other than {@code null}, which is returned.
		 * Returns {@code null} if all listeners returned {@code null}, or if there are none.
		 * For listener methods returning a reference type.
		 */
		FIRST_NON_NULL,
		/**
		 * Call the listeners in order until one returns {@code false}, which is returned.
		 * Returns {@code true} if all listeners returned {@code true}, or if there are none.
		 * For listener methods returning {@code boolean}, typically "allow" events.
		 */
		STOP_ON_FALSE,
		/**
		 * Call the listeners in order until one returns {@code true}, which is returned.
		 * Returns {@code false} if all listeners returned {@code false}, or if there are none.
		 * For listener methods returning {@code boolean}, typically "cancel" or "handled" events.
		 */
		STOP_ON_TRUE,
		/**
		 * Call <b>every</b> listener, and return whether all of them returned {@code true} ({@code true} if there are none).
		 * Unlike {@link #STOP_ON_FALSE}, listeners are still called after one returned {@code false}.
		 * For listener methods returning {@code boolean}.
		 */
		ALL_TRUE,
		/**
		 * Call <b>every</b> listener, and return whether any of them returned {@code true} ({@code false} if there are none).
		 * Unlike {@link #STOP_ON_TRUE}, listeners are still called after one returned {@code true}.
		 * For listener methods returning {@code boolean}.
		 */
		ANY_TRUE
	}

	/**
	 * What an event created with {@link #create(Class, Behavior, ExceptionHandling)} does when a listener throws.
	 * Only {@link Exception}s are handled, including checked ones thrown by non-Java code; {@link Error}s always propagate.
	 * A listener that threw is skipped: the remaining listeners are called and combined as if it had not been registered.
	 */
	public enum ExceptionHandling {
		/**
		 * The exception propagates to the caller of the invoker, and the remaining listeners are not called. The default.
		 */
		PROPAGATE,
		/**
		 * The exception is ignored.
		 */
		SUPPRESS,
		/**
		 * The exception is logged as an error.
		 */
		SUPPRESS_AND_LOG,
		/**
		 * Once the invoker is done, the first exception is rethrown, with the later ones
		 * {@linkplain Throwable#addSuppressed(Throwable) added as suppressed}.
		 */
		ADD_SUPPRESSED
	}

	/**
	 * Create an Event instance that calls every listener in order. For listener interfaces with a single {@code void} method.
	 *
	 * <p>Same as {@link #create(Class, Behavior) create(type, Behavior.INVOKE_ALL)}.
	 *
	 * @param type The listener interface. For a generic listener interface, cast the class literal:
	 *             {@code (Class<MyCallback<Integer>>) (Class<?>) MyCallback.class}.
	 * @param <T>  The listener type.
	 * @return The Event instance.
	 * @throws IllegalArgumentException if the listener interface does not have a single abstract {@code void} method, or is not accessible to Fabric API.
	 */
	public static <T> Event<T> create(Class<T> type) {
		return create(type, Behavior.INVOKE_ALL);
	}

	/**
	 * Create an Event instance whose listeners are called and combined as described by the {@code behavior}.
	 * For listener interfaces with a single abstract method.
	 *
	 * <p>Fabric API generates the invoker, and replaces it with one that calls each listener from its own call site,
	 * which the JIT can inline, once listeners are registered. Use {@link #createArrayBacked(Class, Function)}
	 * for anything that does not fit one of the behaviors.
	 *
	 * @param type     The listener interface.
	 * @param behavior How the listeners are called, and their results combined.
	 * @param <T>      The listener type.
	 * @return The Event instance.
	 * @throws IllegalArgumentException if the method of the listener interface does not fit the {@code behavior}, or is not accessible to Fabric API.
	 */
	public static <T> Event<T> create(Class<T> type, Behavior behavior) {
		return create(type, behavior, ExceptionHandling.PROPAGATE);
	}

	/**
	 * Like {@link #create(Class, Behavior)}, with a choice of what happens when a listener throws an exception.
	 *
	 * @param type       The listener interface.
	 * @param behavior   How the listeners are called, and their results combined.
	 * @param exceptions What happens when a listener throws an exception.
	 * @param <T>        The listener type.
	 * @return The Event instance.
	 * @throws IllegalArgumentException if the method of the listener interface does not fit the {@code behavior}, or is not accessible to Fabric API.
	 */
	public static <T> Event<T> create(Class<T> type, Behavior behavior, ExceptionHandling exceptions) {
		return createArrayBacked(type, invoker(type, behavior, exceptions));
	}

	/**
	 * The invoker factory that Fabric API generates for a listener interface, a {@code behavior} and {@code exceptions},
	 * to pass to the other factory methods, like {@link #createWithPhases(Class, Function, Identifier...)}.
	 * The event then gets the same generated invoker as with {@link #create(Class, Behavior, ExceptionHandling)}.
	 * Anywhere else, it is a regular invoker factory. It can be shared by several events of the same listener interface.
	 *
	 * @param type       The listener interface.
	 * @param behavior   How the listeners are called, and their results combined.
	 * @param exceptions What happens when a listener throws an exception.
	 * @param <T>        The listener type.
	 * @return The invoker factory.
	 * @throws IllegalArgumentException if the method of the listener interface does not fit the {@code behavior}, or is not accessible to Fabric API.
	 */
	public static <T> Function<T[], T> invoker(Class<T> type, Behavior behavior, ExceptionHandling exceptions) {
		return EventFactoryImpl.invoker(type, behavior, exceptions);
	}

	/**
	 * Create an "array-backed" Event instance.
	 *
	 * <p>If the listeners are only combined in a common way, like calling all of them or stopping at the first
	 * {@code false}, use {@link #create(Class, Behavior)} instead: Fabric API then generates an invoker
	 * that is several times faster when there are several listeners.
	 *
	 * @param type           The listener class type.
	 * @param invokerFactory The invoker factory, combining multiple listeners into one instance.
	 * @param <T>            The listener type.
	 * @return The Event instance.
	 */
	public static <T> Event<T> createArrayBacked(Class<? super T> type, Function<T[], T> invokerFactory) {
		return EventFactoryImpl.createArrayBacked(type, invokerFactory);
	}

	/**
	 * Create an "array-backed" Event instance with a custom empty invoker,
	 * for an event whose {@code invokerFactory} only delegates to the listeners.
	 * <ul>
	 *   <li>If there is no listener, the custom empty invoker will be used.</li>
	 *   <li><b>If there is only one listener, that one will be used as the invoker
	 *   and the factory will not be called.</b></li>
	 *   <li>Only when there are at least two listeners will the factory be used.</li>
	 * </ul>
	 *
	 * <p>Having a custom empty invoker (of type (...) -&gt; {}) increases performance
	 * relative to iterating over an empty array; however, it only really matters
	 * if the event is executed thousands of times a second.
	 *
	 * @param type           The listener class type.
	 * @param emptyInvoker   The custom empty invoker.
	 * @param invokerFactory The invoker factory, combining multiple listeners into one instance.
	 * @param <T>            The listener type.
	 * @return The Event instance.
	 */
	public static <T> Event<T> createArrayBacked(Class<T> type, T emptyInvoker, Function<T[], T> invokerFactory) {
		return createArrayBacked(type, listeners -> {
			if (listeners.length == 0) {
				return emptyInvoker;
			} else if (listeners.length == 1) {
				return listeners[0];
			} else {
				return invokerFactory.apply(listeners);
			}
		});
	}

	/**
	 * Create an array-backed event with a list of default phases that get invoked in order.
	 * Exposing the identifiers of the default phases as {@code public static final} constants is encouraged.
	 *
	 * <p>An event phase is a named group of listeners, which may be ordered before or after other groups of listeners.
	 * This allows some listeners to take priority over other listeners.
	 * Adding separate events should be considered before making use of multiple event phases.
	 *
	 * <p>Phases may be freely added to events created with any of the factory functions,
	 * however using this function is preferred for widely used event phases.
	 * If more phases are necessary, discussion with the author of the Event is encouraged.
	 *
	 * <p>Refer to {@link Event#addPhaseOrdering} for an explanation of event phases.
	 *
	 * <p>For the generated invoker of {@link #create(Class, Behavior, ExceptionHandling)}, pass the {@link #invoker} as the invoker factory.
	 *
	 * @param type           The listener class type.
	 * @param invokerFactory The invoker factory, combining multiple listeners into one instance.
	 * @param defaultPhases  The default phases of this event, in the correct order. Must contain {@link Event#DEFAULT_PHASE}.
	 * @param <T>            The listener type.
	 * @return The Event instance.
	 */
	public static <T> Event<T> createWithPhases(Class<? super T> type, Function<T[], T> invokerFactory, Identifier... defaultPhases) {
		EventFactoryImpl.ensureContainsDefault(defaultPhases);
		EventFactoryImpl.ensureNoDuplicates(defaultPhases);

		Event<T> event = createArrayBacked(type, invokerFactory);

		EventFactoryImpl.addDefaultPhases(event, defaultPhases);
		return event;
	}

	/**
	 * @deprecated This is not to be used in events anymore.
	 */
	@Deprecated
	public static String getHandlerName(Object handler) {
		return handler.getClass().getName();
	}

	/**
	 * @deprecated Always returns {@code false}, do not use. This is not to be used in events anymore, standard Java profilers will do fine.
	 */
	@Deprecated
	public static boolean isProfilingEnabled() {
		return false;
	}

	/**
	 * Invalidate and re-create all existing "invoker" instances across
	 * events created by this EventFactory. Use this if, for instance,
	 * the profilingEnabled field changes.
	 *
	 * @deprecated Do not use, will be removed in a future release.
	 */
	@Deprecated(forRemoval = true)
	public static void invalidate() {
		EventFactoryImpl.invalidate();
	}
}
