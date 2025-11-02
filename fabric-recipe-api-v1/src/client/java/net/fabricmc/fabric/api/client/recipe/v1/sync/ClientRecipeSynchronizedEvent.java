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

package net.fabricmc.fabric.api.client.recipe.v1.sync;

import net.minecraft.client.MinecraftClient;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes;

/**
 * This event gets invoked when client receives all synchronized recipes.
 * It will only be invoked if the server client is currently connected supports
 * Fabric's Recipe Sync packets.
 */
public interface ClientRecipeSynchronizedEvent {
	Event<ClientRecipeSynchronizedEvent> EVENT = EventFactory.createArrayBacked(ClientRecipeSynchronizedEvent.class,
			callbacks -> ((client, recipes) -> {
				for (ClientRecipeSynchronizedEvent callback : callbacks) {
					callback.onRecipesSynchronized(client, recipes);
				}
			}));

	void onRecipesSynchronized(MinecraftClient client, SynchronizedRecipes recipes);
}
