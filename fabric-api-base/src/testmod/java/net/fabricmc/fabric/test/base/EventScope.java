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

package net.fabricmc.fabric.test.base;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.base.event.TestEncapsulationBreaker3000;

/**
 * A convenient container for using multiple
 * {@linkplain ScopedEvent scoped events} in one try-with-resources block.
 */
public class EventScope implements Closeable {
	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
	private final List<ScopedEvent.Scope> scopes = new ArrayList<>();

	public <T> void register(Event<T> event, T callback) {
		ScopedEvent<T> scopedEvent = new ScopedEvent<>(
				event,
				TestEncapsulationBreaker3000.getClassOfEvent(event)
		);
		this.scopes.add(scopedEvent.register(callback));
	}

	public <T> void registerEarlyReturn(Event<T> event, T callback, Object defaultReturnValue) {
		ScopedEvent<T> scopedEvent = new ScopedEvent<>(
				event,
				TestEncapsulationBreaker3000.getClassOfEvent(event),
				defaultReturnValue
		);
		this.scopes.add(scopedEvent.register(callback));
	}

	public <T> void registerEarlyReturn(Event<T> event, T callback, Function<Object[], Object> defaultReturner) {
		ScopedEvent<T> scopedEvent = new ScopedEvent<>(
				event,
				TestEncapsulationBreaker3000.getClassOfEvent(event),
				defaultReturner
		);
		this.scopes.add(scopedEvent.register(callback));
	}

	@Override
	public void close() {
		for (ScopedEvent.Scope scope : this.scopes) {
			scope.close();
		}
	}
}
