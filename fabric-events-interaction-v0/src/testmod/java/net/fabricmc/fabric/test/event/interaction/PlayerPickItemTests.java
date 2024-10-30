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

import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.mob.CreakingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerPickItemEvents;

public class PlayerPickItemTests implements ModInitializer {
	@Override
	public void onInitialize() {
		// Expected behavior:
		// - When sneaking and picking an item from bedrock, a barrier will be given.
		// - When sneaking, holding Ctrl/Cmd, and picking an item from bedrock, no item will be given.
		// - When sneaking and picking an item from a creaking, light with the entity name will be given.
		// - When sneaking, holding Ctrl/Cmd, and picking an item from a creaking, structure void with the entity name will be given.

		PlayerPickItemEvents.BLOCK.register((player, pos, state, includeData) -> {
			if (player.isSneaking() && state.isOf(Blocks.BEDROCK)) {
				return includeData ? ItemStack.EMPTY : new ItemStack(Items.BARRIER);
			}

			return null;
		});

		PlayerPickItemEvents.ENTITY.register((player, entity, includeData) -> {
			if (player.isSneaking() && entity instanceof CreakingEntity) {
				ItemStack stack = new ItemStack(includeData ? Items.STRUCTURE_VOID : Items.LIGHT);
				stack.set(DataComponentTypes.ITEM_NAME, entity.getName());
				return stack;
			}

			return null;
		});
	}
}
