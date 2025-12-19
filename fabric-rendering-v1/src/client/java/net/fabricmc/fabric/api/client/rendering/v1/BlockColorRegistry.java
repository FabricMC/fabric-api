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

import org.jspecify.annotations.Nullable;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.impl.client.rendering.BlockColorRegistryImpl;

public interface BlockColorRegistry<T, Color> {
	BlockColorRegistry<Block, BlockColor> BLOCK = BlockColorRegistryImpl.BLOCK;

	/**
	 * Register a block color for one or more objects.
	 *
	 * @param color The block color to register.
	 * @param objects  The objects which should be colored using this color.
	 */
	@SuppressWarnings("unchecked") // @SafeVarargs is not allowed on interface methods.
	void register(Color color, T... objects);

	/**
	 * Get a block color for the given object.
	 *
	 * <p>Please note that the underlying registry may not be fully populated or stable until the game has started,
	 * as other mods may overwrite the registry.
	 *
	 * @param object The object to acquire the color for.
	 * @return The registered mapper for this color, or {@code null} if none is registered or available.
	 */
	@Nullable
	Color get(T object);
}
