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

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.ItemEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

public class UseItemTests implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(UseItemTests.class);

	@Override
	public void onInitialize() {
		UseItemCallback.EVENT.register((player, world, hand) -> {
			LOGGER.info("UseItemCallback: before hook (client-side = %s)".formatted(world.isClientSide()));
			return InteractionResult.PASS;
		});

		// If a player is holding a blaze rod and right-clicks spawn a fireball!
		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (!player.isSpectator()) {
				if (player.getItemInHand(hand).is(Items.BLAZE_ROD)) {
					if (!world.isClientSide()) {
						player.level().addFreshEntity(new LargeFireball(player.level(), player, new Vec3(0, 0, 0), 0));
					}

					return InteractionResult.SUCCESS;
				}
			}

			return InteractionResult.PASS;
		});

		UseItemCallback.EVENT.register((player, world, hand) -> {
			LOGGER.info("UseItemCallback: after hook (client-side = %s)".formatted(world.isClientSide()));
			return InteractionResult.PASS;
		});

		ItemEvents.USE_ON.register((useOnContext) -> {
			LOGGER.info("ItemEvents.USE_ON: (client-side = %s)".formatted(useOnContext.getLevel().isClientSide()));
			return null;
		});

		ItemEvents.USE.register((level, player, interactionHand) -> {
			LOGGER.info("ItemEvents.USE: (client-side = %s)".formatted(level.isClientSide()));
			return null;
		});

		ItemEvents.USE.register((level, player, interactionHand) -> {
			ItemStack stack = player.getItemInHand(interactionHand);

			if (stack.is(Items.FIRE_CHARGE)) {
				Fireball fireball = new SmallFireball(level, player, player.getLookAngle());
				level.addFreshEntity(fireball);
				stack.consume(1, player);
				return InteractionResult.SUCCESS;
			}

			return null;
		});

		ItemEvents.USE_ON.register((useOnContext) -> {
			ItemStack stack = useOnContext.getItemInHand();

			if (stack.is(Items.BLAZE_POWDER)) {
				return Items.FLINT_AND_STEEL.useOn(useOnContext);
			}

			return null;
		});
	}
}
