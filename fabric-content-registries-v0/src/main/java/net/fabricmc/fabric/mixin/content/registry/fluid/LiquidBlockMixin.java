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

package net.fabricmc.fabric.mixin.content.registry.fluid;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.registry.fluid.AllowFluidFlow;

@Mixin(LiquidBlock.class)
public abstract class LiquidBlockMixin extends Block {
	public LiquidBlockMixin(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Inject(method = "shouldSpreadLiquid", at = @At("HEAD"), cancellable = true)
	private void shouldSpreadLiquid(Level level, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
		if (!AllowFluidFlow.EVENT.invoker().allowFlow(level.getFluidState(pos), level, pos)) {
			cir.setReturnValue(false);
		}
	}
}
