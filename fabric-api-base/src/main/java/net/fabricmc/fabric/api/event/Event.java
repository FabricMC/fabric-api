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

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.base.event.ScopedEventListenerImpl;

/**
 * Base class for Fabric's event implementations.
 *
 * @param <T> The listener type.
 * @see EventFactory
 */
@ApiStatus.NonExtendable // Should only be extended by fabric API.
public abstract class Event<T> {
	/**
	 * The invoker field. This should be updated by the implementation to
	 * always refer to an instance containing all code that should be
	 * executed upon event emission.
	 */
	protected volatile T invoker;

	/**
	 * Returns the invoker instance.
	 *
	 * <p>An "invoker" is an object which hides multiple registered
	 * listeners of type T under one instance of type T, executing
	 * them and leaving early as necessary.
	 *
	 * @return The invoker instance.
	 */
	public final T invoker() {
		return invoker;
	}

	/**
	 * Register a listener to the event, in the default phase.
	 * Have a look at {@link #addPhaseOrdering} for an explanation of event phases.
	 *
	 * @param listener The desired listener.
	 */
	public abstract void register(T listener);

	/**
	 * The identifier of the default phase.
	 * Have a look at {@link EventFactory#createWithPhases} for an explanation of event phases.
	 */
	public static final Identifier DEFAULT_PHASE = Identifier.fromNamespaceAndPath("fabric", "default");

	/**
	 * Register a listener to the event for the specified phase.
	 * Have a look at {@link EventFactory#createWithPhases} for an explanation of event phases.
	 *
	 * @param phase Identifier of the phase this listener should be registered for. It will be created if it didn't exist yet.
	 * @param listener The desired listener.
	 */
	public void register(Identifier phase, T listener) {
		// This is done to keep compatibility with existing Event subclasses, but they should really not be subclassing Event.
		register(listener);
	}

	/// Register a temporary listener to the event.
	/// Have a look at [EventFactory#createWithPhases] for an explanation of event phases.
	///
	/// Scoped event listeners are a kind of temporary event listener that is invoked when
	/// the event is invoked until the associated [EventScope] is closed.
	/// Because [EventScope] extends [AutoCloseable], it is intended to be used in
	/// a try-with-resources so it only lives as long as the try-with-resources block's scope.
	///
	/// ## Listener Ordering
	///
	/// For performance reasons, ordering of scoped listeners and permanent listeners
	/// within the same phase is not guaranteed and **should not be relied on**!
	/// [Create][#addPhaseOrdering(Identifier, Identifier)] or use earlier phases
	/// if you are using scoped listeners on a short-circuiting event or otherwise care about
	/// listener invocation order.
	///
	/// @param listener The desired listener.
	/// @return A closeable wrapper around a temporary event listener.
	/// @apiNote Creating and closing scoped event listeners are
	/// performance intensive actions and should be done
	/// sparingly and infrequently when used outside game tests.
	/// Scope creation and destruction have `O(n)` time complexity
	/// where `n` is the number of subscribed event listeners.
	/// @see EventScope
	/// @see #registerScoped(Identifier, Object)
	public EventScope registerScoped(T listener) {
		// This is not abstract to avoid breaking existing Event subclasses, but they should really not be subclassing Event.
		return ScopedEventListenerImpl.EMPTY_FOR_COMPATIBILITY_REASONS_SORRY;
	}

	/// Register a temporary listener to the event for the specified phase.
	/// Have a look at [EventFactory#createWithPhases] for an explanation of event phases.
	///
	/// Scoped event listeners are a kind of temporary event listener that is invoked when
	/// the event is invoked until the associated [EventScope] is closed.
	/// Because [EventScope] extends [AutoCloseable], it is intended to be used in
	/// a try-with-resources so it only lives as long as the try-with-resources block's scope.
	///
	/// ## Listener Ordering
	///
	/// For performance reasons, ordering of scoped listeners and permanent listeners
	/// within the same phase is not guaranteed and **should not be relied on**!
	/// [Create][#addPhaseOrdering(Identifier, Identifier)] or use earlier phases
	/// if you are using scoped listeners on a short-circuiting event or otherwise care about
	/// listener invocation order.
	///
	/// @param phase Identifier of the phase this listener should be registered for. It will be created if it didn't exist yet.
	/// @param listener The desired listener.
	/// @return A closeable wrapper around a temporary event listener.
	/// @apiNote Creating and closing scoped event listeners are
	/// performance intensive actions and should be done
	/// sparingly and infrequently when used outside game tests.
	/// Scope creation and destruction have `O(n)` time complexity
	/// where `n` is the number of subscribed event listeners.
	/// @see EventScope
	/// @see #registerScoped(Object)
	public EventScope registerScoped(Identifier phase, T listener) {
		// This is not abstract to avoid breaking existing Event subclasses, but they should really not be subclassing Event.
		return ScopedEventListenerImpl.EMPTY_FOR_COMPATIBILITY_REASONS_SORRY;
	}

	/**
	 * Request that listeners registered for one phase be executed before listeners registered for another phase.
	 * Relying on the default phases supplied to {@link EventFactory#createWithPhases} should be preferred over manually
	 * registering phase ordering dependencies.
	 *
	 * <p>Incompatible ordering constraints such as cycles will lead to inconsistent behavior:
	 * some constraints will be respected and some will be ignored. If this happens, a warning will be logged.
	 *
	 * @param firstPhase The identifier of the phase that should run before the other. It will be created if it didn't exist yet.
	 * @param secondPhase The identifier of the phase that should run after the other. It will be created if it didn't exist yet.
	 */
	public void addPhaseOrdering(Identifier firstPhase, Identifier secondPhase) {
		// This is not abstract to avoid breaking existing Event subclasses, but they should really not be subclassing Event.
	}

	/**
	 * Do not call this method if you are not {@code fabric-api-base}.
	 * This method will break API or ABI at any time.
	 */
	// The only reason why this method isn't protected/private is so we can avoid any terrible hacks.
	@ApiStatus.OverrideOnly
	public void unregister(Identifier phase, T listener) {
		// This is not abstract to avoid breaking existing Event subclasses, but they should really not be subclassing Event.
	}
}
