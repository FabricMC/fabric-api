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

package net.fabricmc.fabric.api.client.renderer.v1.model;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.Transparency;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.Material;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadAtlas;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.client.renderer.v1.sprite.SpriteFinder;

/**
 * Collection of utilities for model implementations.
 */
public final class ModelHelper {
	/** @see #faceFromIndex(int) */
	private static final Direction[] FACES = Arrays.copyOf(Direction.values(), 7);

	/** Result from {@link #toFaceIndex(Direction)} for null values. */
	public static final int NULL_FACE_ID = 6;

	private ModelHelper() { }

	/**
	 * Computes a {@link BakedQuad.SpriteInfo} for a {@link Material.Baked}.
	 *
	 * <p><b>Warning</b>: do not call this method while rendering as it does not cache the result.
	 * This method is meant primarily for baking.
	 */
	public static BakedQuad.SpriteInfo computeSpriteInfo(Material.Baked material, QuadView quad) {
		Transparency transparency = material.forceTranslucent() ? Transparency.TRANSLUCENT : computeTransparency(material.sprite(), quad);
		// TODO: Consider interning or some other caching scheme to reduce object churn
		return BakedQuad.SpriteInfo.of(material, transparency);
	}

	/**
	 * Computes a {@link BakedQuad.SpriteInfo} for a {@link TextureAtlasSprite}.
	 *
	 * <p><b>Warning</b>: do not call this method while rendering as it does not cache the result.
	 * This method is meant primarily for baking.
	 */
	public static BakedQuad.SpriteInfo computeSpriteInfo(TextureAtlasSprite sprite, QuadView quad) {
		Transparency transparency = computeTransparency(sprite, quad);
		ChunkSectionLayer chunkLayer = ChunkSectionLayer.byTransparency(transparency);
		RenderType itemRenderType;

		if (sprite.atlasLocation().equals(TextureAtlas.LOCATION_BLOCKS)) {
			itemRenderType = transparency.hasTranslucent() ? Sheets.translucentBlockItemSheet() : Sheets.cutoutBlockItemSheet();
		} else {
			itemRenderType = transparency.hasTranslucent() ? Sheets.translucentItemSheet() : Sheets.cutoutItemSheet();
		}

		// TODO: Consider interning or some other caching scheme to reduce object churn
		return new BakedQuad.SpriteInfo(sprite, chunkLayer, itemRenderType);
	}

	/**
	 * Computes {@link Transparency} for a {@link TextureAtlasSprite}.
	 *
	 * <p><b>Warning</b>: do not call this method while rendering as it does not cache the result.
	 * This method is meant primarily for baking.
	 */
	public static Transparency computeTransparency(TextureAtlasSprite sprite, QuadView quad) {
		// Find the minimum and maximum UV's.
		// A simulation of the algorithm is provided in comments below.
		float minU = Float.MAX_VALUE; // 7, 7, 5, 3
		float minV = Float.MAX_VALUE; // 9, 8, 8, 8
		float maxU = 0; // 7, 12, 12, 12
		float maxV = 0; // 9, 9,  10, 13

		// u, v
		// 7, 9
		// 12,8
		// 5, 10
		// 3, 13

		for (int i = 0; i < 4; i++) {
			float u = quad.u(i);
			float v = quad.v(i);

			if (u < minU) {
				minU = u;
			}

			if (u > maxU) {
				maxU = u;
			}

			if (v < minV) {
				minV = v;
			}

			if (v > maxV) {
				maxV = v;
			}
		}

		// Normalize UVs
		// Inverse linear interpolation
		// `(t_q - t_0)/Δt` where `t_q` is the value and `t` is `u` or `v`
		final float width = 1.0f / (sprite.getU1() - sprite.getU0());
		final float height = 1.0f / (sprite.getV1() - sprite.getV0());
		minU = (minU - sprite.getU0()) * width;
		minV = (minV - sprite.getV0()) * height;
		maxU = (maxU - sprite.getU0()) * width;
		maxV = (maxV - sprite.getV0()) * height;

		// See FaceBakery#computeMaterialTransparency
		return sprite
				.contents()
				.computeTransparency(minU, minV, maxU, maxV);
	}

	public static void setSpriteInfo(MutableQuadView quad, Material.Baked material) {
		QuadAtlas atlas = QuadAtlas.ofLocation(material.sprite().atlasLocation());

		if (atlas != null) {
			quad.atlas(atlas);
		}

		BakedQuad.SpriteInfo spriteInfo = computeSpriteInfo(material, quad);
		quad.chunkLayer(spriteInfo.layer());
		quad.itemRenderType(spriteInfo.itemRenderType());
	}

	/**
	 * Convenient way to encode faces that may be null.
	 * Null is returned as {@link #NULL_FACE_ID}.
	 * Use {@link #faceFromIndex(int)} to retrieve encoded face.
	 */
	public static int toFaceIndex(@Nullable Direction face) {
		return face == null ? NULL_FACE_ID : face.get3DDataValue();
	}

	/**
	 * Use to decode a result from {@link #toFaceIndex(Direction)}.
	 * Return value will be null if encoded value was null.
	 * Can also be used for no-allocation iteration of {@link Direction#values()},
	 * optionally including the null face. (Use &lt; or  &lt;= {@link #NULL_FACE_ID}
	 * to exclude or include the null value, respectively.)
	 */
	@Nullable
	public static Direction faceFromIndex(int faceIndex) {
		return FACES[faceIndex];
	}

	/**
	 * Converts a mesh into an array of lists of vanilla baked quads.
	 * Useful for creating vanilla baked models when required for compatibility.
	 * The array indexes correspond to {@link Direction#getName()} with the
	 * addition of {@link #NULL_FACE_ID}.
	 *
	 * <p>Retrieves sprites from the block texture atlas via {@link SpriteFinder}.
	 */
	public static List<BakedQuad>[] toQuadLists(Mesh mesh) {
		SpriteFinder finder = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).spriteFinder();

		@SuppressWarnings("unchecked")
		final ImmutableList.Builder<BakedQuad>[] builders = new ImmutableList.Builder[7];

		for (int i = 0; i < 7; i++) {
			builders[i] = ImmutableList.builder();
		}

		mesh.forEach(q -> {
			Direction cullFace = q.cullFace();
			builders[cullFace == null ? NULL_FACE_ID : cullFace.get3DDataValue()].add(q.toBakedQuad(finder.find(q)));
		});

		@SuppressWarnings("unchecked")
		List<BakedQuad>[] result = new List[7];

		for (int i = 0; i < 7; i++) {
			result[i] = builders[i].build();
		}

		return result;
	}
}
