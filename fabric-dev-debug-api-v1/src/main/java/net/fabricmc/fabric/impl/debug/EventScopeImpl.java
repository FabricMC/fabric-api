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

public class EventScopeImpl<T> implements EventScope {
	private final TestableArrayBackedEvent<T> event;
	private final Identifier phase;
	private final T listener;

	public EventScopeImpl(TestableArrayBackedEvent<T> event, Identifier phase, T listener) {
		this.event = event;
		this.phase = phase;
		this.listener = listener;
	}

	@Override
	public void close() {
		event.unregister(phase, listener);
	}
}
