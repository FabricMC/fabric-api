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

package net.fabricmc.fabric.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.block.v1.BlockFunctionalityTags;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
	@Inject(
			method = "canEnterTrapdoor",
			at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"),
			allow = 1,
			cancellable = true
	)
	private void allowTaggedBlocksForTrapdoorClimbing(BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> info, @Local(ordinal = 1) BlockState belowState) {
		if (belowState.isIn(BlockFunctionalityTags.CAN_CLIMB_TRAPDOOR_ABOVE)) {
			if (belowState.getBlock() instanceof LadderBlock) {
				// Check that the ladder and trapdoor are placed in the same direction.
				// Vanilla does the same check for the normal ladder block.
				if (belowState.get(LadderBlock.FACING) == state.get(TrapdoorBlock.FACING)) {
					info.setReturnValue(true);
				}
			} else {
				// Don't do any checks for other blocks. They might not even have the facing property.
				info.setReturnValue(true);
			}
		}
	}
}
