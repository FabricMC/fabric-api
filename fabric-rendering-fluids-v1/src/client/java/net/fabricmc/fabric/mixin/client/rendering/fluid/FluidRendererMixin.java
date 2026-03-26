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

package net.fabricmc.fabric.mixin.client.rendering.fluid;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderingImpl;

@Mixin(FluidRenderer.class)
public class FluidRendererMixin {
	@Inject(method = "tesselate", at = @At("HEAD"), cancellable = true)
	public void onHeadRender(BlockAndTintGetter view, BlockPos pos, FluidRenderer.Output output, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
		if (FluidRenderingImpl.IS_RENDERING_VANILLA_DEFAULT.isBound()) {
			// Don't attempt to get a handler when we're already rendering the vanilla default, otherwise we'll end up in an infinite loop
			return;
		}

		FluidRenderHandler handler = FluidRenderingRegistry.get(fluidState.getType());

		if (handler != null) {
			handler.renderFluid((FluidRenderer) (Object) this, pos, view, output, blockState, fluidState);
			ci.cancel();
		}
	}

	@Definition(id = "HalfTransparentBlock", type = HalfTransparentBlock.class)
	@Expression("? instanceof HalfTransparentBlock")
	@ModifyExpressionValue(method = "tesselate", at = @At("MIXINEXTRAS:EXPRESSION"))
	private boolean modifyNonOverlayCheck(boolean original, @Local(name = "relativeBlock") Block block) {
		return FluidRenderingRegistry.isBlockTransparent(block);
	}
}
