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

import java.util.function.Function;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoCalculator;

/**
 * Used during terrain block buffering to invoke {@link BakedModel#emitBlockQuads}.
 */
public class TerrainRenderContext extends AbstractBlockRenderContext {
	public static final ThreadLocal<TerrainRenderContext> POOL = ThreadLocal.withInitial(TerrainRenderContext::new);

	private final ChunkRenderInfo chunkInfo = new ChunkRenderInfo();

	public TerrainRenderContext() {
		overlay = OverlayTexture.DEFAULT_UV;
		blockInfo.random = Random.create();
	}

	@Override
	protected AoCalculator createAoCalc(BlockRenderInfo blockInfo) {
		return new AoCalculator(blockInfo) {
			@Override
			public int light(BlockPos pos, BlockState state) {
				return chunkInfo.cachedBrightness(pos, state);
			}

			@Override
			public float ao(BlockPos pos, BlockState state) {
				return chunkInfo.cachedAoLevel(pos, state);
			}
		};
	}

	@Override
	protected VertexConsumer getVertexConsumer(RenderLayer layer) {
		return chunkInfo.getBuffer(layer);
	}

	public void prepare(ChunkRendererRegion blockView, Function<RenderLayer, BufferBuilder> bufferFunc) {
		chunkInfo.prepare(blockView, bufferFunc);
		blockInfo.prepareForWorld(blockView, true);
	}

	public void release() {
		chunkInfo.release();
		blockInfo.release();
	}

	/** Called from chunk renderer hook. */
	public void tessellateBlock(BlockState blockState, BlockPos blockPos, BakedModel model, MatrixStack matrixStack) {
		try {
			Vec3d offset = blockState.getModelOffset(blockPos);
			matrixStack.translate(offset.x, offset.y, offset.z);

			this.matrix = matrixStack.peek().getPositionMatrix();
			this.normalMatrix = matrixStack.peek().getNormalMatrix();

			blockInfo.recomputeSeed = true;

			aoCalc.clear();
			blockInfo.prepareForBlock(blockState, blockPos, model.useAmbientOcclusion());
			model.emitBlockQuads(getEmitter(), blockInfo.blockView, blockInfo.blockState, blockInfo.blockPos, blockInfo.randomSupplier, blockInfo::shouldCullSide);
		} catch (Throwable throwable) {
			CrashReport crashReport = CrashReport.create(throwable, "Tessellating block in world - Indigo Renderer");
			CrashReportSection crashReportSection = crashReport.addElement("Block being tessellated");
			CrashReportSection.addBlockInfo(crashReportSection, chunkInfo.blockView, blockPos, blockState);
			throw new CrashException(crashReport);
		}
	}
}
