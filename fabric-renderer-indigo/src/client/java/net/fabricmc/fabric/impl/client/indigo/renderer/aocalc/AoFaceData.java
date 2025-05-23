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

/**
 * Holds per-corner results for a single block face.
 * Handles caching and provides various utility methods to simplify code elsewhere.
 */
class AoFaceData {
	float a0;
	float a1;
	float a2;
	float a3;
	int b0;
	int b1;
	int b2;
	int b3;
	int s0;
	int s1;
	int s2;
	int s3;

	void l0(int l0) {
		this.b0 = l0 & 0xFFFF;
		this.s0 = (l0 >>> 16) & 0xFFFF;
	}

	void l1(int l1) {
		this.b1 = l1 & 0xFFFF;
		this.s1 = (l1 >>> 16) & 0xFFFF;
	}

	void l2(int l2) {
		this.b2 = l2 & 0xFFFF;
		this.s2 = (l2 >>> 16) & 0xFFFF;
	}

	void l3(int l3) {
		this.b3 = l3 & 0xFFFF;
		this.s3 = (l3 >>> 16) & 0xFFFF;
	}

	int weightedBlockLight(float[] w) {
		return (int) (b0 * w[0] + b1 * w[1] + b2 * w[2] + b3 * w[3]) & 0xFF;
	}

	int weightedSkyLight(float[] w) {
		return (int) (s0 * w[0] + s1 * w[1] + s2 * w[2] + s3 * w[3]) & 0xFF;
	}

	int weightedCombinedLight(float[] w) {
		return weightedSkyLight(w) << 16 | weightedBlockLight(w);
	}

	float weightedAo(float[] w) {
		return a0 * w[0] + a1 * w[1] + a2 * w[2] + a3 * w[3];
	}

	void toArrays(float[] aoOut, int[] lightOut, int[] vertexMap, int vertexOffset) {
		int i0 = vertexMap[vertexOffset];
		int i1 = vertexMap[(vertexOffset + 1) % 4];
		int i2 = vertexMap[(vertexOffset + 2) % 4];
		int i3 = vertexMap[(vertexOffset + 3) % 4];

		aoOut[i0] = a0;
		aoOut[i1] = a1;
		aoOut[i2] = a2;
		aoOut[i3] = a3;

		lightOut[i0] = s0 << 16 | b0;
		lightOut[i1] = s1 << 16 | b1;
		lightOut[i2] = s2 << 16 | b2;
		lightOut[i3] = s3 << 16 | b3;
	}

	static AoFaceData weightedMean(AoFaceData in0, float w0, AoFaceData in1, float w1, AoFaceData out) {
		out.a0 = in0.a0 * w0 + in1.a0 * w1;
		out.a1 = in0.a1 * w0 + in1.a1 * w1;
		out.a2 = in0.a2 * w0 + in1.a2 * w1;
		out.a3 = in0.a3 * w0 + in1.a3 * w1;

		out.b0 = (int) (in0.b0 * w0 + in1.b0 * w1);
		out.b1 = (int) (in0.b1 * w0 + in1.b1 * w1);
		out.b2 = (int) (in0.b2 * w0 + in1.b2 * w1);
		out.b3 = (int) (in0.b3 * w0 + in1.b3 * w1);

		out.s0 = (int) (in0.s0 * w0 + in1.s0 * w1);
		out.s1 = (int) (in0.s1 * w0 + in1.s1 * w1);
		out.s2 = (int) (in0.s2 * w0 + in1.s2 * w1);
		out.s3 = (int) (in0.s3 * w0 + in1.s3 * w1);

		return out;
	}
}
