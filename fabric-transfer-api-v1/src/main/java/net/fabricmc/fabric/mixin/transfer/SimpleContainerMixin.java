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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.impl.transfer.item.SpecialLogicInventory;

/**
 * Defer markDirty until the outer transaction close callback when setStack is called from an inventory wrapper.
 */
@Mixin(SimpleContainer.class)
public class SimpleContainerMixin implements SpecialLogicInventory {
	@Unique
	private boolean fabric_suppressSpecialLogic = false;

	@Redirect(
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/SimpleContainer;setChanged()V"),
			method = "setItem(ILnet/minecraft/world/item/ItemStack;)V"
	)
	public void fabric_redirectMarkDirty(SimpleContainer self) {
		if (!fabric_suppressSpecialLogic) {
			self.setChanged();
		}
	}

	@Override
	public void fabric_setSuppress(boolean suppress) {
		fabric_suppressSpecialLogic = suppress;
	}

	@Override
	public void fabric_onFinalCommit(int slot, ItemStack oldStack, ItemStack newStack) {
	}
}
