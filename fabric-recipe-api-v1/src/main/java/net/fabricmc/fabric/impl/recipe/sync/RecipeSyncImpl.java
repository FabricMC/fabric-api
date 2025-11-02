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

package net.fabricmc.fabric.impl.recipe.sync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.mixin.recipe.sync.ServerCommonNetworkHandlerAccessor;
import net.fabricmc.fabric.mixin.recipe.sync.ServerRecipeManagerAccessor;

public class RecipeSyncImpl implements ModInitializer {
	// Recipe packet might contain a lot of data depending on mods, so it's best to increase it's max size to 64 MB.
	private static final int RECIPE_PAYLOAD_MAX_SIZE = 64 * 1024 * 1024;
	private static final Set<RecipeSerializer<?>> SYNCED_SERIALIZERS = new ReferenceOpenHashSet<>();

	public static final Identifier RECIPE_SYNC_EVENT_PHASE = Identifier.of("fabric", "recipe_sync");

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.configurationC2S().register(SupportedRecipeSerializersPayloadC2S.ID, SupportedRecipeSerializersPayloadC2S.CODEC);
		PayloadTypeRegistry.playS2C().registerLarge(RecipeSyncPayloadS2C.ID, RecipeSyncPayloadS2C.CODEC, RECIPE_PAYLOAD_MAX_SIZE);

		ServerConfigurationNetworking.registerGlobalReceiver(SupportedRecipeSerializersPayloadC2S.ID, RecipeSyncImpl::onRecipeSyncRequest);

		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.addPhaseOrdering(Event.DEFAULT_PHASE, RECIPE_SYNC_EVENT_PHASE);
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(RECIPE_SYNC_EVENT_PHASE, RecipeSyncImpl::sendRecipes);
	}

	private static void onRecipeSyncRequest(SupportedRecipeSerializersPayloadC2S payload, ServerConfigurationNetworking.Context context) {
		var set = new ReferenceOpenHashSet<RecipeSerializer<?>>();

		for (Identifier identifier : payload.synchronizedSerializers()) {
			Registries.RECIPE_SERIALIZER.getOptionalValue(identifier).ifPresent(set::add);
		}

		((SyncedSerializerAwareClientConnection) ((ServerCommonNetworkHandlerAccessor) context.networkHandler()).getConnection())
				.fabric_setSyncedRecipeSerializers(set);
	}

	private static void sendRecipes(ServerPlayerEntity player, boolean exist) {
		if (!ServerPlayNetworking.canSend(player, RecipeSyncPayloadS2C.ID)) {
			return;
		}

		Set<RecipeSerializer<?>> serializers = ((SyncedSerializerAwareClientConnection) ((ServerCommonNetworkHandlerAccessor) player.networkHandler).getConnection()).fabric_getSyncedRecipeSerializers();

		SyncedSerializerAwarePreparedRecipe accessor = (SyncedSerializerAwarePreparedRecipe) ((ServerRecipeManagerAccessor) player.getEntityWorld().getRecipeManager()).getPreparedRecipes();

		var list = new ArrayList<RecipeSyncPayloadS2C.Entry>();

		for (RecipeSerializer<?> serializer : serializers) {
			List<RecipeEntry<?>> recipes = accessor.fabric_getRecipesBySyncedSerializer(serializer);

			if (recipes != null && !recipes.isEmpty()) {
				list.add(new RecipeSyncPayloadS2C.Entry(serializer, recipes));
			}
		}

		if (list.isEmpty()) {
			return;
		}

		ServerPlayNetworking.send(player, new RecipeSyncPayloadS2C(list));
	}

	public static void addSynchronizedSerializer(RecipeSerializer<?> serializer) {
		SYNCED_SERIALIZERS.add(serializer);
	}

	public static boolean isSynced(RecipeSerializer<?> serializer) {
		return SYNCED_SERIALIZERS.contains(serializer);
	}

	public static Set<RecipeSerializer<?>> getSyncedSerializers() {
		return Collections.unmodifiableSet(SYNCED_SERIALIZERS);
	}
}
