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

package net.fabricmc.fabric.impl.debug.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;

import net.fabricmc.fabric.api.client.debug.v1.DebugKeyBindingRegistry;

public final class DebugKeyBindingRegistryImpl {
	private static final Map<KeyMapping, DebugKeyBindingRegistry.DebugKeyHandler> BINDINGS = new ConcurrentHashMap<>();
	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-debug-api-v1");

	public static synchronized void register(KeyMapping keyMapping, DebugKeyBindingRegistry.DebugKeyHandler handler) {
		if (BINDINGS.containsKey(keyMapping)) {
			throw new IllegalStateException(
					"KeyMapping " + keyMapping.getName() + " is already registered for a fabric debug action"
			);
		}

		BINDINGS.put(keyMapping, handler);
	}

	public static boolean invoke(KeyEvent event, boolean didAction) {
		for (Map.Entry<KeyMapping, DebugKeyBindingRegistry.DebugKeyHandler> entry : BINDINGS.entrySet()) {
			KeyMapping keyMapping = entry.getKey();

			if (keyMapping.matches(event)) {
				try {
					didAction = entry.getValue().onDebugKey() || didAction;
				} catch (Throwable t) {
					LOGGER.error("Exception running debug key handler for {}", keyMapping.getName(), t);
				}
			}
		}

		return didAction;
	}
}
