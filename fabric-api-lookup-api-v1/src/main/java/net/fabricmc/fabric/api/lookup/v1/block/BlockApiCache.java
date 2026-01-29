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

package net.fabricmc.fabric.api.lookup.v1.block;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.impl.lookup.block.BlockApiCacheImpl;
import net.fabricmc.fabric.impl.lookup.block.BlockApiLookupImpl;

/**
 * A {@link BlockApiLookup} bound to a {@link ServerLevel} and a position, providing much faster API access.
 * Refer to {@link BlockApiLookup} for example code.
 *
 * <p>This object caches the block entity at the target position, and the last used API provider, removing those queries.
 * If a block entity is available or if the block state is passed as a parameter, the block state doesn't have to be looked up either.
 *
 * @param <A> The type of the API.
 * @param <C> The type of the additional context object.
 * @see BlockApiLookup
 */
@ApiStatus.NonExtendable
public interface BlockApiCache<A, C extends @Nullable Object> {
	/**
	 * Attempt to retrieve an API from a block in the world, using the world and the position passed at creation time.
	 *
	 * <p>Note: If the block state is known, it is more efficient to use {@link BlockApiCache#find(BlockState, Object)}.
	 *
	 * @param context Additional context for the query, defined by type parameter C.
	 * @return The retrieved API, or {@code null} if no API was found.
	 */
	@Nullable
	default A find(C context) {
		return find(null, context);
	}

	/**
	 * Attempt to retrieve an API from a block in the world, using the world and the position passed at creation time.
	 *
	 * @param state The block state at the target position, or null if unknown.
	 * @param context Additional context for the query, defined by type parameter C.
	 * @return The retrieved API, or {@code null} if no API was found.
	 */
	@Nullable
	A find(@Nullable BlockState state, C context);

	/**
	 * Return the block entity at the target position of this lookup.
	 *
	 * <p>This is the most efficient way to query the block entity at the target position repeatedly:
	 * unless the block entity has been loaded or unloaded since the last query, the result will be cached.
	 */
	@Nullable
	BlockEntity getBlockEntity();

	/**
	 * Return the lookup this cache is bound to.
	 */
	BlockApiLookup<A, C> getLookup();

	/**
	 * Return the world this cache is bound to.
	 */
	ServerLevel getWorld();

	/**
	 * Return the position this cache is bound to.
	 */
	BlockPos getPos();

	/**
	 * Create a new instance bound to the passed {@link ServerLevel} and position, and querying the same API as the passed lookup.
	 */
	static <A, C extends @Nullable Object> BlockApiCache<A, C> create(BlockApiLookup<A, C> lookup, ServerLevel world, BlockPos pos) {
		Objects.requireNonNull(pos, "BlockPos may not be null.");
		Objects.requireNonNull(world, "ServerWorld may not be null.");

		if (!(lookup instanceof BlockApiLookupImpl)) {
			throw new IllegalArgumentException("Cannot cache foreign implementation of BlockApiLookup. Use `BlockApiLookup#get(Identifier, Class<A>, Class<C>);` to get instances.");
		}

		return new BlockApiCacheImpl<>((BlockApiLookupImpl<A, C>) lookup, world, pos);
	}
}
