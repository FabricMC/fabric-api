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

package net.fabricmc.fabric.impl.lookup.block;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;

public final class BlockApiCacheImpl<A, C> implements BlockApiCache<A, C> {
	private final BlockApiLookupImpl<A, C> lookup;
	private final ServerLevel world;
	private final BlockPos pos;
	/**
	 * We always cache the block entity, even if it's null. We rely on BE load and unload events to invalidate the cache when necessary.
	 * blockEntityCacheValid maintains whether the cache is valid or not.
	 */
	private boolean blockEntityCacheValid = false;
	private BlockEntity cachedBlockEntity = null;
	/**
	 * We also cache the BlockApiProvider at the target position. We check if the block state has changed to invalidate the cache.
	 * lastState maintains for which block state the cachedProvider is valid.
	 */
	private BlockState lastState = null;
	private BlockApiLookup.BlockApiProvider<A, C> cachedProvider = null;

	public BlockApiCacheImpl(BlockApiLookupImpl<A, C> lookup, ServerLevel world, BlockPos pos) {
		((ServerWorldCache) world).fabric_registerCache(pos, this);
		this.lookup = lookup;
		this.world = world;
		this.pos = pos.immutable();
	}

	public void invalidate() {
		blockEntityCacheValid = false;
		cachedBlockEntity = null;
		lastState = null;
		cachedProvider = null;
	}

	@Nullable
	@Override
	public A find(@Nullable BlockState state, C context) {
		// Update block entity cache
		getBlockEntity();

		// Get block state
		if (state == null) {
			if (cachedBlockEntity != null) {
				state = cachedBlockEntity.getBlockState();
			} else {
				state = world.getBlockState(pos);
			}
		}

		// Get provider
		if (lastState != state) {
			cachedProvider = lookup.getProvider(state.getBlock());
			lastState = state;
		}

		// Query the provider
		A instance = null;

		if (cachedProvider != null) {
			instance = cachedProvider.find(world, pos, state, cachedBlockEntity, context);
		}

		if (instance != null) {
			return instance;
		}

		// Query the fallback providers
		for (BlockApiLookup.BlockApiProvider<A, C> fallbackProvider : lookup.getFallbackProviders()) {
			instance = fallbackProvider.find(world, pos, state, cachedBlockEntity, context);

			if (instance != null) {
				return instance;
			}
		}

		return null;
	}

	@Override
	@Nullable
	public BlockEntity getBlockEntity() {
		if (!blockEntityCacheValid) {
			cachedBlockEntity = world.getBlockEntity(pos);
			blockEntityCacheValid = true;
		}

		return cachedBlockEntity;
	}

	@Override
	public BlockApiLookupImpl<A, C> getLookup() {
		return lookup;
	}

	@Override
	public ServerLevel getWorld() {
		return world;
	}

	@Override
	public BlockPos getPos() {
		return pos;
	}

	static {
		ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, world) -> {
			((ServerWorldCache) world).fabric_invalidateCache(blockEntity.getBlockPos());
		});

		ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, world) -> {
			((ServerWorldCache) world).fabric_invalidateCache(blockEntity.getBlockPos());
		});
	}
}
