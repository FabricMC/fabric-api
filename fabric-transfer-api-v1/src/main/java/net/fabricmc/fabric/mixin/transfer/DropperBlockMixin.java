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

package net.fabricmc.fabric.mixin.transfer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.impl.transfer.TransferApiImpl;

/**
 * Allows droppers to insert into ItemVariant storages.
 */
@Mixin(DropperBlock.class)
public class DropperBlockMixin {
	@Inject(
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/core/dispenser/DispenseItemBehavior;dispense(Lnet/minecraft/core/dispenser/BlockSource;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"
			),
			method = "dispenseFrom",
			cancellable = true,
			allow = 1
	)
	public void hookDispense(ServerLevel world, BlockState blockState, BlockPos pos, CallbackInfo ci) {
		DispenserBlockEntity dispenser = (DispenserBlockEntity) world.getBlockEntity(pos);
		Direction direction = dispenser.getBlockState().getValue(DispenserBlock.FACING);

		Storage<ItemVariant> target = ItemStorage.SIDED.find(world, pos.relative(direction), direction.getOpposite());

		if (target != null) {
			// Always cancel if a storage is available.
			ci.cancel();

			// We pick a non empty slot. It's not necessarily the same as the one vanilla picked, but that doesn't matter.
			int slot = dispenser.getRandomSlot(world.random);

			if (slot == -1) {
				TransferApiImpl.LOGGER.warn("Skipping dropper transfer because the empty slot is unexpectedly -1.");
				return;
			}

			StorageUtil.move(
					InventoryStorage.of(dispenser, null).getSlot(slot),
					target,
					k -> true,
					1,
					null
			);
		}
	}
}
