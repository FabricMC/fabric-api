package net.fabricmc.fabric.api.recipe.v1;

import net.fabricmc.fabric.api.event.Event;

import net.fabricmc.fabric.api.event.EventFactory;

import net.minecraft.recipe.PreparedRecipes;

public interface PopulateSyncableRecipesCallback {
	Event<PopulateSyncableRecipesCallback> EVENT = EventFactory.createArrayBacked(PopulateSyncableRecipesCallback.class, callbacks -> recipes -> {
		for (PopulateSyncableRecipesCallback callback : callbacks) {
			callback.populateRecipes(recipes);
		}
	});

	void populateRecipes(PreparedRecipes recipes);
}
