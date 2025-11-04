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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;

import net.fabricmc.fabric.impl.transfer.item.SpecialLogicInventory;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin implements SpecialLogicInventory {
	@Shadow
	private ItemStack item;

	@Shadow
	public abstract void setTheItem(ItemStack stack);

	@Unique
	private boolean fabric_suppressSpecialLogic = false;

	@Override
	public void fabric_setSuppress(boolean suppress) {
		fabric_suppressSpecialLogic = suppress;
	}

	@Inject(method = "setTheItem", at = @At("HEAD"), cancellable = true)
	private void setStackBypass(ItemStack stack, CallbackInfo ci) {
		if (fabric_suppressSpecialLogic) {
			item = stack;
			ci.cancel();
		}
	}

	@Override
	public void fabric_onFinalCommit(int slot, ItemStack oldStack, ItemStack newStack) {
		// Call setStack again without suppressing vanilla logic,
		// where now the record will actually getting played/stopped.
		setTheItem(newStack);
	}
}
