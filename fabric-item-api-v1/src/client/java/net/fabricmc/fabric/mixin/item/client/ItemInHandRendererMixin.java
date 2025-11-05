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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.item.v1.FabricItem;

/**
 * Allow canceling the held item update animation if {@link FabricItem#allowComponentsUpdateAnimation} returns false.
 */
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
	@Shadow
	private ItemStack mainHandItem;

	@Shadow
	private ItemStack offHandItem;

	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "tick", at = @At("HEAD"))
	private void modifyProgressAnimation(CallbackInfo ci) {
		// Modify main hand
		ItemStack newMainStack = minecraft.player.getMainHandItem();

		if (mainHandItem.getItem() == newMainStack.getItem()) {
			if (!mainHandItem.getItem().allowComponentsUpdateAnimation(minecraft.player, InteractionHand.MAIN_HAND, mainHandItem, newMainStack)) {
				mainHandItem = newMainStack;
			}
		}

		// Modify off hand
		ItemStack newOffStack = minecraft.player.getOffhandItem();

		if (offHandItem.getItem() == newOffStack.getItem()) {
			if (!offHandItem.getItem().allowComponentsUpdateAnimation(minecraft.player, InteractionHand.OFF_HAND, offHandItem, newOffStack)) {
				offHandItem = newOffStack;
			}
		}
	}
}
