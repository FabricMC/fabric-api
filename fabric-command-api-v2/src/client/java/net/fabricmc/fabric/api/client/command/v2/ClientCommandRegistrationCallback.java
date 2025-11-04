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

package net.fabricmc.fabric.api.client.command.v2;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandBuildContext;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Callback for when client commands are registered to the dispatcher.
 *
 * <p>To register some commands, you would register an event listener and implement the callback.
 *
 * <p>See {@link ClientCommandManager} for more details and an example.
 */
public interface ClientCommandRegistrationCallback {
	Event<ClientCommandRegistrationCallback> EVENT = EventFactory.createArrayBacked(ClientCommandRegistrationCallback.class, (callbacks) -> (dispatcher, registryAccess) -> {
		for (ClientCommandRegistrationCallback callback : callbacks) {
			callback.register(dispatcher, registryAccess);
		}
	});

	/**
	 * Called when registering client commands.
	 *
	 * @param dispatcher the command dispatcher to register commands to
	 * @param registryAccess object exposing access to the game's registries
	 */
	void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess);
}
