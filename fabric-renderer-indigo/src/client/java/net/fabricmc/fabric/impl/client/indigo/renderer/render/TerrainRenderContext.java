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

import java.util.Arrays;
import java.util.function.Function;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoLuminanceFix;

/**
 * Used during section block buffering to invoke {@link BlockStateModel#emitQuads}.
 */
public class TerrainRenderContext extends AbstractTerrainRenderContext {
	public static final ThreadLocal<TerrainRenderContext> POOL = ThreadLocal.withInitial(TerrainRenderContext::new);

	private MatrixStack matrixStack;
	private Random random;
	private Function<RenderLayer, BufferBuilder> bufferFunc;

	public TerrainRenderContext() {
		overlay = OverlayTexture.DEFAULT_UV;
	}

	@Override
	protected LightDataProvider createLightDataProvider(BlockRenderInfo blockInfo) {
		return new LightDataCache(blockInfo);
	}

	@Override
	protected VertexConsumer getVertexConsumer(RenderLayer layer) {
		return bufferFunc.apply(layer);
	}

	public void prepare(BlockRenderView blockView, BlockPos sectionOrigin, MatrixStack matrixStack, Random random, Function<RenderLayer, BufferBuilder> bufferFunc) {
		blockInfo.prepareForWorld(blockView, true);
		((LightDataCache) lightDataProvider).prepare(sectionOrigin);

		this.matrixStack = matrixStack;
		this.random = random;
		this.bufferFunc = bufferFunc;
	}

	public void release() {
		matrices = null;
		matrixStack = null;
		random = null;
		bufferFunc = null;

		blockInfo.release();
	}

	/** Called from section builder hook. */
	public void bufferModel(BlockStateModel model, BlockState blockState, BlockPos blockPos) {
		matrixStack.push();

		try {
			matrixStack.translate(ChunkSectionPos.getLocalCoord(blockPos.getX()), ChunkSectionPos.getLocalCoord(blockPos.getY()), ChunkSectionPos.getLocalCoord(blockPos.getZ()));
			Vec3d offset = blockState.getModelOffset(blockPos);
			matrixStack.translate(offset.x, offset.y, offset.z);
			matrices = matrixStack.peek();

			random.setSeed(blockState.getRenderingSeed(blockPos));

			prepare(blockPos, blockState);
			model.emitQuads(getEmitter(), blockInfo.blockView, blockPos, blockState, random, blockInfo::shouldCullSide);
		} catch (Throwable throwable) {
			CrashReport crashReport = CrashReport.create(throwable, "Tessellating block in world - Indigo Renderer");
			CrashReportSection crashReportSection = crashReport.addElement("Block being tessellated");
			CrashReportSection.addBlockInfo(crashReportSection, blockInfo.blockView, blockPos, blockState);
			throw new CrashException(crashReport);
		} finally {
			matrixStack.pop();
		}
	}

	private static class LightDataCache implements LightDataProvider {
		// Since this context is only used during section building, we know ahead of time all positions for which data
		// may be requested by flat or smooth lighting, so we use an array instead of a map to cache that data, unlike
		// vanilla. Even though cache indices are positions and therefore 3D, the cache is 1D to maximize memory
		// locality.
		private final int[] lightCache = new int[18 * 18 * 18];
		private final float[] aoCache = new float[18 * 18 * 18];

		private final BlockRenderInfo blockInfo;
		private BlockPos sectionOrigin;

		LightDataCache(BlockRenderInfo blockInfo) {
			this.blockInfo = blockInfo;
		}

		private final WorldRenderer.BrightnessGetter lightGetter = (world, pos) -> {
			int cacheIndex = cacheIndex(pos);

			if (cacheIndex == -1) {
				return WorldRenderer.BrightnessGetter.DEFAULT.packedBrightness(world, pos);
			}

			int result = lightCache[cacheIndex];

			if (result == Integer.MAX_VALUE) {
				result = WorldRenderer.BrightnessGetter.DEFAULT.packedBrightness(world, pos);
				lightCache[cacheIndex] = result;
			}

			return result;
		};

		public void prepare(BlockPos sectionOrigin) {
			this.sectionOrigin = sectionOrigin;

			Arrays.fill(lightCache, Integer.MAX_VALUE);
			Arrays.fill(aoCache, Float.NaN);
		}

		@Override
		public int light(BlockPos pos, BlockState state) {
			return WorldRenderer.getLightmapCoordinates(lightGetter, blockInfo.blockView, state, pos);
		}

		@Override
		public float ao(BlockPos pos, BlockState state) {
			int cacheIndex = cacheIndex(pos);

			if (cacheIndex == -1) {
				return AoLuminanceFix.INSTANCE.apply(blockInfo.blockView, pos, state);
			}

			float result = aoCache[cacheIndex];

			if (Float.isNaN(result)) {
				result = AoLuminanceFix.INSTANCE.apply(blockInfo.blockView, pos, state);
				aoCache[cacheIndex] = result;
			}

			return result;
		}

		private int cacheIndex(BlockPos pos) {
			int localX = pos.getX() - (sectionOrigin.getX() - 1);

			if (localX < 0 || localX >= 18) {
				return -1;
			}

			int localY = pos.getY() - (sectionOrigin.getY() - 1);

			if (localY < 0 || localY >= 18) {
				return -1;
			}

			int localZ = pos.getZ() - (sectionOrigin.getZ() - 1);

			if (localZ < 0 || localZ >= 18) {
				return -1;
			}

			return localZ * 18 * 18 + localY * 18 + localX;
		}
	}
}
