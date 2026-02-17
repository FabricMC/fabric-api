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

import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jspecify.annotations.Nullable;

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

import net.fabricmc.fabric.api.client.renderer.v1.render.BlockMultiBufferSource;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoLuminanceFix;

/**
 * Used during terrain-like block buffering to invoke {@link BlockStateModel#emitQuads}.
 */
public class TerrainLikeRenderContext extends AbstractTerrainRenderContext {
	public static final ThreadLocal<TerrainLikeRenderContext> POOL = ThreadLocal.withInitial(TerrainLikeRenderContext::new);

	private final RandomSource random = RandomSource.createNewThreadLocalInstance();

	private BlockMultiBufferSource bufferSource;
	@Nullable
	private Predicate<ChunkSectionLayer> layerFilter;

	@Override
	protected LightDataProvider createLightDataProvider(BlockRenderInfo blockInfo) {
		// TODO: Use a cache whenever vanilla would use a cache (BrightnessCache.enabled)
		return new LightDataProvider() {
			@Override
			public int light(BlockPos pos, BlockState state) {
				return LevelRenderer.getLightCoords(LevelRenderer.BrightnessGetter.DEFAULT, blockInfo.level, state, pos);
			}

			@Override
			public float ao(BlockPos pos, BlockState state) {
				return AoLuminanceFix.INSTANCE.apply(blockInfo.level, pos, state);
			}
		};
	}

	@Override
	@Nullable
	protected VertexConsumer getVertexConsumer(ChunkSectionLayer layer) {
		if (layerFilter != null && !layerFilter.test(layer)) {
			return null;
		}

		return bufferSource.getBuffer(layer);
	}

	public void bufferModel(BlockAndTintGetter level, BlockStateModel model, BlockState state, BlockPos pos, PoseStack poseStack, BlockMultiBufferSource bufferSource, @Nullable Predicate<ChunkSectionLayer> layerFilter, boolean cull, long seed, int overlay) {
		try {
			Vec3 offset = state.getOffset(pos);
			poseStack.translate(offset.x, offset.y, offset.z);
			pose = poseStack.last();
			this.overlay = overlay;

			this.bufferSource = bufferSource;
			this.layerFilter = layerFilter;

			blockInfo.prepareForLevel(level, cull);
			random.setSeed(seed);

			prepare(pos, state);
			model.emitQuads(getEmitter(), level, pos, state, random, blockInfo::shouldCullSide);
		} catch (Throwable throwable) {
			CrashReport crashReport = CrashReport.forThrowable(throwable, "Tessellating block model - Indigo Renderer");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Block model being tessellated");
			CrashReportCategory.populateBlockDetails(crashReportCategory,
					level, pos, state);
			throw new ReportedException(crashReport);
		} finally {
			blockInfo.release();
			pose = null;
			this.bufferSource = null;
			this.layerFilter = null;
		}
	}
}
