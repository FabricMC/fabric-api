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

package net.fabricmc.fabric.test.entity.event.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRenderEvents;
import net.fabricmc.fabric.api.entity.event.client.ClientPlayerEvents;
import net.fabricmc.fabric.test.entity.event.EntityEventTests;

public class EntityEventTestsClient implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityEventTestsClient.class);

	@Override
	public void onInitializeClient() {
		LivingEntityFeatureRenderEvents.ALLOW_CAPE_RENDER.register(player -> {
			return !player.getEquippedStack(EquipmentSlot.CHEST).isOf(EntityEventTests.DIAMOND_ELYTRA);
		});

		// the most common usage is to modify using item speed based on main hand item, most for custom items.
		ClientPlayerEvents.MODIFY_ITEM_USE_SPEED.register(player -> {
			if (player.getMainHandStack().isOf(Items.GOLDEN_APPLE)) {
				LOGGER.info("Player {} can move with half speed when eating golden apple.", player);
				return 0.5F;
			}

			if (player.getMainHandStack().isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
				LOGGER.info("Player {} can move with normal speed when eating enchanted golden apple.", player);
				return 1.0F;
			}

			return null;
		});

		// another possible usages is to modify using item speed when certain conditions (e.g. equipped items) are met.
		ClientPlayerEvents.MODIFY_ITEM_USE_SPEED.register(player -> {
			if (player.getMainHandStack().isOf(Items.BOW) && player.getEquippedStack(EquipmentSlot.FEET).isOf(Items.LEATHER_BOOTS)) {
				LOGGER.info("Player {} can move with 80% speed becase of leather boots on feet.", player);
				return 0.8F;
			}

			if (player.getMainHandStack().isOf(Items.BOW) && player.getEquippedStack(EquipmentSlot.FEET).isOf(Items.IRON_BOOTS)) {
				LOGGER.info("Player {} can't move during using bow becase of iron boots on feet.", player);
				return 0.0F;
			}

			return null;
		});

		// this is used to test when multiple mods modify the using item speed together.
		ClientPlayerEvents.MODIFY_ITEM_USE_SPEED.register(player -> {
			if (player.getMainHandStack().isOf(Items.BOW) && player.getOffHandStack().isOf(Items.FEATHER)) {
				LOGGER.info("Player {} can move with half speed becase of feather in offhand.", player);
				return 0.5F;
			}

			return null;
		});
	}
}
