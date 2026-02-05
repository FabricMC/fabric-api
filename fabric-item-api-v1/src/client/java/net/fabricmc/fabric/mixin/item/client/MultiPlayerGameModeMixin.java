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

package net.fabricmc.fabric.mixin.item.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
	@Shadow
	@Final
	private Minecraft minecraft;
	@Shadow
	private BlockPos destroyBlockPos;
	@Shadow
	private ItemStack destroyingItem;

	/**
	 * Allows a FabricItem to continue block breaking progress even if the count or nbt changed.
	 * For this, we inject after vanilla decided that the stack was "not unchanged", and we set if back to "unchanged"
	 * if the item wishes to continue mining.
	 */
	@Redirect(
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"
			),
			method = "sameDestroyTarget"
	)
	private boolean fabricItemContinueBlockBreakingInject(ItemStack stack, ItemStack otherStack) {
		boolean stackUnchanged = ItemStack.isSameItemSameComponents(stack, this.destroyingItem);

		if (!stackUnchanged) {
			// The stack changed and vanilla is about to cancel block breaking progress. Check if the item wants to continue block breaking instead.
			ItemStack oldStack = this.destroyingItem;
			ItemStack newStack = this.minecraft.player.getMainHandItem();

			if (oldStack.is(newStack.getItem()) && oldStack.getItem().allowContinuingBlockBreaking(this.minecraft.player, oldStack, newStack)) {
				stackUnchanged = true;
			}
		}

		return stackUnchanged;
	}
}
