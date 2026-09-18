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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

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

	/**
	 * Allows a FabricItem to receive an item interaction before the block interaction is handled.
	 */
	@Inject(
			method = "performUseItemOn",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/player/LocalPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"
			),
			cancellable = true
	)
	private static void fabricPerformUseItemOnBeforeBlockInteractionInject(
			final LocalPlayer player, final InteractionHand hand, final BlockHitResult blockHit,
			CallbackInfoReturnable<InteractionResult> cir
	) {
		ItemStack itemStack = player.getItemInHand(hand);

		UseOnContext context = new UseOnContext(player, hand, blockHit);
		InteractionResult ret = itemStack.useOnBeforeBlock(context);

		if (ret != InteractionResult.PASS) {
			cir.setReturnValue(ret);
		}
	}
}
