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

package net.fabricmc.fabric.api.recipe.v1.sync;

import java.util.Objects;

import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.impl.recipe.sync.RecipeSyncImpl;

/**
 * Since Minecraft 1.21.2, vanilla no longer syncs all recipes to the client automatically,
 * opting into sending only required recipe book data.
 *
 * <p>This api can be used to enable Fabric's recipe sync for select RecipeSerializers,
 * which will synchronize recipes to the client.
 * See {@link SynchronizedRecipes}
 */
public final class RecipeSynchronization {
	/**
	 * Event phase used for sending recipes to the client. It runs after the default event phase {@link Event#DEFAULT_PHASE}.
	 * It's defined for {@link ServerLifecycleEvents#SYNC_DATA_PACK_CONTENTS} event.
	 */
	public static final Identifier RECIPE_SYNC_EVENT_PHASE = RecipeSyncImpl.RECIPE_SYNC_EVENT_PHASE;

	private RecipeSynchronization() {
	}

	/**
	 * Enables synchronization of recipes to the client, for recipes that can be handled by
	 * the provided RecipeSerializer.
	 *
	 * <p>Only add recipe serializers that are provided by your own mod or vanilla.
	 * Blindly adding unchecked recipe serializers might cause bugs and crashes.
	 *
	 * <p>This method should be called in the main mod initializer on both the client and the server.
	 *
	 * @param serializer the recipe serializer used to synchronize recipes to the client.
	 */
	public static void synchronizeRecipeSerializer(RecipeSerializer<?> serializer) {
		Objects.requireNonNull(serializer, "serializer can't be null!");
		Objects.requireNonNull(serializer.packetCodec(), "PacketCodec can't be null!");

		RecipeSyncImpl.addSynchronizedSerializer(serializer);
	}
}
