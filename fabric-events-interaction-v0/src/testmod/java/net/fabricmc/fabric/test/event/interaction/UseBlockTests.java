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

package net.fabricmc.fabric.test.event.interaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.BlockEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

public class UseBlockTests implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(UseBlockTests.class);

	@Override
	public void onInitialize() {
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			LOGGER.info("UseBlockCallback: before chest/water hook (client-side = %s)".formatted(world.isClientSide()));
			return InteractionResult.PASS;
		});

		// If a chest is used and the player holds a water bucket, delete it!
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			BlockPos pos = hitResult.getBlockPos();

			if (!player.isSpectator() && world.mayInteract(player, pos)) {
				if (world.getBlockState(pos).is(Blocks.CHEST)) {
					if (player.getItemInHand(hand).is(Items.WATER_BUCKET)) {
						world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
						return InteractionResult.SUCCESS;
					}
				}
			}

			return InteractionResult.PASS;
		});

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			LOGGER.info("UseBlockCallback: after chest/water hook (client-side = %s)".formatted(world.isClientSide()));
			return InteractionResult.PASS;
		});

		BlockEvents.USE_WITHOUT_ITEM.register((blockState, level, blockPos, player, blockHitResult) -> {
			LOGGER.info("BlockEvents.USE_WITHOUT_ITEM: (client-side = %s)".formatted(level.isClientSide()));

			return null;
		});

		BlockEvents.USE_ITEM_ON.register((itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult) -> {
			LOGGER.info("BlockEvents.USE_ITEM_ON: (client-side = %s)".formatted(level.isClientSide()));

			return null;
		});

		BlockEvents.USE_WITHOUT_ITEM.register((blockState, level, blockPos, player, blockHitResult) -> {
			if (blockState.is(Blocks.NOTE_BLOCK) && player.hasEffect(MobEffects.BAD_OMEN)) {
				return InteractionResult.FAIL;
			}

			return null;
		});

		BlockEvents.USE_ITEM_ON.register((itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult) -> {
			if (blockState.is(Blocks.SNOW) && itemStack.is(ItemTags.SHOVELS)) {
				if (!level.isClientSide()) {
					int layer = blockState.getValue(SnowLayerBlock.LAYERS) - 1;

					if (layer <= 0) {
						level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
					} else {
						level.setBlockAndUpdate(blockPos, blockState.setValue(SnowLayerBlock.LAYERS, layer));
					}

					itemStack.hurtAndBreak(1, player, interactionHand);
					player.addItem(Items.SNOWBALL.getDefaultInstance());
				}

				return InteractionResult.SUCCESS;
			}

			return null;
		});
	}
}
