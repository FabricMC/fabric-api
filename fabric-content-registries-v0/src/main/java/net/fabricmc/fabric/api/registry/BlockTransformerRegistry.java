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

import net.minecraft.core.component.BlockTransformer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import net.fabricmc.fabric.impl.content.registry.BlockTransformerRegistryImpl;

/**
 * Allows for registration of additional block transform data to axes, shovels, and hoes.
 *
 * <p>Also contains various shortcut methods for standard behaviors.
 */
public final class BlockTransformerRegistry {
	private BlockTransformerRegistry() {
	}

	/**
	 * Registers block transform data that will be added to axes.
	 * <br>Use {@link BlockTransformerRegistry#registerStripping} instead to register standard block transform data for stripping with an axe, like logs into stripped logs.
	 * @param transformData The transform data to register.
	 */
	public static void registerAxe(BlockTransformer.BlockTransformData transformData) {
		BlockTransformerRegistryImpl.registerAxe(transformData);
	}

	/**
	 * Registers block transform data that will be added to hoes.
	 * <br>Use {@link BlockTransformerRegistry#registerTilling} instead to register standard block transform data for tilling with a hoe, like dirt into farmland.
	 * @param transformData The transform data to register.
	 */
	public static void registerHoe(BlockTransformer.BlockTransformData transformData) {
		BlockTransformerRegistryImpl.registerHoe(transformData);
	}

	/**
	 * Registers block transform data that will be added to shovels.
	 * <br>Use {@link BlockTransformerRegistry#registerFlattening} instead to register standard block transform data for flattening with a shovel, like dirt into paths.
	 * @param transformData The transform data to register.
	 */
	public static void registerShovel(BlockTransformer.BlockTransformData transformData) {
		BlockTransformerRegistryImpl.registerShovel(transformData);
	}

	/**
	 * Registers standard block transform data for stripping with an axe, like logs into stripped logs.
	 * <br>Specifically, copies any applicable block state properties to the result, and plays the stripping sound.
	 * <br>Use {@link BlockTransformerRegistry#registerAxe} instead to register any more complex or custom block transform data for axes.
	 * @param fromBlockPredicate A predicate for which blocks can be stripped.
	 * @param toBlockState A provider of the block state which results from the stripping.
	 */
	public static void registerStripping(BlockPredicate fromBlockPredicate, BlockStateProvider toBlockState) {
		BlockTransformerRegistryImpl.registerStripping(fromBlockPredicate, toBlockState);
	}

	/**
	 * Registers standard block transform data for tilling with a hoe, like dirt into farmland.
	 * <br>Specifically, requires the block above the target to be air, disallows the interaction on the bottom face of the block, and plays the tilling sound.
	 * <br>Use {@link BlockTransformerRegistry#registerHoe} instead to register any more complex or custom block transform data for hoes.
	 * @param fromBlockPredicate A predicate for which blocks can be tilled.
	 * @param toBlockState A provider of the block state which results from the tilling.
	 */
	public static void registerTilling(BlockPredicate fromBlockPredicate, BlockStateProvider toBlockState) {
		BlockTransformerRegistryImpl.registerTilling(fromBlockPredicate, toBlockState);
	}

	/**
	 * Registers standard block transform data for flattening with a shovel, like dirt into paths.
	 * <br>Specifically, requires the block above the target to be air, disallows the interaction on the bottom face of the block, and plays the flattening sound.
	 * <br>Use {@link BlockTransformerRegistry#registerShovel} instead to register any more complex or custom block transform data for shovels.
	 * @param fromBlockPredicate A predicate for which blocks can be flattened.
	 * @param toBlockState A provider of the block state which results from the flattening.
	 */
	public static void registerFlattening(BlockPredicate fromBlockPredicate, BlockStateProvider toBlockState) {
		BlockTransformerRegistryImpl.registerFlattening(fromBlockPredicate, toBlockState);
	}

	/**
	 * Registers standard block transform data for stripping with an axe, like logs into stripped logs.
	 * <br>Specifically, copies any applicable block state properties to the result, and plays the stripping sound.
	 * <br>Use {@link BlockTransformerRegistry#registerAxe} instead to register any more complex or custom block transform data for axes.
	 * @param fromBlock The block which can can be stripped.
	 * @param toBlockState A provider of the block state which results from the stripping.
	 */
	public static void registerStripping(Block fromBlock, BlockStateProvider toBlockState) {
		registerStripping(BlockPredicate.matchesBlocks(fromBlock), toBlockState);
	}

	/**
	 * Registers standard block transform data for tilling with a hoe, like dirt into farmland.
	 * <br>Specifically, requires the block above the target to be air, disallows the interaction on the bottom face of the block, and plays the tilling sound.
	 * <br>Use {@link BlockTransformerRegistry#registerHoe} instead to register any more complex or custom block transform data for hoes.
	 * @param fromBlock The block which can be tilled.
	 * @param toBlockState A provider of the block state which results from the tilling.
	 */
	public static void registerTilling(Block fromBlock, BlockStateProvider toBlockState) {
		registerTilling(BlockPredicate.matchesBlocks(fromBlock), toBlockState);
	}

	/**
	 * Registers standard block transform data for flattening with a shovel, like dirt into paths.
	 * <br>Specifically, requires the block above the target to be air, disallows the interaction on the bottom face of the block, and plays the flattening sound.
	 * <br>Use {@link BlockTransformerRegistry#registerShovel} instead to register any more complex or custom block transform data for shovels.
	 * @param fromBlock The block which can be flattened.
	 * @param toBlockState A provider of the block state which results from the flattening.
	 */
	public static void registerFlattening(Block fromBlock, BlockStateProvider toBlockState) {
		registerFlattening(BlockPredicate.matchesBlocks(fromBlock), toBlockState);
	}

	/**
	 * Registers standard block transform data for stripping with an axe, like logs into stripped logs.
	 * <br>Specifically, copies any applicable block state properties to the result, and plays the stripping sound.
	 * <br>Use {@link BlockTransformerRegistry#registerAxe} instead to register any more complex or custom block transform data for axes.
	 * @param fromBlocks The blocks which can can be stripped.
	 * @param toBlockState A provider of the block state which results from the stripping.
	 */
	public static void registerStripping(Block[] fromBlocks, BlockStateProvider toBlockState) {
		registerStripping(BlockPredicate.matchesBlocks(fromBlocks), toBlockState);
	}

	/**
	 * Registers standard block transform data for tilling with a hoe, like dirt into farmland.
	 * <br>Specifically, requires the block above the target to be air, disallows the interaction on the bottom face of the block, and plays the tilling sound.
	 * <br>Use {@link BlockTransformerRegistry#registerHoe} instead to register any more complex or custom block transform data for hoes.
	 * @param fromBlocks The blocks which can be tilled.
	 * @param toBlockState A provider of the block state which results from the tilling.
	 */
	public static void registerTilling(Block[] fromBlocks, BlockStateProvider toBlockState) {
		registerTilling(BlockPredicate.matchesBlocks(fromBlocks), toBlockState);
	}

	/**
	 * Registers standard block transform data for flattening with a shovel, like dirt into paths.
	 * <br>Specifically, requires the block above the target to be air, disallows the interaction on the bottom face of the block, and plays the flattening sound.
	 * <br>Use {@link BlockTransformerRegistry#registerShovel} instead to register any more complex or custom block transform data for shovels.
	 * @param fromBlocks The blocks which can be flattened.
	 * @param toBlockState A provider of the block state which results from the flattening.
	 */
	public static void registerFlattening(Block[] fromBlocks, BlockStateProvider toBlockState) {
		registerFlattening(BlockPredicate.matchesBlocks(fromBlocks), toBlockState);
	}

	/**
	 * Registers standard block transform data for stripping with an axe, like logs into stripped logs.
	 * <br>Specifically, copies any applicable block state properties to the result, and plays the stripping sound.
	 * <br>Use {@link BlockTransformerRegistry#registerAxe} instead to register any more complex or custom block transform data for axes.
	 * @param fromBlocks The blocks which can can be stripped.
	 * @param toBlockState A provider of the block state which results from the stripping.
	 */
	public static void registerStripping(TagKey<Block> fromBlocks, BlockStateProvider toBlockState) {
		registerStripping(BlockPredicate.matchesTag(fromBlocks), toBlockState);
	}

	/**
	 * Registers standard block transform data for tilling with a hoe, like dirt into farmland.
	 * <br>Specifically, requires the block above the target to be air, disallows the interaction on the bottom face of the block, and plays the tilling sound.
	 * <br>Use {@link BlockTransformerRegistry#registerHoe} instead to register any more complex or custom block transform data for hoes.
	 * @param fromBlocks The blocks which can be tilled.
	 * @param toBlockState A provider of the block state which results from the tilling.
	 */
	public static void registerTilling(TagKey<Block> fromBlocks, BlockStateProvider toBlockState) {
		registerTilling(BlockPredicate.matchesTag(fromBlocks), toBlockState);
	}

	/**
	 * Registers standard block transform data for flattening with a shovel, like dirt into paths.
	 * <br>Specifically, requires the block above the target to be air, disallows the interaction on the bottom face of the block, and plays the flattening sound.
	 * <br>Use {@link BlockTransformerRegistry#registerShovel} instead to register any more complex or custom block transform data for shovels.
	 * @param fromBlocks The blocks which can be flattened.
	 * @param toBlockState A provider of the block state which results from the flattening.
	 */
	public static void registerFlattening(TagKey<Block> fromBlocks, BlockStateProvider toBlockState) {
		registerFlattening(BlockPredicate.matchesTag(fromBlocks), toBlockState);
	}

	/**
	 * Registers standard block transform data for stripping with an axe, like logs into stripped logs.
	 * <br>Specifically, copies any applicable block state properties to the result, and plays the stripping sound.
	 * <br>Use {@link BlockTransformerRegistry#registerAxe} instead to register any more complex or custom block transform data for axes.
	 * @param fromBlockPredicate A predicate for which blocks can be stripped.
	 * @param toBlockState The block state which results from the stripping.
	 */
	public static void registerStripping(BlockPredicate fromBlockPredicate, BlockState toBlockState) {
		registerStripping(fromBlockPredicate, BlockStateProvider.simple(toBlockState));
	}

	/**
	 * Registers standard block transform data for tilling with a hoe, like dirt into farmland.
	 * <br>Specifically, requires the block above the target to be air, disallows the interaction on the bottom face of the block, and plays the tilling sound.
	 * <br>Use {@link BlockTransformerRegistry#registerHoe} instead to register any more complex or custom block transform data for hoes.
	 * @param fromBlockPredicate A predicate for which blocks can be tilled.
	 * @param toBlockState The block state which results from the tilling.
	 */
	public static void registerTilling(BlockPredicate fromBlockPredicate, BlockState toBlockState) {
		registerTilling(fromBlockPredicate, BlockStateProvider.simple(toBlockState));
	}

	/**
	 * Registers standard block transform data for flattening with a shovel, like dirt into paths.
	 * <br>Specifically, requires the block above the target to be air, disallows the interaction on the bottom face of the block, and plays the flattening sound.
	 * <br>Use {@link BlockTransformerRegistry#registerShovel} instead to register any more complex or custom block transform data for shovels.
	 * @param fromBlockPredicate A predicate for which blocks can be flattened.
	 * @param toBlockState The block state which results from the flattening.
	 */
	public static void registerFlattening(BlockPredicate fromBlockPredicate, BlockState toBlockState) {
		registerFlattening(fromBlockPredicate, BlockStateProvider.simple(toBlockState));
	}

	/**
	 * Registers standard block transform data for stripping with an axe, like logs into stripped logs.
	 * <br>Specifically, copies any applicable block state properties to the result, and plays the stripping sound.
	 * <br>Use {@link BlockTransformerRegistry#registerAxe} instead to register any more complex or custom block transform data for axes.
	 * @param fromBlock The block state which can can be stripped.
	 * @param toBlockState The block state which results from the stripping.
	 */
	public static void registerStripping(Block fromBlock, BlockState toBlockState) {
		registerStripping(BlockPredicate.matchesBlocks(fromBlock), BlockStateProvider.simple(toBlockState));
	}

	/**
	 * Registers standard block transform data for tilling with a hoe, like dirt into farmland.
	 * <br>Specifically, requires the block above the target to be air, disallows the interaction on the bottom face of the block, and plays the tilling sound.
	 * <br>Use {@link BlockTransformerRegistry#registerHoe} instead to register any more complex or custom block transform data for hoes.
	 * @param fromBlock The block state which can be tilled.
	 * @param toBlockState The block state which results from the tilling.
	 */
	public static void registerTilling(Block fromBlock, BlockState toBlockState) {
		registerTilling(BlockPredicate.matchesBlocks(fromBlock), BlockStateProvider.simple(toBlockState));
	}

	/**
	 * Registers standard block transform data for flattening with a shovel, like dirt into paths.
	 * <br>Specifically, requires the block above the target to be air, disallows the interaction on the bottom face of the block, and plays the flattening sound.
	 * <br>Use {@link BlockTransformerRegistry#registerShovel} instead to register any more complex or custom block transform data for shovels.
	 * @param fromBlock The block state which can be flattened.
	 * @param toBlockState The block state which results from the flattening.
	 */
	public static void registerFlattening(Block fromBlock, BlockState toBlockState) {
		registerFlattening(BlockPredicate.matchesBlocks(fromBlock), BlockStateProvider.simple(toBlockState));
	}

	/**
	 * Registers standard block transform data for stripping with an axe, like logs into stripped logs.
	 * <br>Specifically, copies any applicable block state properties to the result, and plays the stripping sound.
	 * <br>Use {@link BlockTransformerRegistry#registerAxe} instead to register any more complex or custom block transform data for axes.
	 * @param fromBlocks The blocks which can can be stripped.
	 * @param toBlockState The block state which results from the stripping.
	 */
	public static void registerStripping(Block[] fromBlocks, BlockState toBlockState) {
		registerStripping(BlockPredicate.matchesBlocks(fromBlocks), BlockStateProvider.simple(toBlockState));
	}

	/**
	 * Registers standard block transform data for tilling with a hoe, like dirt into farmland.
	 * <br>Specifically, requires the block above the target to be air, disallows the interaction on the bottom face of the block, and plays the tilling sound.
	 * <br>Use {@link BlockTransformerRegistry#registerHoe} instead to register any more complex or custom block transform data for hoes.
	 * @param fromBlocks The blocks which can be tilled.
	 * @param toBlockState The block state which results from the tilling.
	 */
	public static void registerTilling(Block[] fromBlocks, BlockState toBlockState) {
		registerTilling(BlockPredicate.matchesBlocks(fromBlocks), BlockStateProvider.simple(toBlockState));
	}

	/**
	 * Registers standard block transform data for flattening with a shovel, like dirt into paths.
	 * <br>Specifically, requires the block above the target to be air, disallows the interaction on the bottom face of the block, and plays the flattening sound.
	 * <br>Use {@link BlockTransformerRegistry#registerShovel} instead to register any more complex or custom block transform data for shovels.
	 * @param fromBlocks The blocks which can be flattened.
	 * @param toBlockState The block state which results from the flattening.
	 */
	public static void registerFlattening(Block[] fromBlocks, BlockState toBlockState) {
		registerFlattening(BlockPredicate.matchesBlocks(fromBlocks), BlockStateProvider.simple(toBlockState));
	}

	/**
	 * Registers standard block transform data for stripping with an axe, like logs into stripped logs.
	 * <br>Specifically, copies any applicable block state properties to the result, and plays the stripping sound.
	 * <br>Use {@link BlockTransformerRegistry#registerAxe} instead to register any more complex or custom block transform data for axes.
	 * @param fromBlocks The blocks which can can be stripped.
	 * @param toBlockState The block state which results from the stripping.
	 */
	public static void registerStripping(TagKey<Block> fromBlocks, BlockState toBlockState) {
		registerStripping(BlockPredicate.matchesTag(fromBlocks), BlockStateProvider.simple(toBlockState));
	}

	/**
	 * Registers standard block transform data for tilling with a hoe, like dirt into farmland.
	 * <br>Specifically, requires the block above the target to be air, disallows the interaction on the bottom face of the block, and plays the tilling sound.
	 * <br>Use {@link BlockTransformerRegistry#registerHoe} instead to register any more complex or custom block transform data for hoes.
	 * @param fromBlocks The blocks which can be tilled.
	 * @param toBlockState The block state which results from the tilling.
	 */
	public static void registerTilling(TagKey<Block> fromBlocks, BlockState toBlockState) {
		registerTilling(BlockPredicate.matchesTag(fromBlocks), BlockStateProvider.simple(toBlockState));
	}

	/**
	 * Registers standard block transform data for flattening with a shovel, like dirt into paths.
	 * <br>Specifically, requires the block above the target to be air, disallows the interaction on the bottom face of the block, and plays the flattening sound.
	 * <br>Use {@link BlockTransformerRegistry#registerShovel} instead to register any more complex or custom block transform data for shovels.
	 * @param fromBlocks The blocks which can be flattened.
	 * @param toBlockState The block state which results from the flattening.
	 */
	public static void registerFlattening(TagKey<Block> fromBlocks, BlockState toBlockState) {
		registerFlattening(BlockPredicate.matchesTag(fromBlocks), BlockStateProvider.simple(toBlockState));
	}

	/**
	 * Registers standard block transform data for stripping with an axe, like logs into stripped logs.
	 * <br>Specifically, copies any applicable block state properties to the result, and plays the stripping sound.
	 * <br>Use {@link BlockTransformerRegistry#registerAxe} instead to register any more complex or custom block transform data for axes.
	 * @param fromBlockPredicate A predicate for which blocks can be stripped.
	 * @param toBlock The block which results from the stripping.
	 */
	public static void registerStripping(BlockPredicate fromBlockPredicate, Block toBlock) {
		registerStripping(fromBlockPredicate, BlockStateProvider.simple(toBlock));
	}

	/**
	 * Registers standard block transform data for tilling with a hoe, like dirt into farmland.
	 * <br>Specifically, requires the block above the target to be air, disallows the interaction on the bottom face of the block, and plays the tilling sound.
	 * <br>Use {@link BlockTransformerRegistry#registerHoe} instead to register any more complex or custom block transform data for hoes.
	 * @param fromBlockPredicate A predicate for which blocks can be tilled.
	 * @param toBlock The block which results from the tilling.
	 */
	public static void registerTilling(BlockPredicate fromBlockPredicate, Block toBlock) {
		registerTilling(fromBlockPredicate, BlockStateProvider.simple(toBlock));
	}

	/**
	 * Registers standard block transform data for flattening with a shovel, like dirt into paths.
	 * <br>Specifically, requires the block above the target to be air, disallows the interaction on the bottom face of the block, and plays the flattening sound.
	 * <br>Use {@link BlockTransformerRegistry#registerShovel} instead to register any more complex or custom block transform data for shovels.
	 * @param fromBlockPredicate A predicate for which blocks can be flattened.
	 * @param toBlock The block which results from the flattening.
	 */
	public static void registerFlattening(BlockPredicate fromBlockPredicate, Block toBlock) {
		registerFlattening(fromBlockPredicate, BlockStateProvider.simple(toBlock));
	}

	/**
	 * Registers standard block transform data for stripping with an axe, like logs into stripped logs.
	 * <br>Specifically, copies any applicable block state properties to the result, and plays the stripping sound.
	 * <br>Use {@link BlockTransformerRegistry#registerAxe} instead to register any more complex or custom block transform data for axes.
	 * @param fromBlock The block which can can be stripped.
	 * @param toBlock The block which results from the stripping.
	 */
	public static void registerStripping(Block fromBlock, Block toBlock) {
		registerStripping(BlockPredicate.matchesBlocks(fromBlock), BlockStateProvider.simple(toBlock));
	}

	/**
	 * Registers standard block transform data for tilling with a hoe, like dirt into farmland.
	 * <br>Specifically, requires the block above the target to be air, disallows the interaction on the bottom face of the block, and plays the tilling sound.
	 * <br>Use {@link BlockTransformerRegistry#registerHoe} instead to register any more complex or custom block transform data for hoes.
	 * @param fromBlock The block which can be tilled.
	 * @param toBlock The block which results from the tilling.
	 */
	public static void registerTilling(Block fromBlock, Block toBlock) {
		registerTilling(BlockPredicate.matchesBlocks(fromBlock), BlockStateProvider.simple(toBlock));
	}

	/**
	 * Registers standard block transform data for flattening with a shovel, like dirt into paths.
	 * <br>Specifically, requires the block above the target to be air, disallows the interaction on the bottom face of the block, and plays the flattening sound.
	 * <br>Use {@link BlockTransformerRegistry#registerShovel} instead to register any more complex or custom block transform data for shovels.
	 * @param fromBlock The block which can be flattened.
	 * @param toBlock The block which results from the flattening.
	 */
	public static void registerFlattening(Block fromBlock, Block toBlock) {
		registerFlattening(BlockPredicate.matchesBlocks(fromBlock), BlockStateProvider.simple(toBlock));
	}

	/**
	 * Registers standard block transform data for stripping with an axe, like logs into stripped logs.
	 * <br>Specifically, copies any applicable block state properties to the result, and plays the stripping sound.
	 * <br>Use {@link BlockTransformerRegistry#registerAxe} instead to register any more complex or custom block transform data for axes.
	 * @param fromBlocks The blocks which can can be stripped.
	 * @param toBlock The block which results from the stripping.
	 */
	public static void registerStripping(Block[] fromBlocks, Block toBlock) {
		registerStripping(BlockPredicate.matchesBlocks(fromBlocks), BlockStateProvider.simple(toBlock));
	}

	/**
	 * Registers standard block transform data for tilling with a hoe, like dirt into farmland.
	 * <br>Specifically, requires the block above the target to be air, disallows the interaction on the bottom face of the block, and plays the tilling sound.
	 * <br>Use {@link BlockTransformerRegistry#registerHoe} instead to register any more complex or custom block transform data for hoes.
	 * @param fromBlocks The blocks which can be tilled.
	 * @param toBlock The block which results from the tilling.
	 */
	public static void registerTilling(Block[] fromBlocks, Block toBlock) {
		registerTilling(BlockPredicate.matchesBlocks(fromBlocks), BlockStateProvider.simple(toBlock));
	}

	/**
	 * Registers standard block transform data for flattening with a shovel, like dirt into paths.
	 * <br>Specifically, requires the block above the target to be air, disallows the interaction on the bottom face of the block, and plays the flattening sound.
	 * <br>Use {@link BlockTransformerRegistry#registerShovel} instead to register any more complex or custom block transform data for shovels.
	 * @param fromBlocks The blocks which can be flattened.
	 * @param toBlock The block which results from the flattening.
	 */
	public static void registerFlattening(Block[] fromBlocks, Block toBlock) {
		registerFlattening(BlockPredicate.matchesBlocks(fromBlocks), BlockStateProvider.simple(toBlock));
	}

	/**
	 * Registers standard block transform data for stripping with an axe, like logs into stripped logs.
	 * <br>Specifically, copies any applicable block state properties to the result, and plays the stripping sound.
	 * <br>Use {@link BlockTransformerRegistry#registerAxe} instead to register any more complex or custom block transform data for axes.
	 * @param fromBlocks The blocks which can can be stripped.
	 * @param toBlock The block which results from the stripping.
	 */
	public static void registerStripping(TagKey<Block> fromBlocks, Block toBlock) {
		registerStripping(BlockPredicate.matchesTag(fromBlocks), BlockStateProvider.simple(toBlock));
	}

	/**
	 * Registers standard block transform data for tilling with a hoe, like dirt into farmland.
	 * <br>Specifically, requires the block above the target to be air, disallows the interaction on the bottom face of the block, and plays the tilling sound.
	 * <br>Use {@link BlockTransformerRegistry#registerHoe} instead to register any more complex or custom block transform data for hoes.
	 * @param fromBlocks The blocks which can be tilled.
	 * @param toBlock The block which results from the tilling.
	 */
	public static void registerTilling(TagKey<Block> fromBlocks, Block toBlock) {
		registerTilling(BlockPredicate.matchesTag(fromBlocks), BlockStateProvider.simple(toBlock));
	}

	/**
	 * Registers standard block transform data for flattening with a shovel, like dirt into paths.
	 * <br>Specifically, requires the block above the target to be air, disallows the interaction on the bottom face of the block, and plays the flattening sound.
	 * <br>Use {@link BlockTransformerRegistry#registerShovel} instead to register any more complex or custom block transform data for shovels.
	 * @param fromBlocks The blocks which can be flattened.
	 * @param toBlock The block which results from the flattening.
	 */
	public static void registerFlattening(TagKey<Block> fromBlocks, Block toBlock) {
		registerFlattening(BlockPredicate.matchesTag(fromBlocks), BlockStateProvider.simple(toBlock));
	}
}
