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

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoLuminanceFix;

/**
 * Used during section block buffering to invoke {@link BlockStateModel#emitQuads}.
 */
public class TerrainRenderContext extends AbstractTerrainRenderContext {
	public static final ThreadLocal<TerrainRenderContext> POOL = ThreadLocal.withInitial(TerrainRenderContext::new);

	private PoseStack matrixStack;
	private RandomSource random;
	private Function<ChunkSectionLayer, BufferBuilder> bufferFunc;

	public TerrainRenderContext() {
		overlay = OverlayTexture.NO_OVERLAY;
	}

	@Override
	protected LightDataProvider createLightDataProvider(BlockRenderInfo blockInfo) {
		return new LightDataCache(blockInfo);
	}

	@Override
	protected VertexConsumer getVertexConsumer(ChunkSectionLayer layer) {
		return bufferFunc.apply(layer);
	}

	public void prepare(BlockAndTintGetter blockView, BlockPos sectionOrigin, PoseStack matrixStack, RandomSource random, Function<ChunkSectionLayer, BufferBuilder> bufferFunc) {
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
		matrixStack.pushPose();

		try {
			matrixStack.translate(SectionPos.sectionRelative(blockPos.getX()), SectionPos.sectionRelative(blockPos.getY()), SectionPos.sectionRelative(blockPos.getZ()));
			Vec3 offset = blockState.getOffset(blockPos);
			matrixStack.translate(offset.x, offset.y, offset.z);
			matrices = matrixStack.last();

			random.setSeed(blockState.getSeed(blockPos));

			prepare(blockPos, blockState);
			model.emitQuads(getEmitter(), blockInfo.blockView, blockPos, blockState, random, blockInfo::shouldCullSide);
		} catch (Throwable throwable) {
			CrashReport crashReport = CrashReport.forThrowable(throwable, "Tessellating block in world - Indigo Renderer");
			CrashReportCategory crashReportSection = crashReport.addCategory("Block being tessellated");
			CrashReportCategory.populateBlockDetails(crashReportSection, blockInfo.blockView, blockPos, blockState);
			throw new ReportedException(crashReport);
		} finally {
			matrixStack.popPose();
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

		private final LevelRenderer.BrightnessGetter lightGetter = (world, pos) -> {
			int cacheIndex = cacheIndex(pos);

			if (cacheIndex == -1) {
				return LevelRenderer.BrightnessGetter.DEFAULT.packedBrightness(world, pos);
			}

			int result = lightCache[cacheIndex];

			if (result == Integer.MAX_VALUE) {
				result = LevelRenderer.BrightnessGetter.DEFAULT.packedBrightness(world, pos);
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
			return LevelRenderer.getLightColor(lightGetter, blockInfo.blockView, state, pos);
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
