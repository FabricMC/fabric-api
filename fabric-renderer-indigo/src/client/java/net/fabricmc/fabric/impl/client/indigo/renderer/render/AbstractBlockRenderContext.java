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

import static net.fabricmc.fabric.impl.client.indigo.renderer.helper.GeometryHelper.AXIS_ALIGNED_FLAG;
import static net.fabricmc.fabric.impl.client.indigo.renderer.helper.GeometryHelper.LIGHT_FACE_FLAG;

import org.joml.Vector3fc;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.material.ShadeMode;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.fabric.impl.client.indigo.Indigo;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoCalculator;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoConfig;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;

public abstract class AbstractBlockRenderContext extends AbstractRenderContext {
	protected final BlockRenderInfo blockInfo = new BlockRenderInfo();
	protected final AoCalculator aoCalc;

	private final BlockPos.Mutable lightPos = new BlockPos.Mutable();

	protected AbstractBlockRenderContext() {
		aoCalc = createAoCalc(blockInfo);
	}

	protected abstract AoCalculator createAoCalc(BlockRenderInfo blockInfo);

	protected abstract VertexConsumer getVertexConsumer(RenderLayer layer);

	@Override
	protected void bufferQuad(MutableQuadViewImpl quad) {
		if (blockInfo.shouldCullSide(quad.cullFace())) {
			return;
		}

		final RenderMaterial mat = quad.material();
		final TriState aoMode = mat.ambientOcclusion();
		final boolean ao = blockInfo.useAo && (aoMode == TriState.TRUE || (aoMode == TriState.DEFAULT && blockInfo.defaultAo));
		final boolean emissive = mat.emissive();
		final boolean vanillaShade = mat.shadeMode() == ShadeMode.VANILLA;
		final VertexConsumer vertexConsumer = getVertexConsumer(blockInfo.effectiveRenderLayer(mat.blendMode()));

		tintQuad(quad);
		shadeQuad(quad, ao, emissive, vanillaShade);
		bufferQuad(quad, vertexConsumer);
	}

	/** handles block color, common to all renders. */
	private void tintQuad(MutableQuadViewImpl quad) {
		int tintIndex = quad.tintIndex();

		if (tintIndex != -1) {
			final int blockColor = blockInfo.blockColor(tintIndex);

			for (int i = 0; i < 4; i++) {
				quad.color(i, ColorHelper.multiplyColor(blockColor, quad.color(i)));
			}
		}
	}

	private void shadeQuad(MutableQuadViewImpl quad, boolean ao, boolean emissive, boolean vanillaShade) {
		// routines below have a bit of copy-paste code reuse to avoid conditional execution inside a hot loop
		if (ao) {
			aoCalc.compute(quad, vanillaShade);

			if (emissive) {
				for (int i = 0; i < 4; i++) {
					quad.color(i, ColorHelper.multiplyRGB(quad.color(i), aoCalc.ao[i]));
					quad.lightmap(i, LightmapTextureManager.MAX_LIGHT_COORDINATE);
				}
			} else {
				for (int i = 0; i < 4; i++) {
					quad.color(i, ColorHelper.multiplyRGB(quad.color(i), aoCalc.ao[i]));
					quad.lightmap(i, ColorHelper.maxBrightness(quad.lightmap(i), aoCalc.light[i]));
				}
			}
		} else {
			shadeFlatQuad(quad, vanillaShade);

			if (emissive) {
				for (int i = 0; i < 4; i++) {
					quad.lightmap(i, LightmapTextureManager.MAX_LIGHT_COORDINATE);
				}
			} else {
				final int brightness = flatBrightness(quad, blockInfo.blockState, blockInfo.blockPos);

				for (int i = 0; i < 4; i++) {
					quad.lightmap(i, ColorHelper.maxBrightness(quad.lightmap(i), brightness));
				}
			}
		}
	}

	/**
	 * Starting in 1.16 flat shading uses dimension-specific diffuse factors that can be < 1.0
	 * even for un-shaded quads. These are also applied with AO shading but that is done in AO calculator.
	 */
	private void shadeFlatQuad(MutableQuadViewImpl quad, boolean vanillaShade) {
		final boolean hasShade = quad.hasShade();

		// Check the AO mode to match how shade is applied during smooth lighting
		if ((Indigo.AMBIENT_OCCLUSION_MODE == AoConfig.HYBRID && !vanillaShade) || Indigo.AMBIENT_OCCLUSION_MODE == AoConfig.ENHANCED) {
			if (quad.hasAllVertexNormals()) {
				for (int i = 0; i < 4; i++) {
					float shade = normalShade(quad.normalX(i), quad.normalY(i), quad.normalZ(i), hasShade);
					quad.color(i, ColorHelper.multiplyRGB(quad.color(i), shade));
				}
			} else {
				final float faceShade;

				if ((quad.geometryFlags() & AXIS_ALIGNED_FLAG) != 0) {
					faceShade = blockInfo.blockView.getBrightness(quad.lightFace(), hasShade);
				} else {
					Vector3fc faceNormal = quad.faceNormal();
					faceShade = normalShade(faceNormal.x(), faceNormal.y(), faceNormal.z(), hasShade);
				}

				if (quad.hasVertexNormals()) {
					for (int i = 0; i < 4; i++) {
						float shade;

						if (quad.hasNormal(i)) {
							shade = normalShade(quad.normalX(i), quad.normalY(i), quad.normalZ(i), hasShade);
						} else {
							shade = faceShade;
						}

						quad.color(i, ColorHelper.multiplyRGB(quad.color(i), shade));
					}
				} else {
					if (faceShade != 1.0f) {
						for (int i = 0; i < 4; i++) {
							quad.color(i, ColorHelper.multiplyRGB(quad.color(i), faceShade));
						}
					}
				}
			}
		} else {
			final float faceShade = blockInfo.blockView.getBrightness(quad.lightFace(), hasShade);

			if (faceShade != 1.0f) {
				for (int i = 0; i < 4; i++) {
					quad.color(i, ColorHelper.multiplyRGB(quad.color(i), faceShade));
				}
			}
		}
	}

	/**
	 * Finds mean of per-face shading factors weighted by normal components.
	 * Not how light actually works but the vanilla diffuse shading model is a hack to start with
	 * and this gives reasonable results for non-cubic surfaces in a vanilla-style renderer.
	 */
	private float normalShade(float normalX, float normalY, float normalZ, boolean hasShade) {
		float sum = 0;
		float div = 0;

		if (normalX > 0) {
			sum += normalX * blockInfo.blockView.getBrightness(Direction.EAST, hasShade);
			div += normalX;
		} else if (normalX < 0) {
			sum += -normalX * blockInfo.blockView.getBrightness(Direction.WEST, hasShade);
			div -= normalX;
		}

		if (normalY > 0) {
			sum += normalY * blockInfo.blockView.getBrightness(Direction.UP, hasShade);
			div += normalY;
		} else if (normalY < 0) {
			sum += -normalY * blockInfo.blockView.getBrightness(Direction.DOWN, hasShade);
			div -= normalY;
		}

		if (normalZ > 0) {
			sum += normalZ * blockInfo.blockView.getBrightness(Direction.SOUTH, hasShade);
			div += normalZ;
		} else if (normalZ < 0) {
			sum += -normalZ * blockInfo.blockView.getBrightness(Direction.NORTH, hasShade);
			div -= normalZ;
		}

		return sum / div;
	}

	/**
	 * Handles geometry-based check for using self brightness or neighbor brightness.
	 * That logic only applies in flat lighting.
	 */
	private int flatBrightness(MutableQuadViewImpl quad, BlockState blockState, BlockPos pos) {
		lightPos.set(pos);

		// To mirror Vanilla's behavior, if the face has a cull-face, always sample the light value
		// offset in that direction. See net.minecraft.client.render.block.BlockModelRenderer.renderQuadsFlat
		// for reference.
		if (quad.cullFace() != null) {
			lightPos.move(quad.cullFace());
		} else {
			final int flags = quad.geometryFlags();

			if ((flags & LIGHT_FACE_FLAG) != 0 || ((flags & AXIS_ALIGNED_FLAG) != 0 && blockState.isFullCube(blockInfo.blockView, pos))) {
				lightPos.move(quad.lightFace());
			}
		}

		// Unfortunately cannot use brightness cache here unless we implement one specifically for flat lighting. See #329
		return WorldRenderer.getLightmapCoordinates(blockInfo.blockView, blockState, lightPos);
	}
}
