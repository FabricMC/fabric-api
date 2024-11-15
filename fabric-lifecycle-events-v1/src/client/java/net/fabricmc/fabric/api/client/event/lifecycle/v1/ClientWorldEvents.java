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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class ClientWorldEvents {
	private ClientWorldEvents() {
	}

	/**
	 * An event which is called after the client world has been changed.
	 */
	public static final Event<AfterClientWorldChange> AFTER_CLIENT_WORLD_CHANGE = EventFactory.createArrayBacked(AfterClientWorldChange.class, callbacks -> (client, world) -> {
		for (AfterClientWorldChange callback : callbacks) {
			callback.afterWorldChange(client, world);
		}
	});

	@FunctionalInterface
	public interface AfterClientWorldChange {
		/**
		 * Called after the client world has been changed.
		 *
		 * @param client the client instance
		 * @param world the new world instance
		 */
		void afterWorldChange(MinecraftClient client, ClientWorld world);
	}
}
