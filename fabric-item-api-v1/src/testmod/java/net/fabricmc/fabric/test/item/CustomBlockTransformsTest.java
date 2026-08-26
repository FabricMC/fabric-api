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

package net.fabricmc.fabric.test.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.BlockTransformerEvents;
import net.fabricmc.fabric.api.item.v1.BlockTransformerHelper;

public class CustomBlockTransformsTest implements ModInitializer {
	public static final ResourceKey<BlockTransformer> PICKAXE = ResourceKey.create(
			Registries.BLOCK_TRANSFORMER,
			Identifier.fromNamespaceAndPath("fabric-item-api-v1-testmod", "pickaxe")
	);

	@Override
	public void onInitialize() {
		// test the event
		BlockTransformerEvents.MODIFY.register(
				(key, transforms, source, registries) -> {
					if (source.isBuiltIn()) {
						if (key == PICKAXE) {
							// make pickaxes able to turn nether bricks into cracked nether bricks, with a nether bricks hit sound
							transforms.add(
									BlockTransformer.BlockTransformData.builder(
											BlockPredicate.matchesBlocks(Blocks.NETHER_BRICKS),
											Blocks.CRACKED_NETHER_BRICKS
									).sound(Holder.direct(SoundEvents.NETHER_BRICKS_HIT)).build()
							);
						}
					}
				}
		);

		// make axes able to turn any wool stairs into a white wool slab, with the effects of stripping
		BlockTransformerHelper.registerStripping(BlockTags.WOOL_STAIRS, Blocks.WOOL_SLAB.white());

		// make hoes able to turn bamboo mosaics into top-half bamboo mosaic slabs, with the effects of tilling
		BlockTransformerHelper.registerTilling(Blocks.BAMBOO_MOSAIC,
				BlockStateProvider.simple(
						Blocks.BAMBOO_MOSAIC_SLAB.defaultBlockState().setValue(
								BlockStateProperties.SLAB_TYPE, SlabType.TOP
						)
				)
		);
		// make hoes able to turn any solid block that is on top of bedrock into packed ice, without any sound or particle
		BlockTransformerHelper.registerHoe(
				BlockTransformer.BlockTransformData.builder(
						BlockPredicate.allOf(
								BlockPredicate.solid(),
								BlockPredicate.matchesBlocks(Direction.DOWN, Blocks.BEDROCK)
						),
						Blocks.PACKED_ICE
				).build()
		);

		// make shovels able to turn acacia and birch stairs into pale oak stairs, with the effects of flattening
		BlockTransformerHelper.registerFlattening(new Block[]{Blocks.ACACIA_STAIRS, Blocks.BIRCH_STAIRS}, Blocks.PALE_OAK_STAIRS);

		// make shovels able to turn dried kelp into dead brain coral, without any sound or particle
		BlockTransformerHelper.registerShovel(
				BlockTransformer.BlockTransformData.builder(
						BlockPredicate.matchesBlocks(Blocks.DRIED_KELP_BLOCK),
						Blocks.DEAD_BRAIN_CORAL_BLOCK
				).build()
		);
	}
}
