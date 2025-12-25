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

package net.fabricmc.fabric.api.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopperBlocks;

import net.fabricmc.fabric.impl.content.registry.OxidizableBlocksRegistryImpl;

/**
 * Provides methods for registering oxidizable and waxable blocks.
 */
public final class OxidizableBlocksRegistry {
	private OxidizableBlocksRegistry() {
	}

	/**
	 * Registers the next oxidization stage from one block to the other.
	 *
	 * @param from the variant with less oxidation
	 * @param to the variant with more oxidation
	 */
	public static void registerNextStage(Block from, Block to) {
		OxidizableBlocksRegistryImpl.registerNextStage(from, to);
	}

	/**
	 * Registers a block pair as being able to add and remove wax.
	 *
	 * @param unwaxed the unwaxed variant
	 * @param waxed   the waxed variant
	 */
	public static void registerWaxable(Block unwaxed, Block waxed) {
		OxidizableBlocksRegistryImpl.registerWaxable(unwaxed, waxed);
	}

	/**
	 * Registers a {@link WeatheringCopperBlocks} and its oxidizing and waxing variants.
	 *
	 * @param copperBlocks the {@code WeatheringCopperBlocks} to register
	 */
	public static void registerWeatheringCopperBlocks(WeatheringCopperBlocks copperBlocks) {
		OxidizableBlocksRegistryImpl.registerWeatheringCopperBlocks(copperBlocks);
	}
}
