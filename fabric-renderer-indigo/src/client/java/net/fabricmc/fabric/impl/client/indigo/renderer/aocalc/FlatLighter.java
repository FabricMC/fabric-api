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
import static net.fabricmc.fabric.impl.client.indigo.renderer.helper.GeometryHelper.LIGHT_FACE_FLAG;

import org.joml.Vector3fc;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.impl.client.indigo.Indigo;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.QuadViewImpl;

public class FlatLighter {
	private final BlockPos.MutableBlockPos scratchPos = new BlockPos.MutableBlockPos();
	private final BlockModelLighter.Cache lightCache;

	public FlatLighter(BlockModelLighter.Cache lightCache) {
		this.lightCache = lightCache;
	}

	/**
	 * Starting in 1.16 flat shading uses dimension-specific diffuse factors that can be < 1.0
	 * even for un-shaded quads. These are also applied with AO shading but that is done in AO calculator.
	 */
	public void applyDirectionalBrightness(CardinalLighting cardinalLighting, MutableQuadViewImpl quad, boolean vanillaShade) {
		if (!quad.diffuseShade()) {
			final float directionalBrightness = cardinalLighting.up();

			if (directionalBrightness != 1.0f) {
				for (int i = 0; i < 4; i++) {
					quad.color(i, ARGB.scaleRGB(quad.color(i), directionalBrightness));
				}
			}
		} else if ((Indigo.AMBIENT_OCCLUSION_MODE == AoConfig.HYBRID && !vanillaShade) || Indigo.AMBIENT_OCCLUSION_MODE == AoConfig.ENHANCED) {
			// ^ Check the AO mode to match how shade is applied during smooth lighting
			if (quad.hasAllVertexNormals()) {
				for (int i = 0; i < 4; i++) {
					final float directionalBrightness = normalShade(cardinalLighting, quad.normalX(i), quad.normalY(i), quad.normalZ(i));
					quad.color(i, ARGB.scaleRGB(quad.color(i), directionalBrightness));
				}
			} else {
				final float directionalBrightness;

				if ((quad.geometryFlags() & AXIS_ALIGNED_FLAG) != 0) {
					directionalBrightness = cardinalLighting.byFace(quad.lightFace());
				} else {
					Vector3fc faceNormal = quad.faceNormal();
					directionalBrightness = normalShade(cardinalLighting, faceNormal.x(), faceNormal.y(), faceNormal.z());
				}

				if (quad.hasVertexNormals()) {
					for (int i = 0; i < 4; i++) {
						float shade;

						if (quad.hasNormal(i)) {
							shade = normalShade(cardinalLighting, quad.normalX(i), quad.normalY(i), quad.normalZ(i));
						} else {
							shade = directionalBrightness;
						}

						quad.color(i, ARGB.scaleRGB(quad.color(i), shade));
					}
				} else {
					if (directionalBrightness != 1.0f) {
						for (int i = 0; i < 4; i++) {
							quad.color(i, ARGB.scaleRGB(quad.color(i), directionalBrightness));
						}
					}
				}
			}
		} else {
			final float directionalBrightness = cardinalLighting.byFace(quad.lightFace());

			if (directionalBrightness != 1.0f) {
				for (int i = 0; i < 4; i++) {
					quad.color(i, ARGB.scaleRGB(quad.color(i), directionalBrightness));
				}
			}
		}
	}

	/**
	 * Finds mean of per-face shading factors weighted by normal components.
	 * Not how light actually works but the vanilla diffuse shading model is a hack to start with
	 * and this gives reasonable results for non-cubic surfaces in a vanilla-style renderer.
	 */
	private static float normalShade(CardinalLighting cardinalLighting, float normalX, float normalY, float normalZ) {
		float sum = 0;
		float div = 0;

		if (normalX > 0) {
			sum += normalX * cardinalLighting.byFace(Direction.EAST);
			div += normalX;
		} else if (normalX < 0) {
			sum += -normalX * cardinalLighting.byFace(Direction.WEST);
			div -= normalX;
		}

		if (normalY > 0) {
			sum += normalY * cardinalLighting.byFace(Direction.UP);
			div += normalY;
		} else if (normalY < 0) {
			sum += -normalY * cardinalLighting.byFace(Direction.DOWN);
			div -= normalY;
		}

		if (normalZ > 0) {
			sum += normalZ * cardinalLighting.byFace(Direction.SOUTH);
			div += normalZ;
		} else if (normalZ < 0) {
			sum += -normalZ * cardinalLighting.byFace(Direction.NORTH);
			div -= normalZ;
		}

		return sum / div;
	}

	/**
	 * Handles geometry-based check for using self light or neighbor light.
	 * That logic only applies in flat lighting.
	 */
	public int light(BlockAndTintGetter level, BlockState state, BlockPos pos, QuadViewImpl quad) {
		scratchPos.set(pos);

		// To mirror Vanilla's behavior, if the face has a cull-face, always sample the light value
		// offset in that direction. See net.minecraft.client.renderer.block.ModelBlockRenderer.tesselateFlat
		// for reference.
		if (quad.cullFace() != null) {
			scratchPos.move(quad.cullFace());
		} else {
			final int flags = quad.geometryFlags();

			if ((flags & LIGHT_FACE_FLAG) != 0 || ((flags & AXIS_ALIGNED_FLAG) != 0 && state.isCollisionShapeFullBlock(level, pos))) {
				scratchPos.move(quad.lightFace());
			}
		}

		return lightCache.getLightCoords(state, level, scratchPos);
	}
}
