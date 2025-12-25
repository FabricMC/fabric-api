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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

public class UseBlockTests implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(UseBlockTests.class);

	@Override
	public void onInitialize() {
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			LOGGER.info("UseBlockCallback: before chest/water hook (client-side = %s)".formatted(level.isClientSide()));
			return InteractionResult.PASS;
		});

		// If a chest is used and the player holds a water bucket, delete it!
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			BlockPos pos = hitResult.getBlockPos();

			if (!player.isSpectator() && level.mayInteract(player, pos)) {
				if (level.getBlockState(pos).is(Blocks.CHEST)) {
					if (player.getItemInHand(hand).is(Items.WATER_BUCKET)) {
						level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
						return InteractionResult.SUCCESS;
					}
				}
			}

			return InteractionResult.PASS;
		});

		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			LOGGER.info("UseBlockCallback: after chest/water hook (client-side = %s)".formatted(level.isClientSide()));
			return InteractionResult.PASS;
		});
	}
}
