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

import net.minecraft.block.BlockState;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.fabric.api.renderer.v1.render.BlockVertexConsumerProvider;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoLuminanceFix;

/**
 * Used during terrain-like block buffering to invoke {@link BlockStateModel#emitQuads}.
 */
public class TerrainLikeRenderContext extends AbstractTerrainRenderContext {
	public static final ThreadLocal<TerrainLikeRenderContext> POOL = ThreadLocal.withInitial(TerrainLikeRenderContext::new);

	private final Random random = Random.createLocal();

	private BlockVertexConsumerProvider vertexConsumers;

	@Override
	protected LightDataProvider createLightDataProvider(BlockRenderInfo blockInfo) {
		// TODO: Use a cache whenever vanilla would use a cache (BrightnessCache.enabled)
		return new LightDataProvider() {
			@Override
			public int light(BlockPos pos, BlockState state) {
				return WorldRenderer.getLightmapCoordinates(WorldRenderer.BrightnessGetter.DEFAULT, blockInfo.blockView, state, pos);
			}

			@Override
			public float ao(BlockPos pos, BlockState state) {
				return AoLuminanceFix.INSTANCE.apply(blockInfo.blockView, pos, state);
			}
		};
	}

	@Override
	protected VertexConsumer getVertexConsumer(BlockRenderLayer layer) {
		return vertexConsumers.getBuffer(layer);
	}

	public void bufferModel(BlockRenderView blockView, BlockStateModel model, BlockState state, BlockPos pos, MatrixStack matrixStack, BlockVertexConsumerProvider vertexConsumers, boolean cull, long seed, int overlay) {
		try {
			Vec3d offset = state.getModelOffset(pos);
			matrixStack.translate(offset.x, offset.y, offset.z);
			matrices = matrixStack.peek();
			this.overlay = overlay;

			this.vertexConsumers = vertexConsumers;

			blockInfo.prepareForWorld(blockView, cull);
			random.setSeed(seed);

			prepare(pos, state);
			model.emitQuads(getEmitter(), blockView, pos, state, random, blockInfo::shouldCullSide);
		} catch (Throwable throwable) {
			CrashReport crashReport = CrashReport.create(throwable, "Tessellating block model - Indigo Renderer");
			CrashReportSection crashReportSection = crashReport.addElement("Block model being tessellated");
			CrashReportSection.addBlockInfo(crashReportSection, blockView, pos, state);
			throw new CrashException(crashReport);
		} finally {
			blockInfo.release();
			matrices = null;
			this.vertexConsumers = null;
		}
	}
}
