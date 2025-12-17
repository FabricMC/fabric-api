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

package net.fabricmc.fabric.mixin.client.indigo.renderer;

import java.util.List;
import java.util.Map;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessChunkRendererRegion;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainRenderContext;

/**
 * Implements the main hooks for terrain rendering. Attempts to tread
 * lightly. This means we are deliberately stepping over some minor
 * optimization opportunities.
 *
 * <p>Non-Fabric renderer implementations that are looking to maximize
 * performance will likely take a much more aggressive approach.
 * For that reason, mod authors who want compatibility with advanced
 * renderers will do well to steer clear of chunk rebuild hooks unless
 * they are creating a renderer.
 *
 * <p>These hooks are intended only for the Fabric default renderer and
 * aren't expected to be present when a different renderer is being used.
 * Renderer authors are responsible for creating the hooks they need.
 * (Though they can use these as an example if they wish.)
 */
@Mixin(SectionCompiler.class)
abstract class SectionCompilerMixin {
	@Shadow
	@Final
	private BlockRenderDispatcher blockRenderer;

	@Shadow
	protected abstract BufferBuilder getOrBeginLayer(
			Map<ChunkSectionLayer, BufferBuilder> builders,
			SectionBufferBuilderPack allocatorStorage,
			ChunkSectionLayer layer
	);

	@Inject(method = "compile",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;betweenClosed(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Ljava/lang/Iterable;"))
	private void hookBuild(SectionPos sectionPos, RenderSectionRegion region, VertexSorting sorter,
						SectionBufferBuilderPack allocators,
						CallbackInfoReturnable<SectionCompiler.Results> cir,
						@Local(ordinal = 0) BlockPos sectionOrigin,
						@Local(ordinal = 0) PoseStack matrixStack,
						@Local(ordinal = 0) Map<ChunkSectionLayer, BufferBuilder> builderMap,
						@Local(ordinal = 0) RandomSource random) {
		// hook just before iterating over the render chunk's blocks to capture the buffer builder map
		TerrainRenderContext renderer = TerrainRenderContext.POOL.get();
		renderer.prepare(region, sectionOrigin, matrixStack, random, layer -> getOrBeginLayer(builderMap, allocators, layer));
		((AccessChunkRendererRegion) region).fabric_setRenderer(renderer);
	}

	/**
	 * This is the hook that actually implements the rendering API for terrain rendering.
	 *
	 * <p>It's unusual to have a @Redirect in a Fabric library, but in this case it is our explicit intention that
	 * {@link BlockStateModel#collectParts(RandomSource, List)} and
	 * {@link BlockRenderDispatcher#renderBatched(BlockState, BlockPos, BlockAndTintGetter, PoseStack, VertexConsumer, boolean, List)}
	 * do not execute for models that will be rendered by our renderer. For performance and convenience, just skip the
	 * entire if block.
	 *
	 * <p>Any mod that wants to redirect this specific call is likely also a renderer, in which case this
	 * renderer should not be present, or the mod should probably instead be relying on the renderer API
	 * which was specifically created to provide for enhanced terrain rendering.
	 */
	@Redirect(method = "compile", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getRenderShape()Lnet/minecraft/world/level/block/RenderShape;"))
	private RenderShape hookBuildRenderBlock(BlockState blockState, SectionPos sectionPos, RenderSectionRegion renderRegion, VertexSorting vertexSorter, SectionBufferBuilderPack allocatorStorage, @Local(ordinal = 2) BlockPos blockPos) {
		RenderShape blockRenderType = blockState.getRenderShape();

		if (blockRenderType == RenderShape.MODEL) {
			BlockStateModel model = blockRenderer.getBlockModel(blockState);
			((AccessChunkRendererRegion) renderRegion).fabric_getRenderer().bufferModel(model, blockState, blockPos);
			return RenderShape.INVISIBLE; // Cancel the vanilla logic
		}

		return blockRenderType;
	}

	/**
	 * Release all references. Probably not necessary but would be $#%! to debug if it is.
	 */
	@Inject(method = "compile", at = @At(value = "RETURN"))
	private void hookBuildReturn(SectionPos sectionPos, RenderSectionRegion renderRegion, VertexSorting vertexSorter, SectionBufferBuilderPack allocatorStorage, CallbackInfoReturnable<SectionCompiler.Results> cir) {
		((AccessChunkRendererRegion) renderRegion).fabric_getRenderer().release();
		((AccessChunkRendererRegion) renderRegion).fabric_setRenderer(null);
	}
}
