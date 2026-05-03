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

package net.fabricmc.fabric.impl.client.rendering;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.rendering.v1.BlockTintsFactory;

public final class BlockColorRegistryImpl {
	@Nullable
	private static BlockColors blockColors;
	@Nullable
	private static Map<Block, List<BlockTintSource>> map = new IdentityHashMap<>();

	private static Map<Block, BlockTintsFactory> factories = new IdentityHashMap<>();

	public static void initialize(BlockColors blockColors) {
		if (BlockColorRegistryImpl.blockColors != null) {
			return;
		}

		BlockColorRegistryImpl.blockColors = blockColors;

		map.forEach((block, color) -> blockColors.register(color, block));
		map = null;
	}

	public static void register(List<BlockTintSource> layers, Block... blocks) {
		for (final Block block : blocks) {
			if (factories.containsKey(block)) {
				throw new IllegalStateException("A dynamic block color factory for the block %s has already been registered and as such no static usage is allowed!".formatted(
						block
				));
			}
		}

		if (blockColors != null) {
			blockColors.register(layers, blocks);
		} else {
			for (Block block : blocks) {
				map.put(block, layers);
			}
		}
	}

	public static void register(final BlockTintsFactory factory, final Block[] blocks) {
		for (final Block block : blocks) {
			if (map != null && map.containsKey(block)) {
				throw new IllegalStateException("A static block color provider for the block: %s has already been registered and as such no dynamic usage is allowed!".formatted(
						block));
			}

			if (blockColors != null && !blockColors.getTintSources(block.defaultBlockState()).isEmpty()) {
				throw new IllegalStateException(
						"A static block color provider for the block: %s has already been registered and as such no dynamic usage is allowed!".formatted(
								block));
			}

			factories.put(block, factory);
		}
	}

	public static @Nullable BlockTintsFactory getFactory(final BlockState blockState) {
		return factories.get(blockState.getBlock());
	}
}
