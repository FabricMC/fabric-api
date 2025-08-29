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

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.util.Identifier;

/**
 * Base class for Fabric's event implementations.
 *
 * <h1>Cancellable events</h1>
 *
 * <p>Cancellable events are events that may be marked as cancelled to prevent the following listeners' code or vanilla behaviour from executing.
 *
 * <h2>Creating a cancellable event</h2>
 *
 * <p>For event to be practically cancellable, create an event using {@link EventFactory#createCancellable(Class, Function)} or its {@linkplain EventFactory#createCancellable(Class, Object, Function) overload}.
 * This gives you access to the event's {@link #cancelStatus}, whose methods you can then use to mark the event as cancelled.
 *
 * <p>Here's an example of a cancellable event with a void functional method that takes in a parameter of type {@code int}:
 *
 * <blockquote><pre>
 * interface Test {
 *     Event&lt;Test&gt; EVENT = EventFactory.createCancellable(Test.class,
 *     	cancelStatus -> listeners -> i -> {
 *     		cancelStatus.reset(); //*1
 *
 *     	    for (Test test : listeners) {
 *     	        if (cancelStatus.isCancelled()) {
 *     	            return;
 *     	        }
 *
 *     	        test.onRun(i);
 *     	    }
 *     	});
 *
 *     void onRun(int i);
 * }
 * </pre></blockquote>
 *
 * <p>*1: See {@link CancelStatus#reset()} for instructions when and where to call this method.
 *
 * <h2>Registering listeners</h2>
 *
 * <p>Register listeners using the {@link #registerCancellable(Function)} method to get access to the {@link #cancelStatus}.
 * Here's an example of such registration and cancellation:
 *
 * <blockquote><pre>
 * Event&lt;...&gt; EVENT = EventFactory.createCancellable(...);
 *
 * void onTest();
 *
 * void register() {
 *     EVENT.registerCancellable(cancelStatus -> () -> {
 *     	if (...) {
 *     	    cancelStatus.cancel();
 *     	}
 *     })
 * }
 * </pre></blockquote>
 *
 * <p>If you really need to cancel an event, follow these rules:
 * <ul>
 *     <li>First, consider if the cancelling is really necessary, and if the logic cannot be implemented somehow else.
 *     <li>If not, be as specific as you can to avoid stopping other listeners' code from executing.
 * </ul>
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

	protected CancelStatus cancelStatus = new CancelStatus();

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

	public void cancel() {
		this.cancelStatus.cancel();
	}

	public boolean isCancelled() {
		return this.cancelStatus.isCancelled();
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
	 * Have a look at {@link EventFactory#createCancellableWithPhases} for an explanation of event phases.
	 */
	public static final Identifier DEFAULT_PHASE = Identifier.of("fabric", "default");

	/**
	 * Register a listener to the event for the specified phase.
	 * Have a look at {@link EventFactory#createCancellableWithPhases} for an explanation of event phases.
	 *
	 * @param phase Identifier of the phase this listener should be registered for. It will be created if it didn't exist yet.
	 * @param listener The desired listener.
	 */
	public void register(Identifier phase, T listener) {
		// This is done to keep compatibility with existing Event subclasses, but they should really not be subclassing Event.
		register(listener);
	}

	public void registerCancellable(Function<CancelStatus, T> function) {
		register(function.apply(this.cancelStatus));
	}

	/**
	 * Request that listeners registered for one phase be executed before listeners registered for another phase.
	 * Relying on the default phases supplied to {@link EventFactory#createCancellableWithPhases} should be preferred over manually
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
}
