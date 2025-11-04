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

package net.fabricmc.fabric.api.blockview.v2;

import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * General-purpose Fabric-provided extensions for {@link BlockGetter} subclasses.
 *
 * <p>These extensions were designed primarily for use by methods invoked during chunk building, but
 * they can also be used in other contexts.
 *
 * <p>Note: This interface is automatically implemented on all {@link BlockGetter} instances via Mixin and interface injection.
 */
public interface FabricBlockView {
	/**
	 * Retrieves block entity render data for a given block position.
	 *
	 * <p>This method must be used instead of {@link BlockGetter#getBlockEntity(BlockPos)} in cases
	 * where the user knows that the current context may be multithreaded, such as chunk building, to
	 * ensure thread safety and data consistency. Using a {@link BlockEntity} directly may not be
	 * thread-safe since it may lead to non-atomic modification of the internal state of the
	 * {@link BlockEntity} (such as through lazy computation). Using a {@link BlockEntity} directly
	 * may not be consistent since the internal state of the {@link BlockEntity} may change on a
	 * different thread.
	 *
	 * <p>As previously stated, a common environment to use this method in is chunk building. Methods
	 * that are invoked during chunk building and that thus should use this method include, but are
	 * not limited to, {@code FabricBakedModel#emitBlockQuads} (block models),
	 * {@code BlockColorProvider#getColor} (block color providers), and
	 * {@code FabricBlock#getAppearance} (block appearance computation).
	 *
	 * <p>Users of this method are required to check the returned object before using it. Users must
	 * check if it is null and if it is of the correct type to avoid null pointer and class cast
	 * exceptions, as the returned data is not guaranteed to be what the user expects. A simple way
	 * to implement these checks is to use {@code instanceof}, since it always returns {@code false}
	 * if the object is null. If the {@code instanceof} returns {@code false}, a fallback path should
	 * be used.
	 *
	 * @param pos the position of the block entity
	 * @return the render data provided by the block entity, or null if there is no block entity at this position
	 *
	 * @see RenderDataBlockEntity
	 */
	@Nullable
	default Object getBlockEntityRenderData(BlockPos pos) {
		BlockEntity blockEntity = ((BlockGetter) this).getBlockEntity(pos);
		return blockEntity == null ? null : blockEntity.getRenderData();
	}

	/**
	 * Checks whether biome retrieval is supported. The returned value will not change between
	 * multiple calls of this method. See {@link #getBiomeFabric(BlockPos)} for more information.
	 *
	 * @return whether biome retrieval is supported
	 * @see #getBiomeFabric(BlockPos)
	 */
	default boolean hasBiomes() {
		return false;
	}

	/**
	 * Retrieves the biome at the given position if biome retrieval is supported. If
	 * {@link #hasBiomes()} returns {@code true}, this method will always return a non-null
	 * {@link Holder} whose {@link Holder#value() value} is non-null. If
	 * {@link #hasBiomes()} returns {@code false}, this method will always return {@code null}.
	 *
	 * <p>Prefer using {@link LevelReader#getBiome(BlockPos)} instead of this method if this instance
	 * is known to implement {@link LevelReader}.
	 *
	 * @implNote Implementations which do not return null are encouraged to use the plains biome as
	 * the default value, for example when the biome at the given position is unknown.
	 *
	 * @param pos the position for which to retrieve the biome
	 * @return the biome, or null if biome retrieval is not supported
	 * @see #hasBiomes()
	 */
	@UnknownNullability
	default Holder<Biome> getBiomeFabric(BlockPos pos) {
		return null;
	}
}
