package net.fabricmc.fabric.api.recipe.sync.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Set;

public final class RecipeSyncEvents {
	public static final Event<BeforeSync> BEFORE_SYNC = EventFactory.createArrayBacked(
			BeforeSync.class, callbacks -> (player, recipes) -> {
				for (BeforeSync callback : callbacks) {
					callback.beforeSync(player, recipes);
				}
			}
	);

	public static final Event<AfterSync> AFTER_SYNC = EventFactory.createArrayBacked(
			AfterSync.class, callbacks -> (player, recipes) -> {
				for (AfterSync callback : callbacks) {
					callback.afterSync(player, recipes);
				}
			}
	);

	@FunctionalInterface
	public interface BeforeSync {
		void beforeSync(ServerPlayerEntity player, Set<Identifier> recipes);
	}

	@FunctionalInterface
	public interface AfterSync {
		void afterSync(ServerPlayerEntity player, Set<Identifier> recipes);
	}
}
