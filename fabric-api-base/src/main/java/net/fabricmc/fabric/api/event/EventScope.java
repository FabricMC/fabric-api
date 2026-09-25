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
 * A closeable wrapper around a terminal {@link Event} listener.
 * When an instance of {@link EventScope} is closed,
 * the {@linkplain Event event's listener} will be unregistered.
 *
 * <p>
 * This class implements {@link AutoCloseable}
 * and is intended to be used in a try-with-resources block.
 */
@ApiStatus.NonExtendable
public interface EventScope extends AutoCloseable {
	@Override
	void close();

	static <T> EventScope create(Event<T> event, T listener) {
		return create(event, Event.DEFAULT_PHASE, listener);
	}

	static <T> EventScope create(Event<T> event, Identifier phase, T listener) {
		event.register(phase, listener);
		return new ScopedEventListenerImpl<>(event, phase, listener);
	}
}
