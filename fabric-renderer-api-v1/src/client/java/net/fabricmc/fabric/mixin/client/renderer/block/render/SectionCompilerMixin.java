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

package net.fabricmc.fabric.mixin.client.renderer.block.render;

import java.util.Map;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexSorting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.AltModelBlockRenderer;

@Mixin(SectionCompiler.class)
abstract class SectionCompilerMixin {
	@Shadow
	@Final
	private boolean ambientOcclusion;
	@Shadow
	@Final
	private BlockColors blockColors;

	@Shadow
	protected abstract BufferBuilder getOrBeginLayer(
			Map<ChunkSectionLayer, BufferBuilder> startedLayers,
			SectionBufferBuilderPack buffers,
			ChunkSectionLayer layer
	);

	@Inject(method = "compile", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;betweenClosed(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Ljava/lang/Iterable;"))
	private void beforeLoopCompile(SectionPos sectionPos, RenderSectionRegion region, VertexSorting vertexSorting, SectionBufferBuilderPack builders, CallbackInfoReturnable<SectionCompiler.Results> cir, @Local(name = "startedLayers") Map<ChunkSectionLayer, BufferBuilder> startedLayers, @Share("altBlockRenderer") LocalRef<AltModelBlockRenderer> altBlockRenderer, @Share("altQuadOutput") LocalRef<QuadEmitter> altQuadOutput) {
		altBlockRenderer.set(Renderer.get().altModelBlockRenderer(ambientOcclusion, true, blockColors));
		altQuadOutput.set(Renderer.get().quadEmitter(quad -> {
			BufferBuilder builder = getOrBeginLayer(startedLayers, builders, quad.chunkLayer());
			quad.buffer(OverlayTexture.NO_OVERLAY, builder);
		}));
	}

	@Redirect(method = "compile", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/block/ModelBlockRenderer.tesselateBlock(Lnet/minecraft/client/renderer/block/BlockQuadOutput;FFFLnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;J)V"))
	private void tesselateBlockProxy(ModelBlockRenderer blockRenderer, BlockQuadOutput output, float x, float y, float z, BlockAndTintGetter level, BlockPos pos, BlockState blockState, BlockStateModel model, long seed, @Share("altBlockRenderer") LocalRef<AltModelBlockRenderer> altBlockRenderer, @Share("altQuadOutput") LocalRef<QuadEmitter> altQuadOutput) {
		altBlockRenderer.get().tesselateBlock(altQuadOutput.get(), x, y, z, level, pos, blockState, model, seed);
	}
}
