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

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.fabric.impl.client.indigo.Indigo;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.QuadViewImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.LightDataProvider;

/**
 * Adaptation of inner, non-static class in BlockModelRenderer that serves same purpose.
 */
public class AoCalculator {
	private static final Logger LOGGER = LoggerFactory.getLogger(AoCalculator.class);

	private final BlockRenderInfo blockInfo;
	private final LightDataProvider dataProvider;

	private final BlockPos.Mutable lightPos = new BlockPos.Mutable();
	private final BlockPos.Mutable searchPos = new BlockPos.Mutable();

	/** caches results of {@link #computeFace(Direction, boolean, boolean)} for the current block. */
	private final AoFaceData[] faceData = new AoFaceData[24];

	/** indicates which elements of {@link #faceData} have been computed for the current block. */
	private int completionFlags = 0;

	/** holds per-corner weights - used locally to avoid new allocation. */
	private final float[] w = new float[4];

	// outputs
	public final float[] ao = new float[4];
	public final int[] light = new int[4];

	public AoCalculator(BlockRenderInfo blockInfo, LightDataProvider dataProvider) {
		this.blockInfo = blockInfo;
		this.dataProvider = dataProvider;

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
					LOGGER.info(String.format("    Old Brightness: %.2f, %.2f, %.2f, %.2f", vanillaAo[0], vanillaAo[1], vanillaAo[2], vanillaAo[3]));
					LOGGER.info(String.format("    New Brightness: %.2f, %.2f, %.2f, %.2f", ao[0], ao[1], ao[2], ao[3]));
					LOGGER.info(String.format("    Old Light: %s, %s, %s, %s", Integer.toHexString(vanillaLight[0]), Integer.toHexString(vanillaLight[1]), Integer.toHexString(vanillaLight[2]), Integer.toHexString(vanillaLight[3])));
					LOGGER.info(String.format("    New Light: %s, %s, %s, %s", Integer.toHexString(light[0]), Integer.toHexString(light[1]), Integer.toHexString(light[2]), Integer.toHexString(light[3])));
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
	private final int[] vertexData = new int[EncodingFormat.QUAD_STRIDE];
	private final BlockModelRenderer.AmbientOcclusionCalculator vanillaCalc = new BlockModelRenderer.AmbientOcclusionCalculator();

	private void calcVanilla(QuadViewImpl quad, float[] aoDest, int[] lightDest) {
		final Direction lightFace = quad.lightFace();
		quad.toVanilla(vertexData, 0);

		BlockModelRenderer.getQuadDimensions(blockInfo.blockView, blockInfo.blockState, blockInfo.blockPos, vertexData, lightFace, vanillaCalc);
		vanillaCalc.apply(blockInfo.blockView, blockInfo.blockState, blockInfo.blockPos, lightFace, quad.hasShade());

		System.arraycopy(vanillaCalc.fs, 0, aoDest, 0, 4);
		System.arraycopy(vanillaCalc.is, 0, lightDest, 0, 4);
	}

	private void calcFastVanilla(QuadViewImpl quad) {
		int flags = quad.geometryFlags();
		boolean isOnLightFace = (flags & LIGHT_FACE_FLAG) != 0;

		// force to block face if shape is full cube - matches vanilla logic
		if (!isOnLightFace && (flags & AXIS_ALIGNED_FLAG) != 0 && blockInfo.blockState.isFullCube(blockInfo.blockView, blockInfo.blockPos)) {
			isOnLightFace = true;
		}

		if ((flags & CUBIC_FLAG) == 0) {
			vanillaPartialFace(quad, quad.lightFace(), isOnLightFace, quad.hasShade());
		} else {
			vanillaFullFace(quad, quad.lightFace(), isOnLightFace, quad.hasShade());
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
		computeFace(lightFace, isOnLightFace, shade).toArray(ao, light, AoFace.get(lightFace).vertexMap);
	}

	private void vanillaPartialFace(QuadViewImpl quad, Direction lightFace, boolean isOnLightFace, boolean shade) {
		AoFaceData faceData = computeFace(lightFace, isOnLightFace, shade);
		final AoFace aoFace = AoFace.get(lightFace);
		final float[] w = this.w;

		for (int i = 0; i < 4; i++) {
			aoFace.computeCornerWeights(quad, i, w);
			light[i] = faceData.weightedCombinedLight(w);
			ao[i] = faceData.weigtedAo(w);
		}
	}

	/** used in {@link #blendedInsetFace(QuadViewImpl quad, int vertexIndex, Direction lightFace, boolean shade)} as return variable to avoid new allocation. */
	AoFaceData tmpFace = new AoFaceData();

	/** Returns linearly interpolated blend of outer and inner face based on depth of vertex in face. */
	private AoFaceData blendedInsetFace(QuadViewImpl quad, int vertexIndex, Direction lightFace, boolean shade) {
		final float w1 = AoFace.get(lightFace).computeDepth(quad, vertexIndex);
		final float w0 = 1 - w1;
		return AoFaceData.weightedMean(computeFace(lightFace, true, shade), w0, computeFace(lightFace, false, shade), w1, tmpFace);
	}

	/**
	 * Like {@link #blendedInsetFace(QuadViewImpl quad, int vertexIndex, Direction lightFace, boolean shade)} but optimizes if depth is 0 or 1.
	 * Used for irregular faces when depth varies by vertex to avoid unneeded interpolation.
	 */
	private AoFaceData gatherInsetFace(QuadViewImpl quad, int vertexIndex, Direction lightFace, boolean shade) {
		final float w1 = AoFace.get(lightFace).computeDepth(quad, vertexIndex);

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
		final AoFace aoFace = AoFace.get(lightFace);

		for (int i = 0; i < 4; i++) {
			aoFace.computeCornerWeights(quad, i, w);
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
				AoFace.get(face).computeCornerWeights(quad, i, w);
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
				AoFace.get(face).computeCornerWeights(quad, i, w);
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
				AoFace.get(face).computeCornerWeights(quad, i, w);
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
			lightResult[i] = (((int) ((sky + maxSky) * 0.5f) & 0xFF) << 16) | ((int) ((block + maxBlock) * 0.5f) & 0xFF);
		}
	}

	private AoFaceData computeFace(Direction lightFace, boolean isOnBlockFace, boolean shade) {
		final int faceDataIndex = shade ? (isOnBlockFace ? lightFace.getIndex() : lightFace.getIndex() + 6) : (isOnBlockFace ? lightFace.getIndex() + 12 : lightFace.getIndex() + 18);
		final int mask = 1 << faceDataIndex;
		final AoFaceData result = faceData[faceDataIndex];

		if ((completionFlags & mask) == 0) {
			completionFlags |= mask;
			computeFace(result, lightFace, isOnBlockFace, shade);
		}

		return result;
	}

	/**
	 * Computes smoothed light and brightness for four corners of a block face.
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
		final int light0 = dataProvider.light(searchPos, searchState);
		final float ao0 = dataProvider.ao(searchPos, searchState);

		if (!Indigo.FIX_SMOOTH_LIGHTING_OFFSET) {
			searchPos.move(lightFace);
			searchState = world.getBlockState(searchPos);
		}

		final boolean isClear0 = !searchState.shouldBlockVision(world, searchPos) || searchState.getOpacity() == 0;

		searchPos.set(lightPos, aoFace.neighbors[1]);
		searchState = world.getBlockState(searchPos);
		final int light1 = dataProvider.light(searchPos, searchState);
		final float ao1 = dataProvider.ao(searchPos, searchState);

		if (!Indigo.FIX_SMOOTH_LIGHTING_OFFSET) {
			searchPos.move(lightFace);
			searchState = world.getBlockState(searchPos);
		}

		final boolean isClear1 = !searchState.shouldBlockVision(world, searchPos) || searchState.getOpacity() == 0;

		searchPos.set(lightPos, aoFace.neighbors[2]);
		searchState = world.getBlockState(searchPos);
		final int light2 = dataProvider.light(searchPos, searchState);
		final float ao2 = dataProvider.ao(searchPos, searchState);

		if (!Indigo.FIX_SMOOTH_LIGHTING_OFFSET) {
			searchPos.move(lightFace);
			searchState = world.getBlockState(searchPos);
		}

		final boolean isClear2 = !searchState.shouldBlockVision(world, searchPos) || searchState.getOpacity() == 0;

		searchPos.set(lightPos, aoFace.neighbors[3]);
		searchState = world.getBlockState(searchPos);
		final int light3 = dataProvider.light(searchPos, searchState);
		final float ao3 = dataProvider.ao(searchPos, searchState);

		if (!Indigo.FIX_SMOOTH_LIGHTING_OFFSET) {
			searchPos.move(lightFace);
			searchState = world.getBlockState(searchPos);
		}

		final boolean isClear3 = !searchState.shouldBlockVision(world, searchPos) || searchState.getOpacity() == 0;

		// c = corner - values at corners of face
		int cLight0, cLight1, cLight2, cLight3;
		float cAo0, cAo1, cAo2, cAo3;
		boolean cIsClear0, cIsClear1, cIsClear2, cIsClear3;

		// If neighbors on both sides of the corner are opaque, then apparently we use the light/shade
		// from one of the sides adjacent to the corner.  If either neighbor is clear (no light subtraction)
		// then we use values from the outwardly diagonal corner. (outwardly = position is one more away from light face)
		if (!isClear2 && !isClear0) {
			cAo0 = ao0;
			cLight0 = light0;
			cIsClear0 = false;
		} else {
			searchPos.set(lightPos, aoFace.neighbors[0]).move(aoFace.neighbors[2]);
			searchState = world.getBlockState(searchPos);
			cAo0 = dataProvider.ao(searchPos, searchState);
			cLight0 = dataProvider.light(searchPos, searchState);
			cIsClear0 = !searchState.shouldBlockVision(world, searchPos) || searchState.getOpacity() == 0;
		}

		if (!isClear3 && !isClear0) {
			cAo1 = ao0;
			cLight1 = light0;
			cIsClear1 = false;
		} else {
			searchPos.set(lightPos, aoFace.neighbors[0]).move(aoFace.neighbors[3]);
			searchState = world.getBlockState(searchPos);
			cAo1 = dataProvider.ao(searchPos, searchState);
			cLight1 = dataProvider.light(searchPos, searchState);
			cIsClear1 = !searchState.shouldBlockVision(world, searchPos) || searchState.getOpacity() == 0;
		}

		if (!isClear2 && !isClear1) {
			// Use the values from neighbor 1 instead of neighbor 0 since this corner is not adjacent to neighbor 0
			cAo2 = ao1;
			cLight2 = light1;
			cIsClear2 = false;
		} else {
			searchPos.set(lightPos, aoFace.neighbors[1]).move(aoFace.neighbors[2]);
			searchState = world.getBlockState(searchPos);
			cAo2 = dataProvider.ao(searchPos, searchState);
			cLight2 = dataProvider.light(searchPos, searchState);
			cIsClear2 = !searchState.shouldBlockVision(world, searchPos) || searchState.getOpacity() == 0;
		}

		if (!isClear3 && !isClear1) {
			// Use the values from neighbor 1 instead of neighbor 0 since this corner is not adjacent to neighbor 0
			cAo3 = ao1;
			cLight3 = light1;
			cIsClear3 = false;
		} else {
			searchPos.set(lightPos, aoFace.neighbors[1]).move(aoFace.neighbors[3]);
			searchState = world.getBlockState(searchPos);
			cAo3 = dataProvider.ao(searchPos, searchState);
			cLight3 = dataProvider.light(searchPos, searchState);
			cIsClear3 = !searchState.shouldBlockVision(world, searchPos) || searchState.getOpacity() == 0;
		}

		// If on block face and neighbor isn't occluding, "center" will be neighbor light
		// Doesn't use light pos because logic not based solely on this block's geometry
		int lightCenter;
		boolean isClearCenter;
		searchPos.set(pos, lightFace);
		searchState = world.getBlockState(searchPos);

		// Vanilla uses an OR operator here; we use an AND operator to invert the result when isOnBlockFace and
		// isOpaqueFullCube have the same value. When both are true, the vanilla logic caused inset faces against
		// solid blocks to appear too dark when using enhanced AO (i.e. slab below ceiling or fence against wall). When
		// both are false, the vanilla logic caused inset faces against non-solid blocks to be lit discontinuously (i.e.
		// dark room with active sculk sensor above slabs).
		if (isOnBlockFace && !searchState.isOpaqueFullCube()) {
			lightCenter = dataProvider.light(searchPos, searchState);
			isClearCenter = !searchState.shouldBlockVision(world, searchPos) || searchState.getOpacity() == 0;
		} else {
			lightCenter = dataProvider.light(pos, blockState);
			isClearCenter = !blockState.shouldBlockVision(world, pos) || blockState.getOpacity() == 0;
		}

		float aoCenter = dataProvider.ao(lightPos, world.getBlockState(lightPos));
		float shadeBrightness = world.getBrightness(lightFace, shade);

		result.a0 = ((ao3 + ao0 + cAo1 + aoCenter) * 0.25F) * shadeBrightness;
		result.a1 = ((ao2 + ao0 + cAo0 + aoCenter) * 0.25F) * shadeBrightness;
		result.a2 = ((ao2 + ao1 + cAo2 + aoCenter) * 0.25F) * shadeBrightness;
		result.a3 = ((ao3 + ao1 + cAo3 + aoCenter) * 0.25F) * shadeBrightness;

		result.l0(meanLight(light3, light0, cLight1, lightCenter, isClear3, isClear0, cIsClear1, isClearCenter));
		result.l1(meanLight(light2, light0, cLight0, lightCenter, isClear2, isClear0, cIsClear0, isClearCenter));
		result.l2(meanLight(light2, light1, cLight2, lightCenter, isClear2, isClear1, cIsClear2, isClearCenter));
		result.l3(meanLight(light3, light1, cLight3, lightCenter, isClear3, isClear1, cIsClear3, isClearCenter));
	}

	/**
	 * Vanilla code sets light values equal to zero to the center light value (D) before taking the mean, which fixes
	 * solid blocks near a face making edges too dark. However, a value of zero does not mean it came from a solid
	 * block; this causes natural zero values to be treated differently from other natural values, causing visual
	 * inconsistencies. This implementation checks for the source of a light value explicitly. It also fixes samples
	 * being blended inconsistently based on the center position, which causes discontinuities, by computing a
	 * consistent minimum light value from all four samples.
	 */
	private static int meanLight(int lightA, int lightB, int lightC, int lightD, boolean isClearA, boolean isClearB, boolean isClearC, boolean isClearD) {
		if (Indigo.FIX_MEAN_LIGHT_CALCULATION) {
			int lightABlock = lightA & 0xFFFF;
			int lightASky = (lightA >>> 16) & 0xFFFF;
			int lightBBlock = lightB & 0xFFFF;
			int lightBSky = (lightB >>> 16) & 0xFFFF;
			int lightCBlock = lightC & 0xFFFF;
			int lightCSky = (lightC >>> 16) & 0xFFFF;
			int lightDBlock = lightD & 0xFFFF;
			int lightDSky = (lightD >>> 16) & 0xFFFF;

			// Compute per-component minimum light, only including values from clear positions
			int minBlock = 0x10000;
			int minSky = 0x10000;

			if (isClearA) {
				minBlock = lightABlock;
				minSky = lightASky;
			}

			if (isClearB) {
				minBlock = Math.min(minBlock, lightBBlock);
				minSky = Math.min(minSky, lightBSky);
			}

			if (isClearC) {
				minBlock = Math.min(minBlock, lightCBlock);
				minSky = Math.min(minSky, lightCSky);
			}

			if (isClearD) {
				minBlock = Math.min(minBlock, lightDBlock);
				minSky = Math.min(minSky, lightDSky);
			}

			// Ensure that if no positions were clear, minimum is 0
			minBlock &= 0xFFFF;
			minSky &= 0xFFFF;

			lightA = Math.max(lightASky, minSky) << 16 | Math.max(lightABlock, minBlock);
			lightB = Math.max(lightBSky, minSky) << 16 | Math.max(lightBBlock, minBlock);
			lightC = Math.max(lightCSky, minSky) << 16 | Math.max(lightCBlock, minBlock);
			lightD = Math.max(lightDSky, minSky) << 16 | Math.max(lightDBlock, minBlock);

			return meanInnerLight(lightA, lightB, lightC, lightD);
		} else {
			return vanillaMeanLight(lightA, lightB, lightC, lightD);
		}
	}

	/** vanilla logic - excludes missing light values from mean and has anisotropy defect mentioned above. */
	private static int vanillaMeanLight(int a, int b, int c, int d) {
		if (a == 0) a = d;
		if (b == 0) b = d;
		if (c == 0) c = d;
		// bitwise divide by 4, clamp to expected (positive) range
		return a + b + c + d >> 2 & 0xFF00FF;
	}

	private static int meanInnerLight(int a, int b, int c, int d) {
		// bitwise divide by 4, clamp to expected (positive) range
		return a + b + c + d >> 2 & 0xFF00FF;
	}
}
