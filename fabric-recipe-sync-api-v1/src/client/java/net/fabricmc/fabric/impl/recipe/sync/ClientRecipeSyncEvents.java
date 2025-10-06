package net.fabricmc.fabric.impl.recipe.sync;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import net.minecraft.util.Identifier;

import java.util.Set;

public final class ClientRecipeSyncEvents {
	public static final Event<OnSyncChange> ON_SYNC_CHANGE =
			EventFactory.createArrayBacked(OnSyncChange.class, listeners -> recipes -> {
				for (OnSyncChange listener : listeners) {
					listener.onRecipesChanged(recipes);
				}
			});

	@FunctionalInterface
	public interface OnSyncChange {
		void onRecipesChanged(Set<Identifier> syncedRecipes);
	}

	private ClientRecipeSyncEvents() {
	}
}
