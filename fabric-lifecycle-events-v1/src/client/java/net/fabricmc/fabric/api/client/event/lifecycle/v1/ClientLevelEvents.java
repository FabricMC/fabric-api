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

package net.fabricmc.fabric.api.client.event.lifecycle.v1;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class ClientLevelEvents {
	private ClientLevelEvents() {
	}

	/**
	 * An event which is called after the client level has been changed.
	 */
	public static final Event<AfterClientLevelChange> AFTER_CLIENT_LEVEL_CHANGE = EventFactory.createArrayBacked(AfterClientLevelChange.class, callbacks -> (client, level) -> {
		for (AfterClientLevelChange callback : callbacks) {
			callback.afterLevelChange(client, level);
		}
	});

	@FunctionalInterface
	public interface AfterClientLevelChange {
		/**
		 * Called after the client level has been changed.
		 *
		 * @param client the client instance
		 * @param level the new level instance
		 */
		void afterLevelChange(Minecraft client, ClientLevel level);
	}
}
