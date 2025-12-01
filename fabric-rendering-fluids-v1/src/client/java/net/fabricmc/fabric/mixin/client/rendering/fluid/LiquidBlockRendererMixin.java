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
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderHandlerInfo;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderHandlerRegistryImpl;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderingImpl;

@Mixin(LiquidBlockRenderer.class)
public class LiquidBlockRendererMixin {
	@Shadow
	@Final
	private TextureAtlasSprite waterOverlay;

	@Shadow
	@Final
	private TextureAtlasSprite waterStill;

	@Shadow
	@Final
	private TextureAtlasSprite waterFlowing;

	@Shadow
	@Final
	private TextureAtlasSprite lavaStill;

	@Shadow
	@Final
	private TextureAtlasSprite lavaFlowing;

	@Inject(method = "<init>", at = @At("RETURN"))
	public void onResourceReloadReturn(CallbackInfo info) {
		LiquidBlockRenderer self = (LiquidBlockRenderer) (Object) this;
		((FluidRenderHandlerRegistryImpl) FluidRenderHandlerRegistry.INSTANCE).onFluidRendererReload(self, new TextureAtlasSprite[]{waterStill, waterFlowing, waterOverlay}, new TextureAtlasSprite[]{lavaStill, lavaFlowing}, waterOverlay);
	}

	@Inject(method = "tesselate", at = @At("HEAD"), cancellable = true)
	public void onHeadRender(BlockAndTintGetter view, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
		FluidRenderHandlerInfo info = FluidRenderingImpl.getCurrentInfo();

		if (info.handler == null) {
			FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(fluidState.getType());

			if (handler != null) {
				handler.renderFluid(pos, view, vertexConsumer, blockState, fluidState);
				ci.cancel();
			}
		}
	}

	@Unique
	private TextureAtlasSprite getOrDefault(int index, TextureAtlasSprite original) {
		FluidRenderHandlerInfo info = FluidRenderingImpl.getCurrentInfo();

		if (info.handler == null) {
			return original;
		}

		if (info.sprites.length == index - 1) {
			return original;
		}

		return info.sprites[index];
	}

	@ModifyVariable(
			method = "tesselate",
			at = @At("STORE"),
			ordinal = 0
	)
	public TextureAtlasSprite modStill(TextureAtlasSprite original) {
		return getOrDefault(0, original);
	}

	@ModifyVariable(
			method = "tesselate",
			at = @At("STORE"),
			ordinal = 1
	)
	public TextureAtlasSprite modFlowing(TextureAtlasSprite original) {
		return getOrDefault(1, original);
	}

	@ModifyExpressionValue(
			method = "tesselate",
			at = {
					@At(value = "CONSTANT", args = "intValue=" + 0xffffff),
					@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/BiomeColors;getAverageWaterColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I")
			}
	)
	public int modTintColor(int original, BlockAndTintGetter world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
		FluidRenderHandlerInfo info = FluidRenderingImpl.getCurrentInfo();
		return info.handler != null ? info.handler.getFluidColor(world, pos, fluidState) : original;
	}

	@Definition(id = "getU", method = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;getU(F)F")
	@Definition(id = "sprite2", local = @Local(type = TextureAtlasSprite.class, ordinal = 2))
	@Expression("@(sprite2).getU(0.0)")
	@ModifyVariable(
			method = "tesselate",
			at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0),
			slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;waterOverlay:Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;")),
			ordinal = 2
	)
	private TextureAtlasSprite modifyOverlaySprite(
			TextureAtlasSprite waterOverlay,
			BlockAndTintGetter world,
			@Local(ordinal = 1) BlockPos neighborPos,
			@Local(ordinal = 0) boolean isLava,
			@Local(ordinal = 1) TextureAtlasSprite flowingSprite,
			@Share("useOverlay") LocalBooleanRef useOverlay
	) {
		final FluidRenderHandlerInfo info = FluidRenderingImpl.getCurrentInfo();
		boolean hasOverlay = info.handler != null ? info.hasOverlay : !isLava;

		Block neighborBlock = world.getBlockState(neighborPos).getBlock();
		useOverlay.set(hasOverlay && FluidRenderHandlerRegistry.INSTANCE.isBlockTransparent(neighborBlock));

		if (useOverlay.get()) {
			return info.handler != null ? info.overlaySprite : this.waterOverlay;
		} else {
			return flowingSprite;
		}
	}

	@Definition(id = "sprite2", local = @Local(type = TextureAtlasSprite.class, ordinal = 2))
	@Definition(id = "waterOverlaySprite", field = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;waterOverlay:Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;")
	@Expression("sprite2 != this.waterOverlaySprite")
	@ModifyExpressionValue(method = "tesselate", at = @At("MIXINEXTRAS:EXPRESSION"))
	private boolean modifyNonOverlayCheck(boolean original, @Share("useOverlay") LocalBooleanRef useOverlay) {
		return !useOverlay.get();
	}
}
