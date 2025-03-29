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
import com.mojang.blaze3d.systems.VertexSorter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

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
@Mixin(SectionBuilder.class)
abstract class SectionBuilderMixin {
	@Shadow
	@Final
	private BlockRenderManager blockRenderManager;

	@Shadow
	abstract BufferBuilder beginBufferBuilding(Map<RenderLayer, BufferBuilder> builders, BlockBufferAllocatorStorage allocatorStorage, RenderLayer layer);

	@Inject(method = "build",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;iterate(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Ljava/lang/Iterable;"))
	private void hookBuild(ChunkSectionPos sectionPos, ChunkRendererRegion region, VertexSorter sorter,
						BlockBufferAllocatorStorage allocators,
						CallbackInfoReturnable<SectionBuilder.RenderData> cir,
						@Local(ordinal = 0) BlockPos sectionOrigin,
						@Local(ordinal = 0) MatrixStack matrixStack,
						@Local(ordinal = 0) Map<RenderLayer, BufferBuilder> builderMap,
						@Local(ordinal = 0) Random random) {
		// hook just before iterating over the render chunk's blocks to capture the buffer builder map
		TerrainRenderContext renderer = TerrainRenderContext.POOL.get();
		renderer.prepare(region, sectionOrigin, matrixStack, random, layer -> beginBufferBuilding(builderMap, allocators, layer));
		((AccessChunkRendererRegion) region).fabric_setRenderer(renderer);
	}

	/**
	 * This is the hook that actually implements the rendering API for terrain rendering.
	 *
	 * <p>It's unusual to have a @Redirect in a Fabric library, but in this case it is our explicit intention that
	 * {@link BlockStateModel#addParts(Random, List)} and
	 * {@link BlockRenderManager#renderBlock(BlockState, BlockPos, BlockRenderView, MatrixStack, VertexConsumer, boolean, List)}
	 * do not execute for models that will be rendered by our renderer. For performance and convenience, just skip the
	 * entire if block.
	 *
	 * <p>Any mod that wants to redirect this specific call is likely also a renderer, in which case this
	 * renderer should not be present, or the mod should probably instead be relying on the renderer API
	 * which was specifically created to provide for enhanced terrain rendering.
	 */
	@Redirect(method = "build", at = @At(value = "INVOKE", target = "net/minecraft/block/BlockState.getRenderType()Lnet/minecraft/block/BlockRenderType;"))
	private BlockRenderType hookBuildRenderBlock(BlockState blockState, ChunkSectionPos sectionPos, ChunkRendererRegion renderRegion, VertexSorter vertexSorter, BlockBufferAllocatorStorage allocatorStorage, @Local(ordinal = 2) BlockPos blockPos) {
		BlockRenderType blockRenderType = blockState.getRenderType();

		if (blockRenderType == BlockRenderType.MODEL) {
			BlockStateModel model = blockRenderManager.getModel(blockState);
			((AccessChunkRendererRegion) renderRegion).fabric_getRenderer().bufferModel(model, blockState, blockPos);
			return BlockRenderType.INVISIBLE; // Cancel the vanilla logic
		}

		return blockRenderType;
	}

	/**
	 * Release all references. Probably not necessary but would be $#%! to debug if it is.
	 */
	@Inject(method = "build", at = @At(value = "RETURN"))
	private void hookBuildReturn(ChunkSectionPos sectionPos, ChunkRendererRegion renderRegion, VertexSorter vertexSorter, BlockBufferAllocatorStorage allocatorStorage, CallbackInfoReturnable<SectionBuilder.RenderData> cir) {
		((AccessChunkRendererRegion) renderRegion).fabric_getRenderer().release();
		((AccessChunkRendererRegion) renderRegion).fabric_setRenderer(null);
	}
}
