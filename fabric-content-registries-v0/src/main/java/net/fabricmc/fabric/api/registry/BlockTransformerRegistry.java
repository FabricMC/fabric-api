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
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;

import net.fabricmc.fabric.impl.content.registry.BlockTransformerRegistryImpl;

/**
 * Allows for registration of additional block transform data to axes, shovels, and hoes.
 *
 * <p>Also contains various shortcut methods for standard behaviors.
 */
public class BlockTransformerRegistry {
	/**
	 * Registers block transform data that will be added to axes.
	 * <br>Use {@link BlockTransformerRegistry#registerStripping} instead to register a basic transformer for stripping, like logs into stripped logs.
	 * @param transformData The transform data to register.
	 */
	public static void registerAxe(BlockTransformer.BlockTransformData transformData) {
		BlockTransformerRegistryImpl.registerAxe(transformData);
	}

	/**
	 * Registers block transform data that will be added to hoes.
	 * <br>Use {@link BlockTransformerRegistry#registerTilling} instead to register a basic transformer for tilling, like dirt into farmland.
	 * @param transformData The transform data to register.
	 */
	public static void registerHoe(BlockTransformer.BlockTransformData transformData) {
		BlockTransformerRegistryImpl.registerHoe(transformData);
	}

	/**
	 * Registers block transform data that will be added to shovels.
	 * <br>Use {@link BlockTransformerRegistry#registerFlattening} instead to register a basic transformer for flattening, like dirt into paths.
	 * @param transformData The transform data to register.
	 */
	public static void registerShovel(BlockTransformer.BlockTransformData transformData) {
		BlockTransformerRegistryImpl.registerShovel(transformData);
	}

	/**
	 * Registers a basic transformer for stripping, like logs into stripped logs.
	 * <br>Use {@link BlockTransformerRegistry#registerAxe} instead to register any more complex transformers for axes.
	 * @see #registerStripping(Block, Block)
	 * @see #registerStripping(Block[], Block)
	 * @see #registerStripping(TagKey, Block)
	 * @param fromBlockPredicate A predicate for which blocks can be stripped.
	 * @param toBlock The block which results from the stripping.
	 */
	public static void registerStripping(BlockPredicate fromBlockPredicate, Block toBlock) {
		BlockTransformerRegistryImpl.registerStripping(fromBlockPredicate, toBlock);
	}

	/**
	 * Registers a basic transformer for tilling, like dirt into farmland.
	 * <br>Use {@link BlockTransformerRegistry#registerHoe} instead to register any more complex transformers for hoes.
	 * @see #registerTilling(Block, Block)
	 * @see #registerTilling(Block[], Block)
	 * @see #registerTilling(TagKey, Block)
	 * @param fromBlockPredicate A predicate for which blocks can be tilled.
	 * @param toBlock The block which results from the tilling.
	 */
	public static void registerTilling(BlockPredicate fromBlockPredicate, Block toBlock) {
		BlockTransformerRegistryImpl.registerTilling(fromBlockPredicate, toBlock);
	}

	/**
	 * Registers a basic transformer for flattening, like dirt into paths.
	 * <br>Use {@link BlockTransformerRegistry#registerShovel} instead to register any more complex transformers for shovels.
	 * @see #registerFlattening(Block, Block)
	 * @see #registerFlattening(Block[], Block)
	 * @see #registerFlattening(TagKey, Block)
	 * @param fromBlockPredicate A predicate for which blocks can be flattened.
	 * @param toBlock The block which results from the flattening.
	 */
	public static void registerFlattening(BlockPredicate fromBlockPredicate, Block toBlock) {
		BlockTransformerRegistryImpl.registerFlattening(fromBlockPredicate, toBlock);
	}

	/**
	 * Registers a basic transformer for stripping, like logs into stripped logs.
	 * <br>Use {@link BlockTransformerRegistry#registerAxe} instead to register any more complex transformers for axes.
	 * @see #registerStripping(BlockPredicate, Block)
	 * @see #registerStripping(Block[], Block)
	 * @see #registerStripping(TagKey, Block)
	 * @param fromBlock The block which can can be stripped.
	 * @param toBlock The block which results from the stripping.
	 */
	public static void registerStripping(Block fromBlock, Block toBlock) {
		registerStripping(BlockPredicate.matchesBlocks(fromBlock), toBlock);
	}

	/**
	 * Registers a basic transformer for tilling, like dirt into farmland.
	 * <br>Use {@link BlockTransformerRegistry#registerHoe} instead to register any more complex transformers for hoes.
	 * @see #registerTilling(BlockPredicate, Block)
	 * @see #registerTilling(Block[], Block)
	 * @see #registerTilling(TagKey, Block)
	 * @param fromBlock The block which can be tilled.
	 * @param toBlock The block which results from the tilling.
	 */
	public static void registerTilling(Block fromBlock, Block toBlock) {
		registerTilling(BlockPredicate.matchesBlocks(fromBlock), toBlock);
	}

	/**
	 * Registers a basic transformer for flattening, like dirt into paths.
	 * <br>Use {@link BlockTransformerRegistry#registerShovel} instead to register any more complex transformers for shovels.
	 * @see #registerFlattening(BlockPredicate, Block)
	 * @see #registerFlattening(Block[], Block)
	 * @see #registerFlattening(TagKey, Block)
	 * @param fromBlock The block which can be flattened.
	 * @param toBlock The block which results from the flattening.
	 */
	public static void registerFlattening(Block fromBlock, Block toBlock) {
		registerFlattening(BlockPredicate.matchesBlocks(fromBlock), toBlock);
	}

	/**
	 * Registers a basic transformer for stripping, like logs into stripped logs.
	 * <br>Use {@link BlockTransformerRegistry#registerAxe} instead to register any more complex transformers for axes.
	 * @see #registerStripping(BlockPredicate, Block)
	 * @see #registerStripping(Block, Block)
	 * @see #registerStripping(TagKey, Block)
	 * @param fromBlocks The blocks which can can be stripped.
	 * @param toBlock The block which results from the stripping.
	 */
	public static void registerStripping(Block[] fromBlocks, Block toBlock) {
		registerStripping(BlockPredicate.matchesBlocks(fromBlocks), toBlock);
	}

	/**
	 * Registers a basic transformer for tilling, like dirt into farmland.
	 * <br>Use {@link BlockTransformerRegistry#registerHoe} instead to register any more complex transformers for hoes.
	 * @see #registerTilling(BlockPredicate, Block)
	 * @see #registerTilling(Block, Block)
	 * @see #registerTilling(TagKey, Block)
	 * @param fromBlocks The blocks which can be tilled.
	 * @param toBlock The block which results from the tilling.
	 */
	public static void registerTilling(Block[] fromBlocks, Block toBlock) {
		registerTilling(BlockPredicate.matchesBlocks(fromBlocks), toBlock);
	}

	/**
	 * Registers a basic transformer for flattening, like dirt into paths.
	 * <br>Use {@link BlockTransformerRegistry#registerShovel} instead to register any more complex transformers for shovels.
	 * @see #registerFlattening(BlockPredicate, Block)
	 * @see #registerFlattening(Block, Block)
	 * @see #registerFlattening(TagKey, Block)
	 * @param fromBlocks The blocks which can be flattened.
	 * @param toBlock The block which results from the flattening.
	 */
	public static void registerFlattening(Block[] fromBlocks, Block toBlock) {
		registerFlattening(BlockPredicate.matchesBlocks(fromBlocks), toBlock);
	}

	/**
	 * Registers a basic transformer for stripping, like logs into stripped logs.
	 * <br>Use {@link BlockTransformerRegistry#registerAxe} instead to register any more complex transformers for axes.
	 * @see #registerStripping(BlockPredicate, Block)
	 * @see #registerStripping(Block, Block)
	 * @see #registerStripping(Block[], Block)
	 * @param fromBlocks The blocks which can can be stripped.
	 * @param toBlock The block which results from the stripping.
	 */
	public static void registerStripping(TagKey<Block> fromBlocks, Block toBlock) {
		registerStripping(BlockPredicate.matchesTag(fromBlocks), toBlock);
	}

	/**
	 * Registers a basic transformer for tilling, like dirt into farmland.
	 * <br>Use {@link BlockTransformerRegistry#registerHoe} instead to register any more complex transformers for hoes.
	 * @see #registerTilling(BlockPredicate, Block)
	 * @see #registerTilling(Block, Block)
	 * @see #registerTilling(Block[], Block)
	 * @param fromBlocks The blocks which can be tilled.
	 * @param toBlock The block which results from the tilling.
	 */
	public static void registerTilling(TagKey<Block> fromBlocks, Block toBlock) {
		registerTilling(BlockPredicate.matchesTag(fromBlocks), toBlock);
	}

	/**
	 * Registers a basic transformer for flattening, like dirt into paths.
	 * <br>Use {@link BlockTransformerRegistry#registerShovel} instead to register any more complex transformers for shovels.
	 * @see #registerFlattening(BlockPredicate, Block)
	 * @see #registerFlattening(Block, Block)
	 * @see #registerFlattening(Block[], Block)
	 * @param fromBlocks The blocks which can be flattened.
	 * @param toBlock The block which results from the flattening.
	 */
	public static void registerFlattening(TagKey<Block> fromBlocks, Block toBlock) {
		registerFlattening(BlockPredicate.matchesTag(fromBlocks), toBlock);
	}
}
