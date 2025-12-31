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

package net.fabricmc.fabric.api.transfer.v1.fluid;

import java.util.Collection;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import net.fabricmc.fabric.api.lookup.v1.custom.ApiProviderMap;
import net.fabricmc.fabric.impl.transfer.fluid.CauldronStorage;

/// Entrypoint to expose cauldrons to the Fluid Transfer API.
/// Empty, water and lava cauldrons are registered by default, and additional cauldrons must be registered with [#registerCauldron].
/// Contents can be queried with [#getForBlock] and [#getForFluid].
///
/// The `CauldronFluidContent` itself defines:
///
///   - The block of the cauldron.
///   - The fluid that can be accepted by the cauldron. Components are discarded when entering the cauldron.
///   - Which fluid amounts can be stored in the cauldron, and how they map to the level property of the cauldron.
///     If `levelProperty` is `null`, then `maxLevel = 1`, and there is only one level.
///     Otherwise, the levels are all the integer values between `1` and `maxLevel` (included).
///
///   - `amountPerLevel` defines how much fluid (in droplets) there is in one level of the cauldron.
///
public final class CauldronFluidContent {
	/// Block of the cauldron.
	public final Block block;
	/// Fluid stored inside the cauldron.
	public final Fluid fluid;
	/// Amount in droplets for each level of [#levelProperty].
	public final long amountPerLevel;
	/// Maximum level for [#levelProperty]. `1` if `levelProperty` is null, otherwise a number `>= 1`.
	/// The minimum level is always 1.
	public final int maxLevel;
	/// Property storing the level of the cauldron. If it's null, only one level is possible.
	@Nullable
	public final IntegerProperty levelProperty;

	private CauldronFluidContent(Block block, Fluid fluid, long amountPerLevel, int maxLevel, @Nullable IntegerProperty levelProperty) {
		this.block = block;
		this.fluid = fluid;
		this.amountPerLevel = amountPerLevel;
		this.maxLevel = maxLevel;
		this.levelProperty = levelProperty;
	}

	// Copy-on-write, identity semantics, null-checked.
	private static final ApiProviderMap<Block, CauldronFluidContent> BLOCK_TO_CAULDRON = ApiProviderMap.create();
	private static final ApiProviderMap<Fluid, CauldronFluidContent> FLUID_TO_CAULDRON = ApiProviderMap.create();

	/// Get the cauldron fluid content for a cauldron block, or `null` if none was registered (yet).
	@Nullable
	public static CauldronFluidContent getForBlock(Block block) {
		return BLOCK_TO_CAULDRON.get(block);
	}

	/// Get the cauldron fluid content for a fluid, or `null` if no cauldron was registered for that fluid (yet).
	@Nullable
	public static CauldronFluidContent getForFluid(Fluid fluid) {
		return FLUID_TO_CAULDRON.get(fluid);
	}

	/// Attempt to register a new cauldron if not already registered, allowing it to be filled and emptied through the Fluid Transfer API.
	/// In both cases, return the content of the cauldron, either the existing one, or the newly registered one.
	///
	/// @param block The block of the cauldron.
	/// @param fluid The fluid stored in this cauldron.
	/// @param amountPerLevel How much fluid is contained in one level of the cauldron, in [droplets][FluidConstants].
	/// @param levelProperty The property used by the cauldron to store its levels. `null` if the cauldron only has one level.
	public static synchronized CauldronFluidContent registerCauldron(Block block, Fluid fluid, long amountPerLevel, @Nullable IntegerProperty levelProperty) {
		CauldronFluidContent existingBlockData = BLOCK_TO_CAULDRON.get(block);

		if (existingBlockData != null) {
			return existingBlockData;
		}

		if (FLUID_TO_CAULDRON.get(fluid) != null) {
			throw new IllegalArgumentException("Fluid already has a mapping for a different block."); // TODO better message
		}

		CauldronFluidContent data;

		if (levelProperty == null) {
			data = new CauldronFluidContent(block, fluid, amountPerLevel, 1, null);
		} else {
			Collection<Integer> levels = levelProperty.getPossibleValues();

			if (levels.size() == 0) {
				throw new RuntimeException("Cauldron should have at least one possible level.");
			}

			int minLevel = Integer.MAX_VALUE;
			int maxLevel = 0;

			for (int level : levels) {
				minLevel = Math.min(minLevel, level);
				maxLevel = Math.max(maxLevel, level);
			}

			if (minLevel != 1 || maxLevel < 1) {
				throw new IllegalStateException("Minimum level should be 1, and maximum level should be >= 1.");
			}

			data = new CauldronFluidContent(block, fluid, amountPerLevel, maxLevel, levelProperty);
		}

		BLOCK_TO_CAULDRON.putIfAbsent(block, data);
		FLUID_TO_CAULDRON.putIfAbsent(fluid, data);

		FluidStorage.SIDED.registerForBlocks((level, pos, state, be, context) -> CauldronStorage.get(level, pos), block);

		return data;
	}

	/// Return the current level of the cauldron given its block state, or 0 if it's an empty cauldron.
	public int currentLevel(BlockState state) {
		if (fluid == Fluids.EMPTY) {
			return 0;
		} else if (levelProperty == null) {
			return 1;
		} else {
			return state.getValue(levelProperty);
		}
	}

	static {
		// Vanilla registrations
		CauldronFluidContent.registerCauldron(Blocks.CAULDRON, Fluids.EMPTY, FluidConstants.BUCKET, null);
		CauldronFluidContent.registerCauldron(Blocks.WATER_CAULDRON, Fluids.WATER, FluidConstants.BOTTLE, LayeredCauldronBlock.LEVEL);
		CauldronFluidContent.registerCauldron(Blocks.LAVA_CAULDRON, Fluids.LAVA, FluidConstants.BUCKET, null);
	}
}
