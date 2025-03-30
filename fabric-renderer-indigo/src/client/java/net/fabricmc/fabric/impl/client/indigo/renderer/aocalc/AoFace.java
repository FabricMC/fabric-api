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

import static net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoVertexClampFunction.CLAMP_FUNC;

import net.minecraft.util.math.Direction;

import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.QuadViewImpl;

/**
 * Adapted from vanilla BlockModelRenderer.NeighborData and BlockModelRenderer.Translation.
 */
enum AoFace {
	DOWN(new Direction[] { Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH }, new int[] { 0, 1, 2, 3 }) {
		@Override
		void computeCornerWeights(QuadViewImpl q, int vertexIndex, float[] out) {
			final float u = CLAMP_FUNC.clamp(q.x(vertexIndex));
			final float v = CLAMP_FUNC.clamp(q.z(vertexIndex));
			out[0] = (1 - u) * v;
			out[1] = (1 - u) * (1 - v);
			out[2] = u * (1 - v);
			out[3] = u * v;
		}

		@Override
		float computeDepth(QuadViewImpl q, int vertexIndex) {
			return CLAMP_FUNC.clamp(q.y(vertexIndex));
		}
	},
	UP(new Direction[] { Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH }, new int[] { 2, 3, 0, 1 }) {
		@Override
		void computeCornerWeights(QuadViewImpl q, int vertexIndex, float[] out) {
			final float u = CLAMP_FUNC.clamp(q.x(vertexIndex));
			final float v = CLAMP_FUNC.clamp(q.z(vertexIndex));
			out[0] = u * v;
			out[1] = u * (1 - v);
			out[2] = (1 - u) * (1 - v);
			out[3] = (1 - u) * v;
		}

		@Override
		float computeDepth(QuadViewImpl q, int vertexIndex) {
			return 1 - CLAMP_FUNC.clamp(q.y(vertexIndex));
		}
	},
	NORTH(new Direction[] { Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST }, new int[] { 3, 0, 1, 2 }) {
		@Override
		void computeCornerWeights(QuadViewImpl q, int vertexIndex, float[] out) {
			final float u = CLAMP_FUNC.clamp(q.y(vertexIndex));
			final float v = CLAMP_FUNC.clamp(q.x(vertexIndex));
			out[0] = u * (1 - v);
			out[1] = u * v;
			out[2] = (1 - u) * v;
			out[3] = (1 - u) * (1 - v);
		}

		@Override
		float computeDepth(QuadViewImpl q, int vertexIndex) {
			return CLAMP_FUNC.clamp(q.z(vertexIndex));
		}
	},
	SOUTH(new Direction[] { Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP }, new int[] { 0, 1, 2, 3 }) {
		@Override
		void computeCornerWeights(QuadViewImpl q, int vertexIndex, float[] out) {
			final float u = CLAMP_FUNC.clamp(q.y(vertexIndex));
			final float v = CLAMP_FUNC.clamp(q.x(vertexIndex));
			out[0] = u * (1 - v);
			out[1] = (1 - u) * (1 - v);
			out[2] = (1 - u) * v;
			out[3] = u * v;
		}

		@Override
		float computeDepth(QuadViewImpl q, int vertexIndex) {
			return 1 - CLAMP_FUNC.clamp(q.z(vertexIndex));
		}
	},
	WEST(new Direction[] { Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH }, new int[] { 3, 0, 1, 2 }) {
		@Override
		void computeCornerWeights(QuadViewImpl q, int vertexIndex, float[] out) {
			final float u = CLAMP_FUNC.clamp(q.y(vertexIndex));
			final float v = CLAMP_FUNC.clamp(q.z(vertexIndex));
			out[0] = u * v;
			out[1] = u * (1 - v);
			out[2] = (1 - u) * (1 - v);
			out[3] = (1 - u) * v;
		}

		@Override
		float computeDepth(QuadViewImpl q, int vertexIndex) {
			return CLAMP_FUNC.clamp(q.x(vertexIndex));
		}
	},
	EAST(new Direction[] { Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH }, new int[] { 1, 2, 3, 0 }) {
		@Override
		void computeCornerWeights(QuadViewImpl q, int vertexIndex, float[] out) {
			final float u = CLAMP_FUNC.clamp(q.y(vertexIndex));
			final float v = CLAMP_FUNC.clamp(q.z(vertexIndex));
			out[0] = (1 - u) * v;
			out[1] = (1 - u) * (1 - v);
			out[2] = u * (1 - v);
			out[3] = u * v;
		}

		@Override
		float computeDepth(QuadViewImpl q, int vertexIndex) {
			return 1 - CLAMP_FUNC.clamp(q.x(vertexIndex));
		}
	};

	private static final AoFace[] VALUES = AoFace.values();

	final Direction[] neighbors;
	/**
	 * Vanilla models with cubic quads have vertices in a certain order, which allows
	 * us to map them using a lookup.
	 */
	final int[] vertexMap;

	AoFace(Direction[] neighbors, int[] vertexMap) {
		this.neighbors = neighbors;
		this.vertexMap = vertexMap;
	}

	/**
	 * Implementations handle bilinear interpolation of a point on a light face
	 * by computing weights for each corner of the light face. Relies on the fact
	 * that each face is a unit cube. Uses coordinates from axes orthogonal to face
	 * as distance from the edge of the cube, flipping as needed. Multiplying distance
	 * coordinate pairs together gives sub-area that are the corner weights.
	 * Weights sum to 1 because it is a unit cube. Values are stored in the provided array.
	 */
	abstract void computeCornerWeights(QuadViewImpl q, int vertexIndex, float[] out);

	abstract float computeDepth(QuadViewImpl q, int vertexIndex);

	static AoFace get(Direction direction) {
		return VALUES[direction.getIndex()];
	}
}
