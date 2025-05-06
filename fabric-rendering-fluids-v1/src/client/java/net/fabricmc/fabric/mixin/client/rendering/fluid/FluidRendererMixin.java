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

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
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

	private final ThreadLocal<Block> fabric_neighborBlock = new ThreadLocal<>();

	@Inject(at = @At("RETURN"), method = "onResourceReload")
	public void onResourceReloadReturn(CallbackInfo info) {
		FluidRenderer self = (FluidRenderer) (Object) this;
		((FluidRenderHandlerRegistryImpl) FluidRenderHandlerRegistry.INSTANCE).onFluidRendererReload(self, waterSprites, lavaSprites, waterOverlaySprite);
	}

	@Inject(at = @At("HEAD"), method = "render", cancellable = true)
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

	@ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/FluidRenderer;isSameFluid(Lnet/minecraft/fluid/FluidState;Lnet/minecraft/fluid/FluidState;)Z"), method = "render", ordinal = 0)
	public boolean modLavaCheck(boolean chk) {
		// First boolean local is set by vanilla according to 'matches lava'
		// but uses the negation consistent with 'matches water'
		// for determining if overlay water sprite should be used behind glass.

		// Has other uses but those are overridden by this mixin and have
		// already happened by the time this hook is called.

		// If this fluid has an overlay texture, set this boolean to false.
		final FluidRenderHandlerInfo info = FluidRenderingImpl.getCurrentInfo();
		return info.handler != null ? !info.hasOverlay : chk;
	}

	@ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/FluidRenderer;isSameFluid(Lnet/minecraft/fluid/FluidState;Lnet/minecraft/fluid/FluidState;)Z"), method = "render", ordinal = 0)
	public Sprite[] modSpriteArray(Sprite[] chk) {
		FluidRenderHandlerInfo info = FluidRenderingImpl.getCurrentInfo();
		return info.handler != null ? info.sprites : chk;
	}

	@ModifyVariable(at = @At(value = "CONSTANT", args = "intValue=16", ordinal = 0, shift = At.Shift.BEFORE), method = "render", ordinal = 0)
	public int modTintColor(int chk, BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
		FluidRenderHandlerInfo info = FluidRenderingImpl.getCurrentInfo();
		return info.handler != null ? info.handler.getFluidColor(world, pos, fluidState) : chk;
	}

	// Redirect redirects all 'waterOverlaySprite' gets in 'render' to this method, this is correct
	@Redirect(at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/render/block/FluidRenderer;waterOverlaySprite:Lnet/minecraft/client/texture/Sprite;"), method = "render")
	public Sprite modWaterOverlaySprite(FluidRenderer self) {
		FluidRenderHandlerInfo info = FluidRenderingImpl.getCurrentInfo();
		return info.handler != null && info.hasOverlay ? info.overlaySprite : waterOverlaySprite;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;"), method = "render")
	public Block getOverlayBlock(BlockState state) {
		Block block = state.getBlock();
		fabric_neighborBlock.set(block);

		// An if-statement follows, we don't want this anymore and 'null' makes
		// its condition always false (due to instanceof)
		return null;
	}

	@ModifyVariable(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;"), method = "render", ordinal = 0)
	public Sprite modSideSpriteForOverlay(Sprite chk) {
		Block block = fabric_neighborBlock.get();

		if (FluidRenderHandlerRegistry.INSTANCE.isBlockTransparent(block)) {
			FluidRenderHandlerInfo info = FluidRenderingImpl.getCurrentInfo();
			return info.handler != null && info.hasOverlay ? info.overlaySprite : waterOverlaySprite;
		}

		return chk;
	}
}
