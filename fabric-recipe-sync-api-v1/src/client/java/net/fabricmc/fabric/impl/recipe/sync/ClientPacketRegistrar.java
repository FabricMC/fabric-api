package net.fabricmc.fabric.impl.recipe.sync;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ClientPacketRegistrar {
	public static void registerReceiver() {
		ClientPlayNetworking.registerGlobalReceiver(RecipeSyncPayload.ID, (payload, context) -> {
			if (payload.revoke()) {
				ClientRecipeSyncState.removeRecipes(payload.recipes());
			} else {
				ClientRecipeSyncState.addRecipes(payload.recipes());
			}
			if (payload.isLast()) {
				ClientRecipeSyncEvents.ON_SYNC_CHANGE.invoker().onRecipesChanged(ClientRecipeSyncState.getSyncedRecipes());
			}
		});
	}
}
