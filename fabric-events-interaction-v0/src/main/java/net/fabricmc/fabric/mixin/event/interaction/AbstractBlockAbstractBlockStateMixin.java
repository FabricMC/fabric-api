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

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.event.player.UseItemOnBlockEvents;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockAbstractBlockStateMixin {
	@Shadow
	protected abstract BlockState asBlockState();

	@Inject(method = "onUseWithItem", at = @At("HEAD"), cancellable = true)
	private void fabric_onUseWithItem(ItemStack stack, World world, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
		ActionResult result = UseItemOnBlockEvents.BLOCK.invoker().onUseWithItem(stack, this.asBlockState(), world, hit.getBlockPos(), player, hand, hit);

		if (result != ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION) {
			cir.setReturnValue(result);
		}
	}
}
