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

package net.fabricmc.fabric.mixin.event.interaction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import net.fabricmc.fabric.api.event.player.BlockEvents;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockBehaviourBlockStateBaseMixin {
	@Shadow
	protected abstract BlockState asState();

	@Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
	private void callUseItemOnEvent(ItemStack itemStack, Level level, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
		InteractionResult result = BlockEvents.USE_ITEM_ON.invoker().useItemOn(itemStack, this.asState(), level, blockHitResult.getBlockPos(), player, interactionHand, blockHitResult);

		if (result != null) {
			cir.setReturnValue(result);
		}
	}

	@Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
	private void callUseWithoutItemEvent(Level level, Player player, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
		InteractionResult result = BlockEvents.USE_WITHOUT_ITEM.invoker().useWithoutItem(this.asState(), level, blockHitResult.getBlockPos(), player, blockHitResult);

		if (result != null) {
			cir.setReturnValue(result);
		}
	}
}
