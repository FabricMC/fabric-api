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
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderHandlerInfo;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderHandlerRegistryImpl;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderingImpl;

@Mixin(FluidRenderer.class)
public class FluidRendererMixin {
	@Final
	@Shadow
	private Sprite[] lavaSprites;
	@Final
	@Shadow
	private Sprite[] waterSprites;
	@Shadow
	private Sprite waterOverlaySprite;

	@Inject(method = "onResourceReload", at = @At("RETURN"))
	public void onResourceReloadReturn(CallbackInfo info) {
		FluidRenderer self = (FluidRenderer) (Object) this;
		((FluidRenderHandlerRegistryImpl) FluidRenderHandlerRegistry.INSTANCE).onFluidRendererReload(self, waterSprites, lavaSprites, waterOverlaySprite);
	}

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	public void onHeadRender(BlockRenderView view, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
		FluidRenderHandlerInfo info = FluidRenderingImpl.getCurrentInfo();

		if (info.handler == null) {
			FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(fluidState.getFluid());

			if (handler != null) {
				handler.renderFluid(pos, view, vertexConsumer, blockState, fluidState);
				ci.cancel();
			}
		}
	}

	@ModifyVariable(
			method = "render",
			at = @At("STORE"),
			ordinal = 0
	)
	public Sprite[] modSpriteArray(Sprite[] original) {
		FluidRenderHandlerInfo info = FluidRenderingImpl.getCurrentInfo();
		return info.handler != null ? info.sprites : original;
	}

	@ModifyExpressionValue(
			method = "render",
			at = {
					@At(value = "CONSTANT", args = "intValue=" + 0xffffff),
					@At(value = "INVOKE", target = "Lnet/minecraft/client/color/world/BiomeColors;getWaterColor(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/util/math/BlockPos;)I")
			}
	)
	public int modTintColor(int original, BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
		FluidRenderHandlerInfo info = FluidRenderingImpl.getCurrentInfo();
		return info.handler != null ? info.handler.getFluidColor(world, pos, fluidState) : original;
	}

	@Definition(id = "getFrameU", method = "Lnet/minecraft/client/texture/Sprite;getFrameU(F)F")
	@Definition(id = "sprite2", local = @Local(type = Sprite.class))
	@Expression("@(sprite2).getFrameU(0.0)")
	@ModifyVariable(
			method = "render",
			at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0),
			slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/client/render/block/FluidRenderer;waterOverlaySprite:Lnet/minecraft/client/texture/Sprite;"))
	)
	private Sprite modifyOverlaySprite(
			Sprite sprite2,
			BlockRenderView world,
			@Local(ordinal = 1) BlockPos neighborPos,
			@Local(ordinal = 0) boolean isLava,
			@Local Sprite[] sprites,
			@Share("useOverlay") LocalBooleanRef useOverlay
	) {
		final FluidRenderHandlerInfo info = FluidRenderingImpl.getCurrentInfo();
		boolean hasOverlay = info.handler != null ? info.hasOverlay : !isLava;

		Block neighborBlock = world.getBlockState(neighborPos).getBlock();
		useOverlay.set(hasOverlay && FluidRenderHandlerRegistry.INSTANCE.isBlockTransparent(neighborBlock));

		if (useOverlay.get()) {
			return info.handler != null ? info.overlaySprite : this.waterOverlaySprite;
		} else {
			return sprites[1];
		}
	}

	@Definition(id = "sprite2", local = @Local(type = Sprite.class))
	@Definition(id = "waterOverlaySprite", field = "Lnet/minecraft/client/render/block/FluidRenderer;waterOverlaySprite:Lnet/minecraft/client/texture/Sprite;")
	@Expression("sprite2 != this.waterOverlaySprite")
	@ModifyExpressionValue(method = "render", at = @At("MIXINEXTRAS:EXPRESSION"))
	private boolean modifyNonOverlayCheck(boolean original, @Share("useOverlay") LocalBooleanRef useOverlay) {
		return !useOverlay.get();
	}
}
