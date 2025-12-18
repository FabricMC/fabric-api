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

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.util.TriState;

/**
 * Interface for reading quad data encoded in {@linkplain Mesh Meshes}.
 * Enables models to do analysis, re-texturing or translation without knowing the
 * renderer's vertex formats and without retaining redundant information.
 *
 * <p>Unless otherwise stated, assume all properties persist through serialization into {@linkplain Mesh Meshes} and have an
 * effect in both block and item contexts. If a property is described as transient, then its value will not persist
 * through serialization into a {@link Mesh}.
 *
 * <p>Only the renderer should implement or extend this interface.
 */
public interface QuadView {
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
	 * <p>This method is equivalent to {@link BakedQuad#direction()}.
	 */
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
	 * @see MutableQuadView#renderLayer(ChunkSectionLayer)
	 */
	@Nullable
	ChunkSectionLayer renderLayer();

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
	 * @see MutableQuadView#glint(ItemStackRenderState.FoilType)
	 */
	ItemStackRenderState.@Nullable FoilType glint();

	/**
	 * @see MutableQuadView#shadeMode(ShadeMode)
	 */
	ShadeMode shadeMode();

	/**
	 * @see MutableQuadView#atlas(QuadAtlas)
	 */
	QuadAtlas atlas();

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
	 * Creates a new {@link BakedQuad} with an appearance as close as possible to this quad, as permitted by vanilla.
	 *
	 * @param sprite The sprite is not serialized so it must be provided by the caller. Retrieve it using
	 * {@link SpriteFinder#find(QuadView)} if it is not already known.
	 */
	default BakedQuad toBakedQuad(TextureAtlasSprite sprite) {
		Vector3f position0 = copyPos(0, null);
		Vector3f position1 = copyPos(1, null);
		Vector3f position2 = copyPos(2, null);
		Vector3f position3 = copyPos(3, null);
		long packedUV0 = UVPair.pack(u(0), v(0));
		long packedUV1 = UVPair.pack(u(1), v(1));
		long packedUV2 = UVPair.pack(u(2), v(2));
		long packedUV3 = UVPair.pack(u(3), v(3));

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

				int blockLight = LightTexture.block(lightmap);
				int skyLight = LightTexture.sky(lightmap);
				lightEmission = Math.min(lightEmission, Math.min(blockLight, skyLight));
			}
		}

		return new BakedQuad(
				position0,
				position1,
				position2,
				position3,
				packedUV0,
				packedUV1,
				packedUV2,
				packedUV3,
				tintIndex(),
				lightFace(),
				sprite,
				diffuseShade(),
				lightEmission
		);
	}
}
