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

package net.fabricmc.fabric.api.debug.dev.v1;

import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.impl.debug.dev.EventScopeImpl;

/**
 * Represents a wrapper around a short-lived {@link Event}.
 * This class implements {@link AutoCloseable} and is intended to be used in a try-with-resources block.
 * When closed, the event will be unregistered.
 */
@ApiStatus.NonExtendable
public interface EventScope extends AutoCloseable {
	@Override
	void close();

	/**
	 * @param event The {@link Event} to be registered in an {@link EventScope}
	 * @param listener The event listener
	 * @param <T> is the type parameter for the event's listener
	 * @return a new {@link EventScope} instance holding the event, its listener and the event phase {@link Event#DEFAULT_PHASE}
	 */
	static <T> EventScope register(Event<T> event, T listener) {
		return register(event, Event.DEFAULT_PHASE, listener);
	}

	/**
	 * @param event The {@link Event} to be registered in an {@link EventScope}
	 * @param phase The {@linkplain EventFactory#createWithPhases(Class, Function, Identifier...) event phase}
	 * @param listener The event listener
	 * @param <T> is the type parameter for the event's listener
	 * @return a new {@link EventScope} instance holding the event, its listener and the event phase
	 * @see EventFactory#createWithPhases(Class, Function, Identifier...)
	 */
	static <T> EventScope register(Event<T> event, Identifier phase, T listener) {
		return EventScopeImpl.register(event, phase, listener);
	}
}
