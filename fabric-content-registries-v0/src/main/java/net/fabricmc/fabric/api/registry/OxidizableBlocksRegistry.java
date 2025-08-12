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

import net.minecraft.block.Block;
import net.minecraft.block.CopperBlockSet;

import net.fabricmc.fabric.impl.content.registry.OxidizableBlocksRegistryImpl;

/**
 * Provides methods for registering oxidizable and waxable blocks.
 */
public final class OxidizableBlocksRegistry {
	private OxidizableBlocksRegistry() {
	}

	/**
	 * Registers a block pair as being able to increase and decrease oxidation.
	 *
	 * @param less the variant with less oxidation
	 * @param more the variant with more oxidation
	 */
	public static void registerOxidizableBlockPair(Block less, Block more) {
		OxidizableBlocksRegistryImpl.registerOxidizableBlockPair(less, more);
	}

	/**
	 * Registers a block pair as being able to add and remove wax.
	 *
	 * @param unwaxed the unwaxed variant
	 * @param waxed   the waxed variant
	 */
	public static void registerWaxableBlockPair(Block unwaxed, Block waxed) {
		OxidizableBlocksRegistryImpl.registerWaxableBlockPair(unwaxed, waxed);
	}

	/**
	 * Registers a {@link CopperBlockSet} and its oxidizing and waxing variants.
	 *
	 * @param blockSet the copper block set to register
	 */
	public static void registerCopperBlockSet(CopperBlockSet blockSet) {
		OxidizableBlocksRegistryImpl.registerCopperBlockSet(blockSet);
	}
}
