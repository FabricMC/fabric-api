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

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseBehavior;
import net.minecraft.core.dispenser.InsertIntoContainerDispenseBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

@Mixin(InsertIntoContainerDispenseBehavior.class)
class InsertIntoContainerDispenseBehaviorMixin {
	@WrapMethod(method = "execute")
	private DispenseBehavior.DispenseResult insertIntoStorage(BlockSource source, ItemStack dispensed, Operation<DispenseBehavior.DispenseResult> original) {
		DispenseBehavior.DispenseResult result = original.call(source, dispensed);

		if (!(result instanceof DispenseBehavior.DispenseResult.Failure)) {
			return result;
		}

		Direction direction = source.state().getValue(DispenserBlock.FACING);
		Storage<ItemVariant> target = ItemStorage.SIDED.find(source.level(), source.pos().relative(direction), direction.getOpposite());

		if (target == null) {
			return result;
		}

		try (Transaction transaction = Transaction.openOuter()) {
			if (target.insert(ItemVariant.of(dispensed), 1, transaction) == 1) {
				ItemStack remaining = dispensed.copy();
				remaining.shrink(1);
				transaction.commit();
				return DispenseBehavior.DispenseResult.success(remaining);
			}
		}

		return DispenseBehavior.DispenseResult.stoppingFailure();
	}
}
