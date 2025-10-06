package net.fabricmc.fabric.api.recipe.sync.v1;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.function.Predicate;

public interface RecipeSyncHelper {

	void markSyncableSerializer(Identifier serializerId);

	void requestSync(ServerPlayerEntity player, Set<Identifier> recipeIds);

	void requestRemoval(ServerPlayerEntity player, Set<Identifier> recipeIds);

	void requestSyncAll(ServerPlayerEntity player, Predicate<Identifier> filter);

	void clearRequests();
}
