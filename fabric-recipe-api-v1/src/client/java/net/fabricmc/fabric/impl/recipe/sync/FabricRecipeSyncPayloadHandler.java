package net.fabricmc.fabric.impl.recipe.sync;

import net.minecraft.recipe.PreparedRecipes;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.recipe.v1.PopulateSyncableRecipesCallback;

public final class FabricRecipeSyncPayloadHandler {
	private FabricRecipeSyncPayloadHandler() {
	}

	public static void registerPayloadsAndReceivers() {
		PayloadTypeRegistry.playS2C().register(FabricSyncRecipesPayload.ID, FabricSyncRecipesPayload.PACKET_CODEC);

		ClientPlayNetworking.registerGlobalReceiver(FabricSyncRecipesPayload.ID, (payload, context) -> {
			final PreparedRecipes recipes = PreparedRecipes.of(payload.recipeEntries());
			PopulateSyncableRecipesCallback.EVENT.invoker().populateRecipes(recipes);
		});
	}
}
