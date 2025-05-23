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

package net.fabricmc.fabric.impl.client.indigo.renderer.helper;

import static net.minecraft.util.math.MathHelper.approximatelyEquals;

import org.joml.Vector3fc;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.CubeFace;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;

/**
 * Static routines of general utility for renderer implementations.
 * Renderers are not required to use these helpers, but they were
 * designed to be usable without the default renderer.
 */
public final class GeometryHelper {
	private GeometryHelper() { }

	/** set when a quad touches all four corners of a unit cube. */
	public static final int CUBIC_FLAG = 1;

	/** set when a quad is parallel to (but not necessarily on) its light face. */
	public static final int AXIS_ALIGNED_FLAG = CUBIC_FLAG << 1;

	/** set when a quad is coplanar with its light face. Implies {@link #AXIS_ALIGNED_FLAG} */
	public static final int LIGHT_FACE_FLAG = AXIS_ALIGNED_FLAG << 1;

	/** how many bits quad header encoding should reserve for encoding geometry flags. */
	public static final int FLAG_BIT_COUNT = 3;

	private static final float EPS_MIN = 0.0001f;
	private static final float EPS_MAX = 1.0f - EPS_MIN;

	/**
	 * Analyzes the quad and returns a value with some combination
	 * of {@link #AXIS_ALIGNED_FLAG}, {@link #LIGHT_FACE_FLAG} and {@link #CUBIC_FLAG}.
	 * Intended use is to optimize lighting when the geometry is regular.
	 * Expects convex quads with all points co-planar.
	 */
	public static int computeShapeFlags(QuadView quad) {
		Direction lightFace = quad.lightFace();
		int bits = 0;

		if (isQuadParallelToFace(lightFace, quad)) {
			bits |= AXIS_ALIGNED_FLAG;

			if (isParallelQuadOnFace(lightFace, quad)) {
				bits |= LIGHT_FACE_FLAG;
			}
		}

		if (isQuadCubic(lightFace, quad)) {
			bits |= CUBIC_FLAG;
		}

		return bits;
	}

	/**
	 * Returns true if quad is parallel to the given face.
	 * Does not validate quad winding order.
	 * Expects convex quads with all points co-planar.
	 */
	public static boolean isQuadParallelToFace(Direction face, QuadView quad) {
		int i = face.getAxis().ordinal();
		final float val = quad.posByIndex(0, i);
		return approximatelyEquals(val, quad.posByIndex(1, i)) && approximatelyEquals(val, quad.posByIndex(2, i)) && approximatelyEquals(val, quad.posByIndex(3, i));
	}

	/**
	 * True if quad - already known to be parallel to a face - is actually coplanar with it.
	 * For compatibility with vanilla resource packs, also true if quad is outside the face.
	 *
	 * <p>Test will be unreliable if not already parallel, use {@link #isQuadParallelToFace(Direction, QuadView)}
	 * for that purpose. Expects convex quads with all points co-planar.
	 */
	public static boolean isParallelQuadOnFace(Direction lightFace, QuadView quad) {
		final float x = quad.posByIndex(0, lightFace.getAxis().ordinal());
		return lightFace.getDirection() == AxisDirection.POSITIVE ? x >= EPS_MAX : x <= EPS_MIN;
	}

	/**
	 * Returns true if quad is truly a quad (not a triangle) and fills a full block cross-section.
	 * If known to be true, allows use of a simpler/faster AO lighting algorithm.
	 *
	 * <p>Does not check if quad is actually coplanar with the light face, nor does it check that all
	 * quad vertices are coplanar with each other.
	 *
	 * <p>Expects convex quads with all points co-planar.
	 */
	public static boolean isQuadCubic(Direction lightFace, QuadView quad) {
		int a, b;

		switch (lightFace) {
		case EAST:
		case WEST:
			a = 1;
			b = 2;
			break;
		case UP:
		case DOWN:
			a = 0;
			b = 2;
			break;
		case SOUTH:
		case NORTH:
			a = 1;
			b = 0;
			break;
		default:
			// handle WTF case
			return false;
		}

		return confirmSquareCorners(a, b, quad);
	}

	/**
	 * Used by {@link #isQuadCubic(Direction, QuadView)}.
	 * True if quad touches all four corners of unit square.
	 *
	 * <p>For compatibility with resource packs that contain models with quads exceeding
	 * block boundaries, considers corners outside the block to be at the corners.
	 */
	private static boolean confirmSquareCorners(int aCoordinate, int bCoordinate, QuadView quad) {
		int flags = 0;

		for (int i = 0; i < 4; i++) {
			final float a = quad.posByIndex(i, aCoordinate);
			final float b = quad.posByIndex(i, bCoordinate);

			if (a <= EPS_MIN) {
				if (b <= EPS_MIN) {
					flags |= 1;
				} else if (b >= EPS_MAX) {
					flags |= 2;
				} else {
					return false;
				}
			} else if (a >= EPS_MAX) {
				if (b <= EPS_MIN) {
					flags |= 4;
				} else if (b >= EPS_MAX) {
					flags |= 8;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}

		return flags == 15;
	}

	/**
	 * Identifies the face to which the quad is most closely aligned.
	 * This mimics the value that {@link BakedQuad#face()} returns, and is
	 * used in the vanilla renderer for all diffuse lighting.
	 *
	 * <p>Derived from the quad face normal and expects convex quads with all points co-planar.
	 */
	public static Direction lightFace(QuadView quad) {
		final Vector3fc normal = quad.faceNormal();
		switch (GeometryHelper.longestAxis(normal)) {
		case X:
			return normal.x() > 0 ? Direction.EAST : Direction.WEST;

		case Y:
			return normal.y() > 0 ? Direction.UP : Direction.DOWN;

		case Z:
			return normal.z() > 0 ? Direction.SOUTH : Direction.NORTH;

		default:
			// handle WTF case
			return Direction.UP;
		}
	}

	/**
	 * @see #longestAxis(float, float, float)
	 */
	public static Axis longestAxis(Vector3fc vec) {
		return longestAxis(vec.x(), vec.y(), vec.z());
	}

	/**
	 * Identifies the largest (max absolute magnitude) component (X, Y, Z) in the given vector.
	 */
	public static Axis longestAxis(float normalX, float normalY, float normalZ) {
		Axis result = Axis.Y;
		float longest = Math.abs(normalY);
		float a = Math.abs(normalX);

		if (a > longest) {
			result = Axis.X;
			longest = a;
		}

		return Math.abs(normalZ) > longest
				? Axis.Z : result;
	}

	/**
	 * Returns the index of the vertex which is in the first cubic corner for the given quad's light face, according to
	 * the directions specified in {@link CubeFace}. Assumes that the given quad is
	 * {@linkplain #isQuadCubic(Direction, QuadView) cubic}. Used to make smooth lighting for cubic quads work correctly
	 * regardless of vertex order.
	 *
	 * <p>Because cubic quads have all vertices in different corners, the implementation only has to find which corner
	 * the first vertex is in based on the same criteria as {@link #isQuadCubic(Direction, QuadView)}. Then, since
	 * the vertex winding order is always counterclockwise, it can know which vertex is in the first corner.
	 */
	public static int firstCubicVertex(QuadView quad) {
		final float x = quad.x(0);
		final float y = quad.y(0);
		final float z = quad.z(0);

		/*
		!a & !b -> vertex 0 is in corner 0 -> vertex 0 is in corner 0
		!a & b -> vertex 0 is in corner 1 -> vertex 3 is in corner 0
		a & b -> vertex 0 is in corner 2 -> vertex 2 is in corner 0
		a & !b -> vertex 0 is in corner 3 -> vertex 1 is in corner 0
		 */
		final boolean a;
		final boolean b;

		switch (quad.lightFace()) {
		case DOWN -> {
			a = x > EPS_MIN;
			b = z < EPS_MAX;
		}
		case UP -> {
			a = x > EPS_MIN;
			b = z > EPS_MIN;
		}
		case NORTH -> {
			a = x < EPS_MAX;
			b = y < EPS_MAX;
		}
		case SOUTH -> {
			a = x > EPS_MIN;
			b = y < EPS_MAX;
		}
		case WEST -> {
			a = z > EPS_MIN;
			b = y < EPS_MAX;
		}
		case EAST -> {
			a = z < EPS_MAX;
			b = y < EPS_MAX;
		}
		default -> {
			return 0;
		}
		}

		int result = 0;

		if (a) {
			result ^= 1;
		}

		if (b) {
			result ^= 3;
		}

		return result;
	}
}
