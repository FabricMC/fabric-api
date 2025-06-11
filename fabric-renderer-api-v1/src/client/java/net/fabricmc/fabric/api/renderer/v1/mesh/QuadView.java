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

package net.fabricmc.fabric.api.renderer.v1.mesh;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;

import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.util.TriState;

/**
 * Interface for reading quad data encoded in {@link Mesh}es.
 * Enables models to do analysis, re-texturing or translation without knowing the
 * renderer's vertex formats and without retaining redundant information.
 *
 * <p>Unless otherwise stated, assume all properties persist through serialization into {@link Mesh}es and have an
 * effect in both block and item contexts. If a property is described as transient, then its value will not persist
 * through serialization into a {@link Mesh}.
 *
 * <p>Only the renderer should implement or extend this interface.
 */
public interface QuadView {
	/** Count of integers in a conventional (un-modded) block or item vertex. */
	int VANILLA_VERTEX_STRIDE = VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSize() / 4;

	/** Count of integers in a conventional (un-modded) block or item quad. */
	int VANILLA_QUAD_STRIDE = VANILLA_VERTEX_STRIDE * 4;

	/**
	 * Gets the X coordinate of the geometric position of the given vertex.
	 */
	float x(int vertexIndex);

	/**
	 * Gets the Y coordinate of the geometric position of the given vertex.
	 */
	float y(int vertexIndex);

	/**
	 * Gets the Z coordinate of the geometric position of the given vertex.
	 */
	float z(int vertexIndex);

	/**
	 * Gets the specified coordinate of the geometric position of the given vertex. Index 0 is X, 1 is Y, and 2 is Z.
	 */
	float posByIndex(int vertexIndex, int coordinateIndex);

	/**
	 * Copies the geometric position of the given vertex to the given target. If the target is {@code null}, a new
	 * {@link Vector3f} will be allocated and returned.
	 */
	Vector3f copyPos(int vertexIndex, @Nullable Vector3f target);

	/**
	 * Gets the vertex color in ARGB format (0xAARRGGBB) of the given vertex.
	 */
	int color(int vertexIndex);

	/**
	 * Gets the horizontal texture coordinates of the given vertex.
	 */
	float u(int vertexIndex);

	/**
	 * Gets the vertical texture coordinates of the given vertex.
	 */
	float v(int vertexIndex);

	/**
	 * Copies the texture coordinates of the given vertex to the given target. If the target is {@code null}, a new
	 * {@link Vector2f} will be allocated and returned.
	 */
	Vector2f copyUv(int vertexIndex, @Nullable Vector2f target);

	/**
	 * Gets the minimum lightmap value of the given vertex.
	 */
	int lightmap(int vertexIndex);

	/**
	 * Returns whether a normal vector is present for the given vertex. If not, the vertex implicitly uses the
	 * {@linkplain #faceNormal() face normal}.
	 */
	boolean hasNormal(int vertexIndex);

	/**
	 * Gets the X coordinate of the normal vector of the given vertex. Returns {@link Float#NaN} if the
	 * {@linkplain #hasNormal(int) normal is not present}.
	 */
	float normalX(int vertexIndex);

	/**
	 * Gets the Y coordinate of the normal vector of the given vertex. Returns {@link Float#NaN} if the
	 * {@linkplain #hasNormal(int) normal is not present}.
	 */
	float normalY(int vertexIndex);

	/**
	 * Gets the Z coordinate of the normal vector of the given vertex. Returns {@link Float#NaN} if the
	 * {@linkplain #hasNormal(int) normal is not present}.
	 */
	float normalZ(int vertexIndex);

	/**
	 * Copies the normal vector of the given vertex to the given target, if the vertex
	 * {@linkplain #hasNormal(int) has a normal}. Otherwise, returns {@code null}. If the target is {@code null} and a
	 * normal exists, a new {@link Vector3f} will be allocated and returned.
	 */
	@Nullable
	Vector3f copyNormal(int vertexIndex, @Nullable Vector3f target);

	/**
	 * Gets the normal vector of this quad as implied by its vertex positions. It will be invalid if the vertices are
	 * not co-planar.
	 */
	Vector3fc faceNormal();

	/**
	 * Gets the light face of this quad as implied by its {@linkplain #faceNormal() face normal}. It is equal to the
	 * axis-aligned direction closest to the face normal, and is never {@code null}.
	 *
	 * <p>This method is equivalent to {@link BakedQuad#face()}.
	 */
	@NotNull
	Direction lightFace();

	/**
	 * @see MutableQuadView#nominalFace(Direction)
	 */
	@Nullable
	Direction nominalFace();

	/**
	 * @see MutableQuadView#cullFace(Direction)
	 */
	@Nullable
	Direction cullFace();

	/**
	 * @see MutableQuadView#renderLayer(BlockRenderLayer)
	 */
	@Nullable
	BlockRenderLayer renderLayer();

	/**
	 * @see MutableQuadView#emissive(boolean)
	 */
	boolean emissive();

	/**
	 * This method is equivalent to {@link BakedQuad#shade()}.
	 *
	 * @see MutableQuadView#diffuseShade(boolean)
	 */
	boolean diffuseShade();

	/**
	 * @see MutableQuadView#ambientOcclusion(TriState)
	 */
	TriState ambientOcclusion();

	/**
	 * @see MutableQuadView#glint(ItemRenderState.Glint)
	 */
	@Nullable
	ItemRenderState.Glint glint();

	/**
	 * @see MutableQuadView#shadeMode(ShadeMode)
	 */
	ShadeMode shadeMode();

	/**
	 * This method is equivalent to {@link BakedQuad#tintIndex()}.
	 *
	 * @see MutableQuadView#tintIndex(int)
	 */
	int tintIndex();

	/**
	 * @see MutableQuadView#tag(int)
	 */
	int tag();

	/**
	 * Outputs this quad's vertex data into the given array, starting at the given index. The array must have at least
	 * {@link #VANILLA_QUAD_STRIDE} elements available starting at the given index. The format of the data is the same
	 * as {@link BakedQuad#vertexData()}. Lightmap values and normals will be populated even though vanilla does not use
	 * them.
	 */
	void toVanilla(int[] target, int startIndex);

	/**
	 * Creates a new {@link BakedQuad} with an appearance as close as possible to this quad, as permitted by vanilla.
	 * Vertex lightmap values and vertex normals will be populated even though vanilla does not use them.
	 *
	 * @param sprite The sprite is not serialized so it must be provided by the caller. Retrieve it using
	 * {@link SpriteFinder#find(QuadView)} if it is not already known.
	 */
	default BakedQuad toBakedQuad(Sprite sprite) {
		int[] vertexData = new int[VANILLA_QUAD_STRIDE];
		toVanilla(vertexData, 0);

		// The light emission is set to 15 if the quad is emissive; otherwise, to the minimum of all four sky light
		// values and all four block light values.
		int lightEmission = 15;

		if (!emissive()) {
			for (int i = 0; i < 4; i++) {
				int lightmap = lightmap(i);

				if (lightmap == 0) {
					lightEmission = 0;
					break;
				}

				int blockLight = LightmapTextureManager.getBlockLightCoordinates(lightmap);
				int skyLight = LightmapTextureManager.getSkyLightCoordinates(lightmap);
				lightEmission = Math.min(lightEmission, Math.min(blockLight, skyLight));
			}
		}

		return new BakedQuad(vertexData, tintIndex(), lightFace(), sprite, diffuseShade(), lightEmission);
	}
}
