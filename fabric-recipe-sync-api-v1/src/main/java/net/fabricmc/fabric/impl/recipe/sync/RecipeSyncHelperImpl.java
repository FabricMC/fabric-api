package net.fabricmc.fabric.impl.recipe.sync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.recipe.sync.v1.RecipeSyncHelper;

public class RecipeSyncHelperImpl implements RecipeSyncHelper {
	public static final RecipeSyncHelperImpl INSTANCE = new RecipeSyncHelperImpl();
	private static final int MAX_RECIPES_PER_PACKET = 128;

	private final Map<ServerPlayerEntity, Set<Identifier>> syncRequests = new ConcurrentHashMap<>();
	private final Map<ServerPlayerEntity, Set<Identifier>> removalRequests = new ConcurrentHashMap<>();

	private final Set<Identifier> syncableSerializers = ConcurrentHashMap.newKeySet();

	private RecipeSyncHelperImpl() {}

	@Override
	public void markSyncableSerializer(Identifier serializerId) {
		syncableSerializers.add(serializerId);
	}

	@Override
	public void requestSync(ServerPlayerEntity player, Set<Identifier> recipeIds) {
		syncRequests.computeIfAbsent(player, k -> ConcurrentHashMap.newKeySet()).addAll(recipeIds);
	}

	@Override
	public void requestRemoval(ServerPlayerEntity player, Set<Identifier> recipeIds) {
		removalRequests.computeIfAbsent(player, k -> ConcurrentHashMap.newKeySet()).addAll(recipeIds);
	}

	@Override
	public void requestSyncAll(ServerPlayerEntity player, Predicate<Identifier> filter) {
		// TODO: Pull from real recipe registry and filter by syncableSerializers
		Set<Identifier> all = new HashSet<>(syncableSerializers); // stub
		Set<Identifier> filtered = new HashSet<>();
		for (Identifier id : all) {
			if (filter.test(id)) filtered.add(id);
		}
		requestSync(player, filtered);
	}

	@Override
	public void clearRequests() {
		syncRequests.clear();
	}

	public void flushToClient(ServerPlayerEntity player) {
		// Send revokes first
		Set<Identifier> toRevoke = removalRequests.getOrDefault(player, Collections.emptySet());
		if (!toRevoke.isEmpty()) {
			sendSplitPackets(player, toRevoke, true);
			removalRequests.remove(player);
		}
		// Then send syncs
		Set<Identifier> toSync = syncRequests.getOrDefault(player, Collections.emptySet());
		if (!toSync.isEmpty()) {
			sendSplitPackets(player, toSync, false);
			syncRequests.remove(player);
		}
	}

	private void sendSplitPackets(ServerPlayerEntity player, Set<Identifier> recipes, boolean revoke) {
		List<Identifier> recipeList = new ArrayList<>(recipes);
		int total = recipeList.size();
		for (int i = 0; i < total; i += MAX_RECIPES_PER_PACKET) {
			int end = Math.min(i + MAX_RECIPES_PER_PACKET, total);
			Set<Identifier> batch = new HashSet<>(recipeList.subList(i, end));
			boolean isLast = (end == total);
			ServerPlayNetworking.send(player, new RecipeSyncPayload(batch, revoke, isLast));
		}
	}

	public Set<Identifier> collectQueuedSyncs(ServerPlayerEntity player) {
		Set<Identifier> queued = syncRequests.getOrDefault(player, java.util.Collections.emptySet());
		return new java.util.HashSet<>(queued);
	}
}
