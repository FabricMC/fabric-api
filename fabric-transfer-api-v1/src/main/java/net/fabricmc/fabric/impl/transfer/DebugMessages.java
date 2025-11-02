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

package net.fabricmc.fabric.impl.transfer;

import org.jspecify.annotations.Nullable;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class DebugMessages {
	public static String forGlobalPos(@Nullable World world, BlockPos pos) {
		String dimension = world != null ? world.getDimensionEntry().getIdAsString() : "<no dimension>";
		return dimension + "@" + pos.toShortString();
	}

	public static String forPlayer(PlayerEntity player) {
		return player.getDisplayName() + "/" + player.getUuidAsString();
	}

	public static String forInventory(@Nullable Inventory inventory) {
		if (inventory == null) {
			return "~~NULL~~"; // like in crash reports
		} else if (inventory instanceof PlayerInventory playerInventory) {
			return forPlayer(playerInventory.player);
		} else {
			String result = inventory.toString();

			if (inventory instanceof BlockEntity blockEntity) {
				result += " (%s, %s)".formatted(blockEntity.getCachedState(), forGlobalPos(blockEntity.getWorld(), blockEntity.getPos()));
			}

			return result;
		}
	}
}
