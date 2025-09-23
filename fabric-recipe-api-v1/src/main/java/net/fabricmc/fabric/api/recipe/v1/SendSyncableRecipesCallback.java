package net.fabricmc.fabric.api.recipe.v1;

import java.util.Collection;

import it.unimi.dsi.fastutil.objects.ReferenceSet;

import net.fabricmc.fabric.api.event.Event;

import net.fabricmc.fabric.api.event.EventFactory;

import net.minecraft.recipe.RecipeType;

public interface SendSyncableRecipesCallback {
	Event<SendSyncableRecipesCallback> EVENT = EventFactory.createArrayBacked(SendSyncableRecipesCallback.class, callbacks -> recipeTypesToSend -> {
		for (SendSyncableRecipesCallback callback : callbacks) {
			callback.sendRecipes(recipeTypesToSend);
		}
	});
	void sendRecipes(ReferenceSet<RecipeType<?>> recipeTypesToSend);
}
