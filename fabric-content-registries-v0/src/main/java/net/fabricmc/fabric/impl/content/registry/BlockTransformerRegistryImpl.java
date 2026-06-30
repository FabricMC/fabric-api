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

package net.fabricmc.fabric.impl.content.registry;

import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.component.BlockTransformerMappings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.CopyPropertiesProvider;

public class BlockTransformerRegistryImpl {
	public static void registerAxe(BlockTransformer.BlockTransformData transformData) {
		BlockTransformerMappings.AXE.transforms().add(transformData);
	}

	public static void registerHoe(BlockTransformer.BlockTransformData transformData) {
		BlockTransformerMappings.HOE.transforms().add(transformData);
	}

	public static void registerShovel(BlockTransformer.BlockTransformData transformData) {
		BlockTransformerMappings.SHOVEL.transforms().add(transformData);
	}

	public static void registerStripping(BlockPredicate fromBlockPredicate, Block toBlock) {
		registerAxe(createStripping(fromBlockPredicate, toBlock));
	}

	public static void registerTilling(BlockPredicate fromBlockPredicate, Block toBlock) {
		registerHoe(createTilling(fromBlockPredicate, toBlock));
	}

	public static void registerFlattening(BlockPredicate fromBlockPredicate, Block toBlock) {
		registerShovel(createFlattening(fromBlockPredicate, toBlock));
	}

	static void registerOxidationScraping(Block fromBlock, Block toBlock) {
		registerAxe(createOxidationScraping(BlockPredicate.matchesBlocks(fromBlock), toBlock));
	}

	static void registerWaxScraping(Block fromBlock, Block toBlock) {
		registerAxe(createWaxScraping(BlockPredicate.matchesBlocks(fromBlock), toBlock));
	}

	private static BlockTransformer.BlockTransformData createStripping(BlockPredicate fromBlockPredicate, Block toBlock) {
		return BlockTransformer.BlockTransformData.builder(fromBlockPredicate, new CopyPropertiesProvider(toBlock))
				.sound(SoundEvents.AXE_STRIP)
				.build();
	}

	private static BlockTransformer.BlockTransformData createTilling(BlockPredicate fromBlockPredicate, Block toBlock) {
		return BlockTransformer.BlockTransformData.builder(
						BlockPredicate.allOf(fromBlockPredicate, BlockPredicate.matchesTag(Direction.UP, BlockTags.AIR)), toBlock
				)
				.sound(SoundEvents.HOE_TILL)
				.disallowedFaces(List.of(Direction.DOWN))
				.build();
	}

	private static BlockTransformer.BlockTransformData createFlattening(BlockPredicate fromBlockPredicate, Block toBlock) {
		return BlockTransformer.BlockTransformData.builder(
						BlockPredicate.allOf(fromBlockPredicate, BlockPredicate.matchesTag(Direction.UP, BlockTags.AIR)), toBlock
				)
				.sound(SoundEvents.SHOVEL_FLATTEN)
				.disallowedFaces(List.of(Direction.DOWN))
				.build();
	}

	private static BlockTransformer.BlockTransformData createOxidationScraping(BlockPredicate fromBlockPredicate, Block toBlock) {
		return BlockTransformer.BlockTransformData.builder(fromBlockPredicate, new CopyPropertiesProvider(toBlock))
				.sound(SoundEvents.AXE_SCRAPE)
				.particle(BlockTransformer.TransformParticle.SCRAPE)
				.build();
	}

	private static BlockTransformer.BlockTransformData createWaxScraping(BlockPredicate fromBlockPredicate, Block toBlock) {
		return BlockTransformer.BlockTransformData.builder(fromBlockPredicate, new CopyPropertiesProvider(toBlock))
				.sound(SoundEvents.AXE_WAX_OFF)
				.particle(BlockTransformer.TransformParticle.WAX_OFF)
				.build();
	}
}
