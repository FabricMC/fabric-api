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

import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

public class UseBlockTests implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(UseBlockTests.class);

	@Override
	public void onInitialize() {
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			LOGGER.info("UseBlockCallback: before chest/water hook (client-side = %s)".formatted(world.isClient()));
			return ActionResult.PASS;
		});

		// If a chest is used and the player holds a water bucket, delete it!
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			BlockPos pos = hitResult.getBlockPos();

			if (!player.isSpectator() && world.canEntityModifyAt(player, pos)) {
				if (world.getBlockState(pos).isOf(Blocks.CHEST)) {
					if (player.getStackInHand(hand).isOf(Items.WATER_BUCKET)) {
						world.setBlockState(pos, Blocks.AIR.getDefaultState());
						return ActionResult.SUCCESS;
					}
				}
			}

			return ActionResult.PASS;
		});

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			LOGGER.info("UseBlockCallback: after chest/water hook (client-side = %s)".formatted(world.isClient()));
			return ActionResult.PASS;
		});
	}
}
