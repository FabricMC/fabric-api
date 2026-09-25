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

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventScope;

// Because this needs to be public, we call it this so it doesn't auto complete.
public final class ScopedEventListenerImpl<T> implements EventScope {
	private final ArrayBackedEvent<T> event;
	private final Identifier phase;
	private final T listener;

	public ScopedEventListenerImpl(
			Event<T> event,
			Identifier phase,
			T listener
	) {
		// This is done for compatibility with subclasses of Event even though mods
		//  absolutely should not be doing that.
		if (!(event instanceof ArrayBackedEvent<T> arrayBackedEvent)) {
			throw new IllegalArgumentException("Only ArrayBackedEvent instances may be scoped. Do not create classes extending Event!");
		}

		this.event = arrayBackedEvent;
		this.phase = phase;
		this.listener = listener;
	}

	@Override
	public void close() {
		this.event.unregister(this.phase, this.listener);
	}
}
