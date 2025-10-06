package net.fabricmc.fabric.impl.recipe.sync;

import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class ClientRecipeSyncState {
	private static final Set<Identifier> SYNCED_RECIPES = new HashSet<>();

	public static Set<Identifier> getSyncedRecipes() {
		return Collections.unmodifiableSet(SYNCED_RECIPES);
	}

	static void addRecipes(Set<Identifier> recipes) {
		SYNCED_RECIPES.addAll(recipes);
	}

	static void removeRecipes(Set<Identifier> recipes) {
		SYNCED_RECIPES.removeAll(recipes);
	}

	static void clear() {
		SYNCED_RECIPES.clear();
	}
}
