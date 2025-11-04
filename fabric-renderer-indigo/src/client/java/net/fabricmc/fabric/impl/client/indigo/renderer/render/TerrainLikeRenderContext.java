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

package net.fabricmc.fabric.impl.client.indigo.renderer.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.renderer.v1.render.BlockVertexConsumerProvider;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoLuminanceFix;

/**
 * Used during terrain-like block buffering to invoke {@link BlockStateModel#emitQuads}.
 */
public class TerrainLikeRenderContext extends AbstractTerrainRenderContext {
	public static final ThreadLocal<TerrainLikeRenderContext> POOL = ThreadLocal.withInitial(TerrainLikeRenderContext::new);

	private final RandomSource random = RandomSource.createNewThreadLocalInstance();

	private BlockVertexConsumerProvider vertexConsumers;

	@Override
	protected LightDataProvider createLightDataProvider(BlockRenderInfo blockInfo) {
		// TODO: Use a cache whenever vanilla would use a cache (BrightnessCache.enabled)
		return new LightDataProvider() {
			@Override
			public int light(BlockPos pos, BlockState state) {
				return LevelRenderer.getLightColor(LevelRenderer.BrightnessGetter.DEFAULT, blockInfo.blockView, state, pos);
			}

			@Override
			public float ao(BlockPos pos, BlockState state) {
				return AoLuminanceFix.INSTANCE.apply(blockInfo.blockView, pos, state);
			}
		};
	}

	@Override
	protected VertexConsumer getVertexConsumer(ChunkSectionLayer layer) {
		return vertexConsumers.getBuffer(layer);
	}

	public void bufferModel(BlockAndTintGetter blockView, BlockStateModel model, BlockState state, BlockPos pos, PoseStack matrixStack, BlockVertexConsumerProvider vertexConsumers, boolean cull, long seed, int overlay) {
		try {
			Vec3 offset = state.getOffset(pos);
			matrixStack.translate(offset.x, offset.y, offset.z);
			matrices = matrixStack.last();
			this.overlay = overlay;

			this.vertexConsumers = vertexConsumers;

			blockInfo.prepareForWorld(blockView, cull);
			random.setSeed(seed);

			prepare(pos, state);
			model.emitQuads(getEmitter(), blockView, pos, state, random, blockInfo::shouldCullSide);
		} catch (Throwable throwable) {
			CrashReport crashReport = CrashReport.forThrowable(throwable, "Tessellating block model - Indigo Renderer");
			CrashReportCategory crashReportSection = crashReport.addCategory("Block model being tessellated");
			CrashReportCategory.populateBlockDetails(crashReportSection, blockView, pos, state);
			throw new ReportedException(crashReport);
		} finally {
			blockInfo.release();
			matrices = null;
			this.vertexConsumers = null;
		}
	}
}
