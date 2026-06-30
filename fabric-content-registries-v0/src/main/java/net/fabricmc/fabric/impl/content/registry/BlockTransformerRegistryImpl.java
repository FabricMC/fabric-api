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
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.CopyPropertiesProvider;

public final class BlockTransformerRegistryImpl {
	private BlockTransformerRegistryImpl() {
	}

	public static void registerAxe(BlockTransformer.BlockTransformData transformData) {
		BlockTransformerMappings.AXE.transforms().add(transformData);
	}

	public static void registerHoe(BlockTransformer.BlockTransformData transformData) {
		BlockTransformerMappings.HOE.transforms().add(transformData);
	}

	public static void registerShovel(BlockTransformer.BlockTransformData transformData) {
		BlockTransformerMappings.SHOVEL.transforms().add(transformData);
	}

	public static void registerStripping(BlockPredicate fromBlockPredicate, BlockStateProvider toBlockState) {
		registerAxe(createStripping(fromBlockPredicate, toBlockState));
	}

	public static void registerTilling(BlockPredicate fromBlockPredicate, BlockStateProvider toBlockState) {
		registerHoe(createTilling(fromBlockPredicate, toBlockState));
	}

	public static void registerFlattening(BlockPredicate fromBlockPredicate, BlockStateProvider toBlockState) {
		registerShovel(createFlattening(fromBlockPredicate, toBlockState));
	}

	static void registerOxidationScraping(BlockPredicate fromBlockPredicate, BlockStateProvider toBlockState) {
		registerAxe(createOxidationScraping(fromBlockPredicate, toBlockState));
	}

	static void registerWaxScraping(BlockPredicate fromBlockPredicate, BlockStateProvider toBlockState) {
		registerAxe(createWaxScraping(fromBlockPredicate, toBlockState));
	}

	private static BlockTransformer.BlockTransformData createStripping(BlockPredicate fromBlockPredicate, BlockStateProvider toBlockState) {
		return BlockTransformer.BlockTransformData.builder(fromBlockPredicate, new CopyPropertiesProvider(toBlockState))
				.sound(SoundEvents.AXE_STRIP)
				.build();
	}

	private static BlockTransformer.BlockTransformData createTilling(BlockPredicate fromBlockPredicate, BlockStateProvider toBlockState) {
		return BlockTransformer.BlockTransformData.builder(
						BlockPredicate.allOf(fromBlockPredicate, BlockPredicate.matchesTag(Direction.UP, BlockTags.AIR)), toBlockState
				)
				.sound(SoundEvents.HOE_TILL)
				.disallowedFaces(List.of(Direction.DOWN))
				.build();
	}

	private static BlockTransformer.BlockTransformData createFlattening(BlockPredicate fromBlockPredicate, BlockStateProvider toBlockState) {
		return BlockTransformer.BlockTransformData.builder(
						BlockPredicate.allOf(fromBlockPredicate, BlockPredicate.matchesTag(Direction.UP, BlockTags.AIR)), toBlockState
				)
				.sound(SoundEvents.SHOVEL_FLATTEN)
				.disallowedFaces(List.of(Direction.DOWN))
				.build();
	}

	private static BlockTransformer.BlockTransformData createOxidationScraping(BlockPredicate fromBlockPredicate, BlockStateProvider toBlockState) {
		return BlockTransformer.BlockTransformData.builder(fromBlockPredicate, new CopyPropertiesProvider(toBlockState))
				.sound(SoundEvents.AXE_SCRAPE)
				.particle(BlockTransformer.TransformParticle.SCRAPE)
				.build();
	}

	private static BlockTransformer.BlockTransformData createWaxScraping(BlockPredicate fromBlockPredicate, BlockStateProvider toBlockState) {
		return BlockTransformer.BlockTransformData.builder(fromBlockPredicate, new CopyPropertiesProvider(toBlockState))
				.sound(SoundEvents.AXE_WAX_OFF)
				.particle(BlockTransformer.TransformParticle.WAX_OFF)
				.build();
	}
}
