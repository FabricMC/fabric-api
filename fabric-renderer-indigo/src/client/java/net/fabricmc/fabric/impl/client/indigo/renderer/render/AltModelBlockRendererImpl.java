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

import java.util.List;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadTransform;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.ShadeMode;
import net.fabricmc.fabric.api.client.renderer.v1.render.AltModelBlockRenderer;
import net.fabricmc.fabric.api.client.renderer.v1.render.ExtraLightCoordsUtil;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockTintsFactory;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoCalculator;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.FlatLighter;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.fabricmc.fabric.mixin.client.indigo.renderer.BlockModelLighterAccessor;

public class AltModelBlockRendererImpl implements AltModelBlockRenderer, QuadTransform {
	private final boolean ambientOcclusion;
	private final boolean cull;
	private final BlockColors blockColors;

	private final Predicate<@Nullable Direction> cullTest = this::shouldCullFace;
	private final AoCalculator aoCalc;
	private final FlatLighter flatLighter;

	private final RandomSource random = RandomSource.createThreadLocalInstance(0L);
	private final BlockPos.MutableBlockPos scratchPos = new BlockPos.MutableBlockPos();

	private int cacheValid;
	private int shouldCullFaceCache;

	private int tintCacheIndex = -1;
	private int tintCacheValue;
	private boolean tintSourcesInitialized;
	private final List<@Nullable BlockTintSource> tintSources = new ObjectArrayList<>();
	private final IntList computedTintValues = new IntArrayList();

	private final Vector3f offset = new Vector3f();
	private BlockAndTintGetter level;
	private BlockPos pos;
	private BlockState blockState;
	private boolean defaultAo;

	public AltModelBlockRendererImpl(boolean ambientOcclusion, boolean cull, BlockColors blockColors) {
		this.ambientOcclusion = ambientOcclusion;
		this.cull = cull;
		this.blockColors = blockColors;

		BlockModelLighter.Cache lightCache = BlockModelLighterAccessor.fabric_getCACHE().get();
		aoCalc = new AoCalculator(lightCache);
		flatLighter = new FlatLighter(lightCache);
	}

	@Override
	public void tesselateBlock(QuadEmitter output, float x, float y, float z, BlockAndTintGetter level, BlockPos pos, BlockState blockState, BlockStateModel model, long seed) {
		Vec3 offset = blockState.getOffset(pos);
		this.offset.set(x + offset.x, y + offset.y, z + offset.z);
		this.level = level;
		this.pos = pos;
		this.blockState = blockState;
		defaultAo = ambientOcclusion && blockState.getLightEmission() == 0;

		cacheValid = 0;
		shouldCullFaceCache = 0;
		aoCalc.prepare(level, blockState, pos);

		random.setSeed(seed);
		output.clear();
		output.pushTransform(this);

		try {
			model.emitQuads(output, level, pos, blockState, random, cull ? cullTest : _ -> false);
		} finally {
			output.popTransform();

			this.level = null;
			aoCalc.clear();
			resetTintCache();
		}
	}

	@Override
	public boolean transform(MutableQuadView quad) {
		if (cull && shouldCullFace(quad.cullFace())) {
			return false;
		}

		shadeQuad((MutableQuadViewImpl) quad, ambientOcclusion && quad.ambientOcclusion().orElse(defaultAo), quad.emissive(), quad.shadeMode() == ShadeMode.VANILLA);
		tintQuad(quad);
		quad.translate(offset.x, offset.y, offset.z);
		return true;
	}

	private boolean shouldCullFace(final @Nullable Direction direction) {
		if (direction == null) {
			return false;
		}

		int cacheMask = 1 << direction.ordinal();

		if ((cacheValid & cacheMask) == 0) {
			cacheValid |= cacheMask;
			BlockState neighborState = level.getBlockState(scratchPos.setWithOffset(pos, direction));

			if (!Block.shouldRenderFace(blockState, neighborState, direction)) {
				shouldCullFaceCache |= cacheMask;
				return true;
			} else {
				return false;
			}
		} else {
			return (shouldCullFaceCache & cacheMask) != 0;
		}
	}

	private void shadeQuad(MutableQuadViewImpl quad, boolean ao, boolean emissive, boolean vanillaShade) {
		// routines below have a bit of copy-paste code reuse to avoid conditional execution inside a hot loop
		if (ao) {
			aoCalc.compute(quad, vanillaShade);

			if (emissive) {
				for (int i = 0; i < 4; i++) {
					quad.color(i, ARGB.scaleRGB(quad.color(i), aoCalc.ao[i]));
					quad.lightmap(i, LightCoordsUtil.FULL_BRIGHT);
				}
			} else {
				for (int i = 0; i < 4; i++) {
					quad.color(i, ARGB.scaleRGB(quad.color(i), aoCalc.ao[i]));
					quad.lightmap(i, ExtraLightCoordsUtil.smoothMax(quad.lightmap(i), aoCalc.light[i]));
				}
			}
		} else {
			if (emissive) {
				quad.lightmap(LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT, LightCoordsUtil.FULL_BRIGHT);
			} else {
				quad.minLightmap(flatLighter.light(level, blockState, pos, quad));
			}

			flatLighter.applyDirectionalBrightness(level.cardinalLighting(), quad, vanillaShade);
		}
	}

	private void tintQuad(MutableQuadView quad) {
		int tintIndex = quad.tintIndex();

		if (tintIndex != -1) {
			quad.multiplyColor(getTintColor(level, blockState, pos, tintIndex));
		}
	}

	private void configureTintCache(final BlockState blockState,
			final BlockAndTintGetter level,
			final BlockPos pos) {
		List<BlockTintSource> tintSources = blockColors.getTintSources(blockState);
		int tintSourceCount = tintSources.size();

		if (tintSourceCount > 0) {
			this.tintSources.addAll(tintSources);

			for (int i = 0; i < tintSourceCount; ++i) {
				computedTintValues.add(-1);
			}
		} else {
			final BlockTintsFactory factory = BlockColorRegistry.getFactory(blockState);

			if (factory != null) {
				factory.collect(blockState, level, pos, computedTintValues);
			}

			if (!this.computedTintValues.isEmpty()) {
				for (int i = 0; i < this.computedTintValues.size(); i++) {
					this.tintSources.add(null);
				}
			}
		}
	}

	private int computeTintColor(final BlockAndTintGetter level, final BlockState state, final BlockPos pos, final int tintIndex) {
		if (!tintSourcesInitialized) {
			configureTintCache(state, level, pos);
			tintSourcesInitialized = true;
		}

		if (tintIndex >= tintSources.size()) {
			return -1;
		} else {
			BlockTintSource tintSource = tintSources.set(tintIndex, null);

			if (tintSource != null) {
				int computedTintValue = tintSource.colorInWorld(state, level, pos);
				computedTintValues.set(tintIndex, computedTintValue);
				return computedTintValue;
			} else {
				return computedTintValues.getInt(tintIndex);
			}
		}
	}

	private int getTintColor(final BlockAndTintGetter level, final BlockState state, final BlockPos pos, final int tintIndex) {
		if (tintCacheIndex == tintIndex) {
			return tintCacheValue;
		} else {
			int tintColor = computeTintColor(level, state, pos, tintIndex);
			tintCacheIndex = tintIndex;
			tintCacheValue = tintColor;
			return tintColor;
		}
	}

	private void resetTintCache() {
		tintCacheIndex = -1;

		if (tintSourcesInitialized) {
			tintSources.clear();
			computedTintValues.clear();
			tintSourcesInitialized = false;
		}
	}
}
