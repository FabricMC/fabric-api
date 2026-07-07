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

package net.fabricmc.fabric.api.client.rendering.v1;

import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.impl.client.rendering.BlockColorRegistryImpl;

/**
 * The registry for {@link BlockTintSource}s.
 */
public final class BlockColorRegistry {
	private BlockColorRegistry() {
	}

	/**
	 * Register a block color for one or more blocks. Overriding existing registrations is allowed.
	 *
	 * <p>Mods must use this method instead of {@link BlockColors#register(List, Block...)} during mod
	 * initialization as it runs before {@link Minecraft#getBlockColors()} is available.
	 *
	 * @param layers A list of {@link BlockTintSource}s.
	 * @param blocks The blocks which should be colored using the given color.
	 */
	public static void register(List<BlockTintSource> layers, Block... blocks) {
		BlockColorRegistryImpl.register(layers, blocks);
	}

	/**
	 * Register a block tint factory for one or more blocks. Overriding existing registration is allowed.
	 *
	 * @param factory The factory which allows dynamic tinting.
	 * @param blocks The blocks which should be colored using the given factory.
	 */
	public static void register(BlockTintsFactory factory, Block... blocks) {
		BlockColorRegistryImpl.register(factory, blocks);
	}

	/**
	 * Retrieves the current {@link BlockTintsFactory factory}, or {@code null} if no factory exists,
	 * for the given {@link BlockState block state}.
	 *
	 * @param blockState The block state to look up.
	 * @return The factory.
	 */
	public static @Nullable BlockTintsFactory getFactory(BlockState blockState) {
		return BlockColorRegistryImpl.getFactory(blockState);
	}
}
