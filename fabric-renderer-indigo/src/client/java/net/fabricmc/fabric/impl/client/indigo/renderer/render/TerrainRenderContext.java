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

import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
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

import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoCalculator;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoLuminanceFix;

/**
 * Used during section block buffering to invoke {@link BlockStateModel#emitQuads}.
 */
public class TerrainRenderContext extends AbstractTerrainRenderContext {
	public static final ThreadLocal<TerrainRenderContext> POOL = ThreadLocal.withInitial(TerrainRenderContext::new);

	// TODO: Allow TerrainLikeRenderContext to also cache these values, including for flat lighting (possible as of
	//  1.21.5 rc1), and respect the setting of the vanilla brightness cache.
	//  This context (TerrainRenderContext) should use an array (or arrays) of length 18^3 instead of maps to cache
	//  these values since it is known which positions they may be computed for.
	/**
	 * Serves same function as brightness cache in Mojang's AO calculator,
	 * with some differences as follows...
	 *
	 * <ul><li>Mojang limits the cache to 100 values.
	 * However, a render chunk only has 16^3 blocks in it, and the cache is cleared every chunk.
	 * For performance and simplicity, we just let map grow to the size of the render chunk.
	 *
	 * <li>The Mojang cache is a separate threadlocal with a threadlocal boolean to
	 * enable and disable. Cache clearing happens on disable. There's no use case for
	 * us when the cache needs to be disabled (and no apparent case in Mojang's code either)
	 * so we simply clear the cache at the start of each new chunk. It is also
	 * not a threadlocal because it's held within a threadlocal TerrainRenderContext.</ul>
	 */
	private final Long2IntOpenHashMap lightCache = new Long2IntOpenHashMap();
	private final Long2FloatOpenHashMap aoCache = new Long2FloatOpenHashMap();

	private MatrixStack matrixStack;
	private Random random;
	private Function<RenderLayer, BufferBuilder> bufferFunc;

	public TerrainRenderContext() {
		lightCache.defaultReturnValue(Integer.MAX_VALUE);
		aoCache.defaultReturnValue(Float.MAX_VALUE);

		overlay = OverlayTexture.DEFAULT_UV;
	}

	@Override
	protected AoCalculator createAoCalc(BlockRenderInfo blockInfo) {
		return new AoCalculator(blockInfo) {
			@Override
			public int light(BlockPos pos, BlockState state) {
				long key = pos.asLong();
				int result = lightCache.get(key);

				if (result == Integer.MAX_VALUE) {
					result = AoCalculator.getLightmapCoordinates(blockInfo.blockView, state, pos);
					lightCache.put(key, result);
				}

				return result;
			}

			@Override
			public float ao(BlockPos pos, BlockState state) {
				long key = pos.asLong();
				float result = aoCache.get(key);

				if (result == Float.MAX_VALUE) {
					result = AoLuminanceFix.INSTANCE.apply(blockInfo.blockView, pos, state);
					aoCache.put(key, result);
				}

				return result;
			}
		};
	}

	@Override
	protected VertexConsumer getVertexConsumer(RenderLayer layer) {
		return bufferFunc.apply(layer);
	}

	public void prepare(BlockRenderView blockView, MatrixStack matrixStack, Random random, Function<RenderLayer, BufferBuilder> bufferFunc) {
		blockInfo.prepareForWorld(blockView, true);

		this.matrixStack = matrixStack;
		this.random = random;
		this.bufferFunc = bufferFunc;

		lightCache.clear();
		aoCache.clear();
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
}
