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

package net.fabricmc.fabric.impl.client.indigo.renderer.aocalc;

import static net.fabricmc.fabric.impl.client.indigo.renderer.helper.GeometryHelper.AXIS_ALIGNED_FLAG;
import static net.fabricmc.fabric.impl.client.indigo.renderer.helper.GeometryHelper.CUBIC_FLAG;
import static net.fabricmc.fabric.impl.client.indigo.renderer.helper.GeometryHelper.LIGHT_FACE_FLAG;
import static net.minecraft.util.math.Direction.DOWN;
import static net.minecraft.util.math.Direction.EAST;
import static net.minecraft.util.math.Direction.NORTH;
import static net.minecraft.util.math.Direction.SOUTH;
import static net.minecraft.util.math.Direction.UP;
import static net.minecraft.util.math.Direction.WEST;

import java.util.BitSet;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.LightType;

import net.fabricmc.fabric.impl.client.indigo.Indigo;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoFace.WeightFunction;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.QuadViewImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;

/**
 * Adaptation of inner, non-static class in BlockModelRenderer that serves same purpose.
 */
public abstract class AoCalculator {
	/**
	 * Vanilla models with cubic quads have vertices in a certain order, which allows
	 * us to map them using a lookup. Adapted from enum in vanilla AoCalculator.
	 */
	private static final int[][] VERTEX_MAP = new int[6][4];
	static {
		VERTEX_MAP[DOWN.getId()] = new int[] { 0, 1, 2, 3 };
		VERTEX_MAP[UP.getId()] = new int[] { 2, 3, 0, 1 };
		VERTEX_MAP[NORTH.getId()] = new int[] { 3, 0, 1, 2 };
		VERTEX_MAP[SOUTH.getId()] = new int[] { 0, 1, 2, 3 };
		VERTEX_MAP[WEST.getId()] = new int[] { 3, 0, 1, 2 };
		VERTEX_MAP[EAST.getId()] = new int[] { 1, 2, 3, 0 };
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(AoCalculator.class);

	private final BlockModelRenderer.AmbientOcclusionCalculator vanillaCalc;
	private final BlockPos.Mutable lightPos = new BlockPos.Mutable();
	private final BlockPos.Mutable searchPos = new BlockPos.Mutable();
	protected final BlockRenderInfo blockInfo;

	public abstract int light(BlockPos pos, BlockState state);

	public abstract float ao(BlockPos pos, BlockState state);

	/** caches results of {@link #computeFace(Direction, boolean, boolean)} for the current block. */
	private final AoFaceData[] faceData = new AoFaceData[24];

	/** indicates which elements of {@link #faceData} have been computed for the current block. */
	private int completionFlags = 0;

	/** holds per-corner weights - used locally to avoid new allocation. */
	private final float[] w = new float[4];

	// outputs
	public final float[] ao = new float[4];
	public final int[] light = new int[4];

	public AoCalculator(BlockRenderInfo blockInfo) {
		this.blockInfo = blockInfo;
		this.vanillaCalc = new BlockModelRenderer.AmbientOcclusionCalculator();

		for (int i = 0; i < 24; i++) {
			faceData[i] = new AoFaceData();
		}
	}

	/** call at start of each new block. */
	public void clear() {
		completionFlags = 0;
	}

	public void compute(QuadViewImpl quad, boolean vanillaShade) {
		final AoConfig config = Indigo.AMBIENT_OCCLUSION_MODE;

		switch (config) {
		case VANILLA -> calcVanilla(quad);
		case EMULATE -> calcFastVanilla(quad);
		case HYBRID -> {
			if (vanillaShade) {
				calcFastVanilla(quad);
			} else {
				calcEnhanced(quad);
			}
		}
		case ENHANCED -> calcEnhanced(quad);
		}

		if (Indigo.DEBUG_COMPARE_LIGHTING && vanillaShade && (config == AoConfig.EMULATE || config == AoConfig.HYBRID)) {
			float[] vanillaAo = new float[4];
			int[] vanillaLight = new int[4];
			calcVanilla(quad, vanillaAo, vanillaLight);

			for (int i = 0; i < 4; i++) {
				if (light[i] != vanillaLight[i] || !MathHelper.approximatelyEquals(ao[i], vanillaAo[i])) {
					LOGGER.info(String.format("Mismatch for %s @ %s", blockInfo.blockState.toString(), blockInfo.blockPos.toString()));
					LOGGER.info(String.format("Flags = %d, LightFace = %s", quad.geometryFlags(), quad.lightFace().toString()));
					LOGGER.info(String.format("    Old Multiplier: %.2f, %.2f, %.2f, %.2f", vanillaAo[0], vanillaAo[1], vanillaAo[2], vanillaAo[3]));
					LOGGER.info(String.format("    New Multiplier: %.2f, %.2f, %.2f, %.2f", ao[0], ao[1], ao[2], ao[3]));
					LOGGER.info(String.format("    Old Brightness: %s, %s, %s, %s", Integer.toHexString(vanillaLight[0]), Integer.toHexString(vanillaLight[1]), Integer.toHexString(vanillaLight[2]), Integer.toHexString(vanillaLight[3])));
					LOGGER.info(String.format("    New Brightness: %s, %s, %s, %s", Integer.toHexString(light[0]), Integer.toHexString(light[1]), Integer.toHexString(light[2]), Integer.toHexString(light[3])));
					break;
				}
			}
		}
	}

	private void calcVanilla(QuadViewImpl quad) {
		calcVanilla(quad, ao, light);
	}

	// These are what vanilla AO calc wants, per its usage in vanilla code
	// Because this instance is effectively thread-local, we preserve instances
	// to avoid making a new allocation each call.
	private final float[] vanillaAoData = new float[Direction.values().length * 2];
	private final BitSet vanillaAoFlags = new BitSet(3);
	private final int[] vertexData = new int[EncodingFormat.QUAD_STRIDE];

	private void calcVanilla(QuadViewImpl quad, float[] aoDest, int[] lightDest) {
		vanillaAoFlags.clear();
		final Direction lightFace = quad.lightFace();
		quad.toVanilla(vertexData, 0);

		VanillaAoHelper.getQuadDimensions(blockInfo.blockView, blockInfo.blockState, blockInfo.blockPos, vertexData, lightFace, vanillaAoData, vanillaAoFlags);
		vanillaCalc.apply(blockInfo.blockView, blockInfo.blockState, blockInfo.blockPos, lightFace, vanillaAoData, vanillaAoFlags, quad.hasShade());

		System.arraycopy(vanillaCalc.brightness, 0, aoDest, 0, 4);
		System.arraycopy(vanillaCalc.light, 0, lightDest, 0, 4);
	}

	private void calcFastVanilla(QuadViewImpl quad) {
		int flags = quad.geometryFlags();

		// force to block face if shape is full cube - matches vanilla logic
		if ((flags & LIGHT_FACE_FLAG) == 0 && (flags & AXIS_ALIGNED_FLAG) != 0 && blockInfo.blockState.isFullCube(blockInfo.blockView, blockInfo.blockPos)) {
			flags |= LIGHT_FACE_FLAG;
		}

		if ((flags & CUBIC_FLAG) == 0) {
			vanillaPartialFace(quad, quad.lightFace(), (flags & LIGHT_FACE_FLAG) != 0, quad.hasShade());
		} else {
			vanillaFullFace(quad, quad.lightFace(), (flags & LIGHT_FACE_FLAG) != 0, quad.hasShade());
		}
	}

	private void calcEnhanced(QuadViewImpl quad) {
		switch (quad.geometryFlags()) {
		case AXIS_ALIGNED_FLAG | CUBIC_FLAG | LIGHT_FACE_FLAG:
		case AXIS_ALIGNED_FLAG | LIGHT_FACE_FLAG:
			vanillaPartialFace(quad, quad.lightFace(), true, quad.hasShade());
			break;

		case AXIS_ALIGNED_FLAG | CUBIC_FLAG:
		case AXIS_ALIGNED_FLAG:
			blendedPartialFace(quad, quad.lightFace(), quad.hasShade());
			break;

		default:
			irregularFace(quad, quad.hasShade());
			break;
		}
	}

	private void vanillaFullFace(QuadViewImpl quad, Direction lightFace, boolean isOnLightFace, boolean shade) {
		computeFace(lightFace, isOnLightFace, shade).toArray(ao, light, VERTEX_MAP[lightFace.getId()]);
	}

	private void vanillaPartialFace(QuadViewImpl quad, Direction lightFace, boolean isOnLightFace, boolean shade) {
		AoFaceData faceData = computeFace(lightFace, isOnLightFace, shade);
		final WeightFunction wFunc = AoFace.get(lightFace).weightFunc;
		final float[] w = this.w;

		for (int i = 0; i < 4; i++) {
			wFunc.apply(quad, i, w);
			light[i] = faceData.weightedCombinedLight(w);
			ao[i] = faceData.weigtedAo(w);
		}
	}

	/** used in {@link #blendedInsetFace(QuadViewImpl quad, int vertexIndex, Direction lightFace, boolean shade)} as return variable to avoid new allocation. */
	AoFaceData tmpFace = new AoFaceData();

	/** Returns linearly interpolated blend of outer and inner face based on depth of vertex in face. */
	private AoFaceData blendedInsetFace(QuadViewImpl quad, int vertexIndex, Direction lightFace, boolean shade) {
		final float w1 = AoFace.get(lightFace).depthFunc.apply(quad, vertexIndex);
		final float w0 = 1 - w1;
		return AoFaceData.weightedMean(computeFace(lightFace, true, shade), w0, computeFace(lightFace, false, shade), w1, tmpFace);
	}

	/**
	 * Like {@link #blendedInsetFace(QuadViewImpl quad, int vertexIndex, Direction lightFace, boolean shade)} but optimizes if depth is 0 or 1.
	 * Used for irregular faces when depth varies by vertex to avoid unneeded interpolation.
	 */
	private AoFaceData gatherInsetFace(QuadViewImpl quad, int vertexIndex, Direction lightFace, boolean shade) {
		final float w1 = AoFace.get(lightFace).depthFunc.apply(quad, vertexIndex);

		if (MathHelper.approximatelyEquals(w1, 0)) {
			return computeFace(lightFace, true, shade);
		} else if (MathHelper.approximatelyEquals(w1, 1)) {
			return computeFace(lightFace, false, shade);
		} else {
			final float w0 = 1 - w1;
			return AoFaceData.weightedMean(computeFace(lightFace, true, shade), w0, computeFace(lightFace, false, shade), w1, tmpFace);
		}
	}

	private void blendedPartialFace(QuadViewImpl quad, Direction lightFace, boolean shade) {
		AoFaceData faceData = blendedInsetFace(quad, 0, lightFace, shade);
		final WeightFunction wFunc = AoFace.get(lightFace).weightFunc;

		for (int i = 0; i < 4; i++) {
			wFunc.apply(quad, i, w);
			light[i] = faceData.weightedCombinedLight(w);
			ao[i] = faceData.weigtedAo(w);
		}
	}

	/** used exclusively in irregular face to avoid new heap allocations each call. */
	private final Vector3f vertexNormal = new Vector3f();

	private void irregularFace(QuadViewImpl quad, boolean shade) {
		final Vector3fc faceNorm = quad.faceNormal();
		Vector3fc normal;
		final float[] w = this.w;
		final float[] aoResult = this.ao;
		final int[] lightResult = this.light;

		for (int i = 0; i < 4; i++) {
			normal = quad.hasNormal(i) ? quad.copyNormal(i, vertexNormal) : faceNorm;
			float ao = 0, sky = 0, block = 0, maxAo = 0;
			int maxSky = 0, maxBlock = 0;

			final float x = normal.x();

			if (!MathHelper.approximatelyEquals(0f, x)) {
				final Direction face = x > 0 ? Direction.EAST : Direction.WEST;
				final AoFaceData fd = gatherInsetFace(quad, i, face, shade);
				AoFace.get(face).weightFunc.apply(quad, i, w);
				final float n = x * x;
				final float a = fd.weigtedAo(w);
				final int s = fd.weigtedSkyLight(w);
				final int b = fd.weigtedBlockLight(w);
				ao += n * a;
				sky += n * s;
				block += n * b;
				maxAo = a;
				maxSky = s;
				maxBlock = b;
			}

			final float y = normal.y();

			if (!MathHelper.approximatelyEquals(0f, y)) {
				final Direction face = y > 0 ? Direction.UP : Direction.DOWN;
				final AoFaceData fd = gatherInsetFace(quad, i, face, shade);
				AoFace.get(face).weightFunc.apply(quad, i, w);
				final float n = y * y;
				final float a = fd.weigtedAo(w);
				final int s = fd.weigtedSkyLight(w);
				final int b = fd.weigtedBlockLight(w);
				ao += n * a;
				sky += n * s;
				block += n * b;
				maxAo = Math.max(maxAo, a);
				maxSky = Math.max(maxSky, s);
				maxBlock = Math.max(maxBlock, b);
			}

			final float z = normal.z();

			if (!MathHelper.approximatelyEquals(0f, z)) {
				final Direction face = z > 0 ? Direction.SOUTH : Direction.NORTH;
				final AoFaceData fd = gatherInsetFace(quad, i, face, shade);
				AoFace.get(face).weightFunc.apply(quad, i, w);
				final float n = z * z;
				final float a = fd.weigtedAo(w);
				final int s = fd.weigtedSkyLight(w);
				final int b = fd.weigtedBlockLight(w);
				ao += n * a;
				sky += n * s;
				block += n * b;
				maxAo = Math.max(maxAo, a);
				maxSky = Math.max(maxSky, s);
				maxBlock = Math.max(maxBlock, b);
			}

			aoResult[i] = (ao + maxAo) * 0.5f;
			lightResult[i] = (((int) ((sky + maxSky) * 0.5f) & 0xF0) << 16) | ((int) ((block + maxBlock) * 0.5f) & 0xF0);
		}
	}

	private AoFaceData computeFace(Direction lightFace, boolean isOnBlockFace, boolean shade) {
		final int faceDataIndex = shade ? (isOnBlockFace ? lightFace.getId() : lightFace.getId() + 6) : (isOnBlockFace ? lightFace.getId() + 12 : lightFace.getId() + 18);
		final int mask = 1 << faceDataIndex;
		final AoFaceData result = faceData[faceDataIndex];

		if ((completionFlags & mask) == 0) {
			completionFlags |= mask;
			computeFace(result, lightFace, isOnBlockFace, shade);
		}

		return result;
	}

	/**
	 * Computes smoothed brightness and Ao shading for four corners of a block face.
	 * Outer block face is what you normally see and what you get when the second
	 * parameter is true. Inner is light *within* the block and usually darker.
	 * It is blended with the outer face for inset surfaces, but is also used directly
	 * in vanilla logic for some blocks that aren't full opaque cubes.
	 * Except for parameterization, the logic itself is practically identical to vanilla.
	 */
	private void computeFace(AoFaceData result, Direction lightFace, boolean isOnBlockFace, boolean shade) {
		final BlockRenderView world = blockInfo.blockView;
		final BlockPos pos = blockInfo.blockPos;
		final BlockState blockState = blockInfo.blockState;
		final BlockPos.Mutable lightPos = this.lightPos;
		final BlockPos.Mutable searchPos = this.searchPos;
		BlockState searchState;

		if (isOnBlockFace) {
			lightPos.set(pos, lightFace);
		} else {
			lightPos.set(pos);
		}

		AoFace aoFace = AoFace.get(lightFace);

		// Vanilla was further offsetting the positions for opaque block checks in the
		// direction of the light face, but it was actually mis-sampling and causing
		// visible artifacts in certain situations

		searchPos.set(lightPos, aoFace.neighbors[0]);
		searchState = world.getBlockState(searchPos);
		final int light0 = light(searchPos, searchState);
		final float ao0 = ao(searchPos, searchState);
		final boolean em0 = hasEmissiveLighting(world, searchPos, searchState);

		if (!Indigo.FIX_SMOOTH_LIGHTING_OFFSET) {
			searchPos.move(lightFace);
			searchState = world.getBlockState(searchPos);
		}

		final boolean isClear0 = !searchState.shouldBlockVision(world, searchPos) || searchState.getOpacity() == 0;

		searchPos.set(lightPos, aoFace.neighbors[1]);
		searchState = world.getBlockState(searchPos);
		final int light1 = light(searchPos, searchState);
		final float ao1 = ao(searchPos, searchState);
		final boolean em1 = hasEmissiveLighting(world, searchPos, searchState);

		if (!Indigo.FIX_SMOOTH_LIGHTING_OFFSET) {
			searchPos.move(lightFace);
			searchState = world.getBlockState(searchPos);
		}

		final boolean isClear1 = !searchState.shouldBlockVision(world, searchPos) || searchState.getOpacity() == 0;

		searchPos.set(lightPos, aoFace.neighbors[2]);
		searchState = world.getBlockState(searchPos);
		final int light2 = light(searchPos, searchState);
		final float ao2 = ao(searchPos, searchState);
		final boolean em2 = hasEmissiveLighting(world, searchPos, searchState);

		if (!Indigo.FIX_SMOOTH_LIGHTING_OFFSET) {
			searchPos.move(lightFace);
			searchState = world.getBlockState(searchPos);
		}

		final boolean isClear2 = !searchState.shouldBlockVision(world, searchPos) || searchState.getOpacity() == 0;

		searchPos.set(lightPos, aoFace.neighbors[3]);
		searchState = world.getBlockState(searchPos);
		final int light3 = light(searchPos, searchState);
		final float ao3 = ao(searchPos, searchState);
		final boolean em3 = hasEmissiveLighting(world, searchPos, searchState);

		if (!Indigo.FIX_SMOOTH_LIGHTING_OFFSET) {
			searchPos.move(lightFace);
			searchState = world.getBlockState(searchPos);
		}

		final boolean isClear3 = !searchState.shouldBlockVision(world, searchPos) || searchState.getOpacity() == 0;

		// c = corner - values at corners of face
		int cLight0, cLight1, cLight2, cLight3;
		float cAo0, cAo1, cAo2, cAo3;
		boolean cEm0, cEm1, cEm2, cEm3;

		// If neighbors on both sides of the corner are opaque, then apparently we use the light/shade
		// from one of the sides adjacent to the corner.  If either neighbor is clear (no light subtraction)
		// then we use values from the outwardly diagonal corner. (outwardly = position is one more away from light face)
		if (!isClear2 && !isClear0) {
			cAo0 = ao0;
			cLight0 = light0;
			cEm0 = em0;
		} else {
			searchPos.set(lightPos).move(aoFace.neighbors[0]).move(aoFace.neighbors[2]);
			searchState = world.getBlockState(searchPos);
			cAo0 = ao(searchPos, searchState);
			cLight0 = light(searchPos, searchState);
			cEm0 = hasEmissiveLighting(world, searchPos, searchState);
		}

		if (!isClear3 && !isClear0) {
			cAo1 = ao0;
			cLight1 = light0;
			cEm1 = em0;
		} else {
			searchPos.set(lightPos).move(aoFace.neighbors[0]).move(aoFace.neighbors[3]);
			searchState = world.getBlockState(searchPos);
			cAo1 = ao(searchPos, searchState);
			cLight1 = light(searchPos, searchState);
			cEm1 = hasEmissiveLighting(world, searchPos, searchState);
		}

		if (!isClear2 && !isClear1) {
			cAo2 = ao1;
			cLight2 = light1;
			cEm2 = em1;
		} else {
			searchPos.set(lightPos).move(aoFace.neighbors[1]).move(aoFace.neighbors[2]);
			searchState = world.getBlockState(searchPos);
			cAo2 = ao(searchPos, searchState);
			cLight2 = light(searchPos, searchState);
			cEm2 = hasEmissiveLighting(world, searchPos, searchState);
		}

		if (!isClear3 && !isClear1) {
			cAo3 = ao1;
			cLight3 = light1;
			cEm3 = em1;
		} else {
			searchPos.set(lightPos).move(aoFace.neighbors[1]).move(aoFace.neighbors[3]);
			searchState = world.getBlockState(searchPos);
			cAo3 = ao(searchPos, searchState);
			cLight3 = light(searchPos, searchState);
			cEm3 = hasEmissiveLighting(world, searchPos, searchState);
		}

		// If on block face or neighbor isn't occluding, "center" will be neighbor brightness
		// Doesn't use light pos because logic not based solely on this block's geometry
		int lightCenter;
		boolean emCenter;
		searchPos.set(pos, lightFace);
		searchState = world.getBlockState(searchPos);

		if (isOnBlockFace || !searchState.isOpaqueFullCube()) {
			lightCenter = light(searchPos, searchState);
			emCenter = hasEmissiveLighting(world, searchPos, searchState);
		} else {
			lightCenter = light(pos, blockState);
			emCenter = hasEmissiveLighting(world, pos, blockState);
		}

		float aoCenter = ao(lightPos, world.getBlockState(lightPos));
		float worldBrightness = world.getBrightness(lightFace, shade);

		result.a0 = ((ao3 + ao0 + cAo1 + aoCenter) * 0.25F) * worldBrightness;
		result.a1 = ((ao2 + ao0 + cAo0 + aoCenter) * 0.25F) * worldBrightness;
		result.a2 = ((ao2 + ao1 + cAo2 + aoCenter) * 0.25F) * worldBrightness;
		result.a3 = ((ao3 + ao1 + cAo3 + aoCenter) * 0.25F) * worldBrightness;

		result.l0(meanBrightness(light3, light0, cLight1, lightCenter, em3, em0, cEm1, emCenter));
		result.l1(meanBrightness(light2, light0, cLight0, lightCenter, em2, em0, cEm0, emCenter));
		result.l2(meanBrightness(light2, light1, cLight2, lightCenter, em2, em1, cEm2, emCenter));
		result.l3(meanBrightness(light3, light1, cLight3, lightCenter, em3, em1, cEm3, emCenter));
	}

	public static int getLightmapCoordinates(BlockRenderView world, BlockState state, BlockPos pos) {
		if (Indigo.FIX_EMISSIVE_LIGHTING) {
			// Same as WorldRenderer.getLightmapCoordinates but without the hasEmissiveLighting check.
			// We don't want emissive lighting to influence the minimum lightmap in a quad,
			// so when the fix is enabled we apply emissive lighting after the quad minimum is computed.
			// See AoCalculator#meanBrightness.
			int i = world.getLightLevel(LightType.SKY, pos);
			int j = world.getLightLevel(LightType.BLOCK, pos);
			int k = state.getLuminance();

			if (j < k) {
				j = k;
			}

			return i << 20 | j << 4;
		} else {
			return WorldRenderer.getLightmapCoordinates(world, state, pos);
		}
	}

	private boolean hasEmissiveLighting(BlockRenderView world, BlockPos pos, BlockState state) {
		if (Indigo.FIX_EMISSIVE_LIGHTING) {
			return state.hasEmissiveLighting(world, pos);
		} else {
			// When the fix is disabled, emissive lighting was already applied and does not need to be accounted for.
			return false;
		}
	}

	/**
	 * Vanilla code excluded missing light values from mean but was not isotropic.
	 * Still need to substitute or edges are too dark but consistently use the min
	 * value from all four samples.
	 */
	private static int meanBrightness(int lightA, int lightB, int lightC, int lightD, boolean emA, boolean emB, boolean emC, boolean emD) {
		if (Indigo.FIX_MEAN_LIGHT_CALCULATION) {
			if (lightA == 0 || lightB == 0 || lightC == 0 || lightD == 0) {
				// Normalize values to non-zero minimum
				final int min = nonZeroMin(nonZeroMin(lightA, lightB), nonZeroMin(lightC, lightD));

				lightA = Math.max(lightA, min);
				lightB = Math.max(lightB, min);
				lightC = Math.max(lightC, min);
				lightD = Math.max(lightD, min);
			}

			if (Indigo.FIX_EMISSIVE_LIGHTING) {
				// Apply the fullbright lightmap from emissive blocks at the very end so it cannot influence
				// the minimum lightmap and produce incorrect results (for example, sculk sensors in a dark room)
				if (emA) lightA = LightmapTextureManager.MAX_LIGHT_COORDINATE;
				if (emB) lightB = LightmapTextureManager.MAX_LIGHT_COORDINATE;
				if (emC) lightC = LightmapTextureManager.MAX_LIGHT_COORDINATE;
				if (emD) lightD = LightmapTextureManager.MAX_LIGHT_COORDINATE;
			}

			return meanInnerBrightness(lightA, lightB, lightC, lightD);
		} else {
			return vanillaMeanBrightness(lightA, lightB, lightC, lightD);
		}
	}

	/** vanilla logic - excludes missing light values from mean and has anisotropy defect mentioned above. */
	private static int vanillaMeanBrightness(int a, int b, int c, int d) {
		if (a == 0) a = d;
		if (b == 0) b = d;
		if (c == 0) c = d;
		// bitwise divide by 4, clamp to expected (positive) range
		return a + b + c + d >> 2 & 0xFF00FF;
	}

	private static int meanInnerBrightness(int a, int b, int c, int d) {
		// bitwise divide by 4, clamp to expected (positive) range
		return a + b + c + d >> 2 & 0xFF00FF;
	}

	private static int nonZeroMin(int a, int b) {
		if (a == 0) return b;
		if (b == 0) return a;
		return Math.min(a, b);
	}
}
