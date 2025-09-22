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
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.block.entity.ShelfBlockEntity;
import net.minecraft.item.ItemStack;

import net.fabricmc.fabric.impl.transfer.item.SpecialLogicAccess;
import net.fabricmc.fabric.impl.transfer.item.SpecialLogicInventory;

@Mixin(ShelfBlockEntity.class)
public abstract class ShelfBlockEntityMixin implements SpecialLogicInventory, SpecialLogicAccess {
	@Unique
	boolean fabric_suppressSpecialLogic;

	@Override
	public void fabric_setSuppress(boolean suppress) {
		fabric_suppressSpecialLogic = suppress;
	}

	@Override
	public boolean fabric_shouldSuppressSpecialLogic() {
		return fabric_suppressSpecialLogic;
	}

	@Override
	public void fabric_onFinalCommit(int slot, ItemStack oldStack, ItemStack newStack) {
	}
}
