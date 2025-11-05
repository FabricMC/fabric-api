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

package net.fabricmc.fabric.api.networking.v1;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.mixin.networking.accessor.ChunkMapAccessor;
import net.fabricmc.fabric.mixin.networking.accessor.EntityTrackerAccessor;

/**
 * Helper methods to lookup players in a server.
 *
 * <p>The word "tracking" means that an entity/chunk on the server is known to a player's client (within in view distance) and the (block) entity should notify tracking clients of changes.
 *
 * <p>These methods should only be called on the server thread and only be used on logical a server.
 */
public final class PlayerLookup {
	/**
	 * Gets all the players on the minecraft server.
	 *
	 * <p>The returned collection is immutable.
	 *
	 * @param server the server
	 * @return all players on the server
	 */
	public static Collection<ServerPlayer> all(MinecraftServer server) {
		Objects.requireNonNull(server, "The server cannot be null");

		// return an immutable collection to guard against accidental removals.
		if (server.getPlayerList() != null) {
			return Collections.unmodifiableCollection(server.getPlayerList().getPlayers());
		}

		return Collections.emptyList();
	}

	/**
	 * Gets all the players in a server world.
	 *
	 * <p>The returned collection is immutable.
	 *
	 * @param world the server world
	 * @return the players in the server world
	 */
	public static Collection<ServerPlayer> world(ServerLevel world) {
		Objects.requireNonNull(world, "The world cannot be null");

		// return an immutable collection to guard against accidental removals.
		return Collections.unmodifiableCollection(world.players());
	}

	/**
	 * Gets all players tracking a chunk in a server world.
	 *
	 * @param world the server world
	 * @param pos   the chunk in question
	 * @return the players tracking the chunk
	 */
	public static Collection<ServerPlayer> tracking(ServerLevel world, ChunkPos pos) {
		Objects.requireNonNull(world, "The world cannot be null");
		Objects.requireNonNull(pos, "The chunk pos cannot be null");

		return world.getChunkSource().chunkMap.getPlayers(pos, false);
	}

	/**
	 * Gets all players tracking an entity in a server world.
	 *
	 * <p>The returned collection is immutable.
	 *
	 * <p><b>Warning</b>: If the provided entity is a player, it is not
	 * guaranteed by the contract that said player is included in the
	 * resulting stream.
	 *
	 * @param entity the entity being tracked
	 * @return the players tracking the entity
	 * @throws IllegalArgumentException if the entity is not in a server world
	 */
	public static Collection<ServerPlayer> tracking(Entity entity) {
		Objects.requireNonNull(entity, "Entity cannot be null");
		ChunkSource manager = entity.level().getChunkSource();

		if (manager instanceof ServerChunkCache) {
			ChunkMap chunkLoadingManager = ((ServerChunkCache) manager).chunkMap;
			EntityTrackerAccessor tracker = ((ChunkMapAccessor) chunkLoadingManager).getEntityTrackers().get(entity.getId());

			// return an immutable collection to guard against accidental removals.
			if (tracker != null) {
				return tracker.getPlayersTracking()
						.stream().map(ServerPlayerConnection::getPlayer).collect(Collectors.toUnmodifiableSet());
			}

			return Collections.emptySet();
		}

		throw new IllegalArgumentException("Only supported on server worlds!");
	}

	/**
	 * Gets all players tracking a block entity in a server world.
	 *
	 * @param blockEntity the block entity
	 * @return the players tracking the block position
	 * @throws IllegalArgumentException if the block entity is not in a server world
	 */
	public static Collection<ServerPlayer> tracking(BlockEntity blockEntity) {
		Objects.requireNonNull(blockEntity, "BlockEntity cannot be null");

		//noinspection ConstantConditions - IJ intrinsics don't know hasWorld == true will result in no null
		if (!blockEntity.hasLevel() || blockEntity.getLevel().isClientSide()) {
			throw new IllegalArgumentException("Only supported on server worlds!");
		}

		return tracking((ServerLevel) blockEntity.getLevel(), blockEntity.getBlockPos());
	}

	/**
	 * Gets all players tracking a block position in a server world.
	 *
	 * @param world the server world
	 * @param pos   the block position
	 * @return the players tracking the block position
	 */
	public static Collection<ServerPlayer> tracking(ServerLevel world, BlockPos pos) {
		Objects.requireNonNull(pos, "BlockPos cannot be null");

		return tracking(world, new ChunkPos(pos));
	}

	/**
	 * Gets all players around a position in a world.
	 *
	 * <p>The distance check is done in the three-dimensional space instead of in the horizontal plane.
	 *
	 * @param world  the world
	 * @param pos the position
	 * @param radius the maximum distance from the position in blocks
	 * @return the players around the position
	 */
	public static Collection<ServerPlayer> around(ServerLevel world, Vec3 pos, double radius) {
		double radiusSq = radius * radius;

		return world(world)
				.stream()
				.filter((p) -> p.distanceToSqr(pos) <= radiusSq)
				.collect(Collectors.toList());
	}

	/**
	 * Gets all players around a position in a world.
	 *
	 * <p>The distance check is done in the three-dimensional space instead of in the horizontal plane.
	 *
	 * @param world  the world
	 * @param pos    the position (can be a block pos)
	 * @param radius the maximum distance from the position in blocks
	 * @return the players around the position
	 */
	public static Collection<ServerPlayer> around(ServerLevel world, Vec3i pos, double radius) {
		double radiusSq = radius * radius;

		return world(world)
				.stream()
				.filter((p) -> p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) <= radiusSq)
				.collect(Collectors.toList());
	}

	private PlayerLookup() {
	}
}
