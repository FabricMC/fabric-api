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

package net.fabricmc.fabric.impl.debug.dev;

import java.util.Objects;
import java.util.function.Function;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.base.event.ArrayBackedEvent;

public class TestableArrayBackedEvent<T> extends ArrayBackedEvent<T> {
	TestableArrayBackedEvent(Class<? super T> type, Function<T[], T> invokerFactory) {
		super(type, invokerFactory);
	}

	public void unregister(Identifier phaseIdentifier, T listener) {
		Objects.requireNonNull(phaseIdentifier, "Tried to unregister a listener for a null phase!");
		Objects.requireNonNull(listener, "Tried to unregister a null listener!");

		synchronized (lock) {
			if (getOrCreatePhase(phaseIdentifier, false).removeListener(listener)) {
				rebuildInvoker(handlers.length - 1);
			}
		}
	}
}
