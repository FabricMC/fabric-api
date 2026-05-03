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

package net.fabricmc.fabric.test.rendering.client;

import java.util.List;

import it.unimi.dsi.fastutil.ints.IntList;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockTintsFactory;
import net.fabricmc.fabric.api.client.rendering.v1.ColorResolverRegistry;
import net.fabricmc.fabric.test.rendering.CustomColorResolverTestInit;

public class CustomColorResolverTest implements ClientModInitializer {
	public static final ColorResolver TEST_COLOR_RESOLVER = (biome, x, z) -> {
		if (biome.hasPrecipitation()) {
			return 0xFFFF00FF;
		} else {
			return 0xFFFFFF00;
		}
	};

	private static final BlockTintSource TINT_SOURCE = new BlockTintSource() {
		@Override
		public int color(BlockState state) {
			return -1;
		}

		@Override
		public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
			return level.getBlockTint(pos, TEST_COLOR_RESOLVER);
		}
	};

	private static final BlockTintsFactory TINTS_FACTORY = new BlockTintsFactory() {
		private final ThreadLocal<RandomSource> RANDOM = ThreadLocal.withInitial(() -> RandomSource.createThreadLocalInstance(42L));

		@Override
		public void collect(
				final BlockState state,
				final BlockAndTintGetter level,
				final BlockPos pos,
				final IntList tintValues) {
			tintValues.size(2);
			tintValues.set(0, ARGB.color(255, RANDOM.get().nextInt()));
			tintValues.set(1, ARGB.color(255, RANDOM.get().nextInt()));
		}
	};

	@Override
	public void onInitializeClient() {
		ColorResolverRegistry.register(TEST_COLOR_RESOLVER);
		BlockColorRegistry.register(List.of(TINT_SOURCE), CustomColorResolverTestInit.CUSTOM_COLOR_BLOCK);
		BlockColorRegistry.register(TINTS_FACTORY, CustomColorResolverTestInit.CUSTOM_COLOR_BLOCK_DYNAMIC);
	}
}
