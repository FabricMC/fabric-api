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

package net.fabricmc.fabric.impl.item;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.Direction;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.component.BlockTransformers;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.CopyPropertiesProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedStateProvider;

import net.fabricmc.fabric.api.item.v1.BlockTransformerEvents;
import net.fabricmc.fabric.api.item.v1.ResourceSource;

public final class BlockTransformerHelperImpl {
	private static final Logger LOGGER = LoggerFactory.getLogger(BlockTransformerHelperImpl.class);

	private static final List<BlockTransformer.BlockTransformData> AXE_TRANSFORMATIONS = new ArrayList<>();
	private static final List<BlockTransformer.BlockTransformData> HOE_TRANSFORMATIONS = new ArrayList<>();
	private static final List<BlockTransformer.BlockTransformData> SHOVEL_TRANSFORMATIONS = new ArrayList<>();

	@Nullable
	public static BlockTransformer modify(ResourceKey<BlockTransformer> key, BlockTransformer original, ResourceSource source, RegistryOps.RegistryInfoLookup registryInfoLookup) {
		List<BlockTransformer.BlockTransformData> transforms = new ArrayList<>(original.transforms());

		if (key.equals(BlockTransformers.AXE)) {
			transforms.addAll(AXE_TRANSFORMATIONS);
		} else if (key.equals(BlockTransformers.HOE)) {
			transforms.addAll(HOE_TRANSFORMATIONS);
		} else if (key.equals(BlockTransformers.SHOVEL)) {
			transforms.addAll(SHOVEL_TRANSFORMATIONS);
		}

		BlockTransformerEvents.MODIFY.invoker().modify(key, transforms, source, registryInfoLookup);

		if (!transforms.equals(original.transforms())) {
			LOGGER.debug("Block transformer {} was modified", key.identifier());

			return new BlockTransformer(transforms);
		}

		return null;
	}

	public static void registerAxe(BlockTransformer transformer) {
		AXE_TRANSFORMATIONS.addAll(transformer.transforms());
	}

	public static void registerHoe(BlockTransformer transformer) {
		HOE_TRANSFORMATIONS.addAll(transformer.transforms());
	}

	public static void registerShovel(BlockTransformer transformer) {
		SHOVEL_TRANSFORMATIONS.addAll(transformer.transforms());
	}

	public static void registerAxe(BlockTransformer.BlockTransformData transformData) {
		AXE_TRANSFORMATIONS.add(transformData);
	}

	public static void registerHoe(BlockTransformer.BlockTransformData transformData) {
		HOE_TRANSFORMATIONS.add(transformData);
	}

	public static void registerShovel(BlockTransformer.BlockTransformData transformData) {
		SHOVEL_TRANSFORMATIONS.add(transformData);
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

	public static void registerOxidationScraping(BlockPredicate fromBlockPredicate, BlockStateProvider toBlockState) {
		registerAxe(createOxidationScraping(fromBlockPredicate, toBlockState));
	}

	public static void registerWaxScraping(BlockPredicate fromBlockPredicate, BlockStateProvider toBlockState) {
		registerAxe(createWaxScraping(fromBlockPredicate, toBlockState));
	}

	/**
	 * Maintainer's note: this should be kept equivalent to the vanilla stripping transform data created in {@link BlockTransformers#bootstrap}.
	 */
	public static BlockTransformer.BlockTransformData createStripping(BlockPredicate fromBlockPredicate, BlockStateProvider toBlockState) {
		return BlockTransformer.BlockTransformData.builder(RuleBasedStateProvider.builder()
						.ifTrueThenProvide(
								fromBlockPredicate,
								new CopyPropertiesProvider(toBlockState))
						.build()
				)
				.sound(SoundEvents.AXE_STRIP)
				.build();
	}

	/**
	 * Maintainer's note: this should be kept equivalent to the vanilla 'hoeDefault' transform data created in {@link BlockTransformers#bootstrap}.
	 */
	public static BlockTransformer.BlockTransformData createTilling(BlockPredicate fromBlockPredicate, BlockStateProvider toBlockState) {
		return BlockTransformer.BlockTransformData.builder(RuleBasedStateProvider.builder()
						.ifTrueThenProvide(
								BlockPredicate.allOf(fromBlockPredicate, BlockPredicate.matchesTag(Direction.UP, BlockTags.AIR)),
								toBlockState
						)
						.build()
				)
				.sound(SoundEvents.HOE_TILL)
				.disallowedFaces(List.of(Direction.DOWN))
				.build();
	}

	/**
	 * Maintainer's note: this should be kept equivalent to the vanilla shovel transform data created in {@link BlockTransformers#bootstrap}.
	 */
	public static BlockTransformer.BlockTransformData createFlattening(BlockPredicate fromBlockPredicate, BlockStateProvider toBlockState) {
		return BlockTransformer.BlockTransformData.builder(RuleBasedStateProvider.builder()
						.ifTrueThenProvide(
								BlockPredicate.allOf(fromBlockPredicate, BlockPredicate.matchesTag(Direction.UP, BlockTags.AIR)),
								toBlockState
						)
						.build()
				)
				.sound(SoundEvents.SHOVEL_FLATTEN)
				.disallowedFaces(List.of(Direction.DOWN))
				.build();
	}

	/**
	 * Maintainer's note: this should be kept equivalent to the vanilla oxidation scraping transform data created in {@link BlockTransformers#bootstrap}.
	 */
	public static BlockTransformer.BlockTransformData createOxidationScraping(BlockPredicate fromBlockPredicate, BlockStateProvider toBlockState) {
		return BlockTransformer.BlockTransformData.builder(RuleBasedStateProvider.builder()
						.ifTrueThenProvide(
								fromBlockPredicate,
								new CopyPropertiesProvider(toBlockState)
						)
						.build()
				)
				.sound(SoundEvents.AXE_SCRAPE)
				.particle(BlockTransformer.TransformParticle.SCRAPE)
				.build();
	}

	/**
	 * Maintainer's note: this should be kept equivalent to the vanilla wax scraping transform data created in {@link BlockTransformers#bootstrap}.
	 */
	public static BlockTransformer.BlockTransformData createWaxScraping(BlockPredicate fromBlockPredicate, BlockStateProvider toBlockState) {
		return BlockTransformer.BlockTransformData.builder(RuleBasedStateProvider.builder()
						.ifTrueThenProvide(
								fromBlockPredicate,
								new CopyPropertiesProvider(toBlockState)
						)
						.build()
				)
				.sound(SoundEvents.AXE_WAX_OFF)
				.particle(BlockTransformer.TransformParticle.WAX_OFF)
				.build();
	}

	private BlockTransformerHelperImpl() { }
}
