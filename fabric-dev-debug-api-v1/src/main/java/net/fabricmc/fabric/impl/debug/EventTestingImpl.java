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

package net.fabricmc.fabric.impl.debug;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.debug.v1.EventScope;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.loader.api.FabricLoader;

public final class EventTestingImpl {
	private EventTestingImpl() {
	}

	public static <T> EventScope registerScoped(Event<T> event, Identifier phase, T listener) {
		if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
			throw new IllegalArgumentException("EventScopes only work in development environments!");
		}

		if (!(event instanceof TestableArrayBackedEvent<T> testableEvent)) {
			throw new IllegalArgumentException("Event is not testable, something has gone very wrong!");
		}

		event.register(phase, listener);
		return new EventScopeImpl<>(testableEvent, phase, listener);
	}
}
