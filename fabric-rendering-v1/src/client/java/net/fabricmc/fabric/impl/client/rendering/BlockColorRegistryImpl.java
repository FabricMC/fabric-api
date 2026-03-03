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

public final class BlockColorRegistryImpl {
	@Nullable
	private static BlockColors blockColors;
	@Nullable
	private static Map<Block, List<BlockTintSource>> map = new IdentityHashMap<>();

	public static void initialize(BlockColors blockColors) {
		if (BlockColorRegistryImpl.blockColors != null) {
			return;
		}

		BlockColorRegistryImpl.blockColors = blockColors;

		map.forEach((block, color) -> blockColors.register(color, block));
		map = null;
	}

	public static void register(List<BlockTintSource> layers, Block... blocks) {
		if (blockColors != null) {
			blockColors.register(layers, blocks);
		} else {
			for (Block block : blocks) {
				map.put(block, layers);
			}
		}
	}
}
