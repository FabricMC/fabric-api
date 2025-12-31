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
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.renderer.v1.render.FabricLayerRenderState;
import net.fabricmc.fabric.api.renderer.v1.render.ItemRenderTypeGetter;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.fabric.impl.renderer.QuadSpriteBaker;

/// A mutable [QuadView] instance. The base interface for
/// [QuadEmitter] and for dynamic renders/mesh transforms.
///
/// Instances of [MutableQuadView] will practically always be
/// thread local and/or reused - do not retain references.
///
/// Unless otherwise stated, assume all properties persist through serialization into [Mesh]es and have an
/// effect in both block and item contexts. If a property is described as transient, then its value will not persist
/// through serialization into a [Mesh].
///
/// Only the renderer should implement or extend this interface.
public interface MutableQuadView extends QuadView {
	/// When enabled, causes texture to appear with no rotation. This is the default and does not have to be specified
	/// explicitly. Can be overridden by other rotation flags.
	/// Pass in bakeFlags parameter to [#spriteBake(TextureAtlasSprite, int)].
	int BAKE_ROTATE_NONE = 0;

	/// When enabled, causes texture to appear rotated 90 degrees clockwise.
	/// Pass in bakeFlags parameter to [#spriteBake(TextureAtlasSprite, int)].
	int BAKE_ROTATE_90 = 1;

	/// When enabled, causes texture to appear rotated 180 degrees.
	/// Pass in bakeFlags parameter to [#spriteBake(TextureAtlasSprite, int)].
	int BAKE_ROTATE_180 = 2;

	/// When enabled, causes texture to appear rotated 270 degrees clockwise.
	/// Pass in bakeFlags parameter to [#spriteBake(TextureAtlasSprite, int)].
	int BAKE_ROTATE_270 = 3;

	/// When enabled, texture coordinates are assigned based on vertex positions and the
	/// {@linkplain #nominalFace() nominal face}.
	/// Any existing UV coordinates will be replaced and the [#BAKE_NORMALIZED] flag will be ignored.
	/// Pass in bakeFlags parameter to [#spriteBake(TextureAtlasSprite, int)].
	///
	/// UV lock derives texture coordinates based on {@linkplain #nominalFace() nominal face} by projecting the quad
	/// onto it, even when the quad is not co-planar with it. This flag is ignored if the normal face is `null`.
	int BAKE_LOCK_UV = 4;

	/// When enabled, U texture coordinates for the given sprite are
	/// flipped as part of baking. Can be useful for some randomization
	/// and texture mapping scenarios. Results are different from what
	/// can be obtained via rotation and both can be applied. Any
	/// rotation is applied before this flag.
	/// Pass in bakeFlags parameter to [#spriteBake(TextureAtlasSprite, int)].
	int BAKE_FLIP_U = 8;

	/// Same as [#BAKE_FLIP_U] but for V coordinate.
	int BAKE_FLIP_V = 16;

	/// UV coordinates by default are assumed to be 0-16 scale for consistency
	/// with conventional Minecraft model format. This is scaled to 0-1 during
	/// baking before interpolation. Model loaders that already have 0-1 coordinates
	/// can avoid wasteful multiplication/division by passing 0-1 coordinates directly.
	/// Pass in bakeFlags parameter to [#spriteBake(TextureAtlasSprite, int)].
	int BAKE_NORMALIZED = 32;

	/// Sets the geometric vertex position for the given vertex,
	/// relative to block origin, (0,0,0). Minecraft rendering is designed
	/// for models that fit within a single block space and is recommended
	/// that coordinates remain in the 0-1 range, with multi-block meshes
	/// split into multiple per-block models.
	///
	/// The default value for all vertices is `0.0f`.
	MutableQuadView pos(int vertexIndex, float x, float y, float z);

	/// Sets the geometric position for the given vertex. Only use this method if you already have a [Vector3f].
	/// Otherwise, use [#pos(int, float, float, float)].
	default MutableQuadView pos(int vertexIndex, Vector3f pos) {
		return pos(vertexIndex, pos.x, pos.y, pos.z);
	}

	/// Sets the geometric position for the given vertex. Only use this method if you already have a [Vector3fc].
	/// Otherwise, use [#pos(int, float, float, float)].
	default MutableQuadView pos(int vertexIndex, Vector3fc pos) {
		return pos(vertexIndex, pos.x(), pos.y(), pos.z());
	}

	/// Sets the color in ARGB format (0xAARRGGBB) for the given vertex.
	///
	/// The default value for all vertices is `0xFFFFFFFF`.
	MutableQuadView color(int vertexIndex, int color);

	/// Sets the color in ARGB format (0xAARRGGBB) for all vertices at once.
	///
	/// @see #color(int, int)
	default MutableQuadView color(int c0, int c1, int c2, int c3) {
		color(0, c0);
		color(1, c1);
		color(2, c2);
		color(3, c3);
		return this;
	}

	/// Sets the texture coordinates for the given vertex.
	///
	/// The default value for all vertices is `0.0f`.
	MutableQuadView uv(int vertexIndex, float u, float v);

	/// Sets the texture coordinates for the given vertex. Only use this method if you already have a [Vector2f].
	/// Otherwise, use [#uv(int, float, float)].
	default MutableQuadView uv(int vertexIndex, Vector2f uv) {
		return uv(vertexIndex, uv.x, uv.y);
	}

	/// Sets the texture coordinates for the given vertex. Only use this method if you already have a [Vector2fc].
	/// Otherwise, use [#uv(int, float, float)].
	default MutableQuadView uv(int vertexIndex, Vector2fc uv) {
		return uv(vertexIndex, uv.x(), uv.y());
	}

	/// Sets the texture coordinates for all vertices using the given sprite. Also sets this quad's atlas to the given
	/// sprite's atlas. Can handle UV locking, rotation, interpolation, etc. Control this behavior by passing additive
	/// combinations of the BAKE_ flags defined in this interface.
	default MutableQuadView spriteBake(TextureAtlasSprite sprite, int bakeFlags) {
		QuadSpriteBaker.bakeSprite(this, sprite, bakeFlags);
		QuadAtlas atlas = QuadAtlas.of(sprite.atlasLocation());

		if (atlas == null) {
			atlas = QuadAtlas.BLOCK;
		}

		atlas(atlas);
		return this;
	}

	/// Sets the minimum lightmap value for the given vertex. Input values will override lightmap values computed from
	/// level state if input values are higher. Exposed for completeness but some rendering implementations with
	/// non-standard lighting model may not honor it.
	///
	/// For emissive rendering, prefer using [#emissive(boolean)].
	///
	/// The default value for all vertices is `0`.
	MutableQuadView lightmap(int vertexIndex, int lightmap);

	/// Sets the lightmap value for all vertices at once.
	///
	/// For emissive rendering, prefer using [#emissive(boolean)].
	///
	/// @see #lightmap(int, int)
	default MutableQuadView lightmap(int l0, int l1, int l2, int l3) {
		lightmap(0, l0);
		lightmap(1, l1);
		lightmap(2, l2);
		lightmap(3, l3);
		return this;
	}

	/// Sets the normal vector for the given vertex. The {@linkplain #faceNormal() face normal} is used when no vertex
	/// normal is provided. Models that have per-vertex normals should include them to get correct lighting when it
	/// matters.
	MutableQuadView normal(int vertexIndex, float x, float y, float z);

	/// Sets the normal vector for the given vertex. Only use this method if you already have a [Vector3f].
	/// Otherwise, use [#normal(int, float, float, float)].
	default MutableQuadView normal(int vertexIndex, Vector3f normal) {
		return normal(vertexIndex, normal.x, normal.y, normal.z);
	}

	/// Sets the normal vector for the given vertex. Only use this method if you already have a [Vector3fc].
	/// Otherwise, use [#normal(int, float, float, float)].
	default MutableQuadView normal(int vertexIndex, Vector3fc normal) {
		return normal(vertexIndex, normal.x(), normal.y(), normal.z());
	}

	/// Sets the nominal face, which provides a hint to the renderer about the facing of this quad. It is not required,
	/// but if set, should be the expected value of [#lightFace()]. It may be used to shortcut geometric analysis,
	/// if the provided value was correct; otherwise, it is ignored.
	///
	/// The nominal face is also used for [#spriteBake(TextureAtlasSprite, int)] with [#BAKE_LOCK_UV].
	///
	/// When [#cullFace(Direction)] is called, it also sets the nominal face.
	///
	/// The default value is `null`.
	///
	/// This property is transient. It is set to the same value as [#lightFace()] when a quad is decoded.
	MutableQuadView nominalFace(@Nullable Direction face);

	/// Sets the cull face. This quad will not be rendered if its cull face is non-null and the block is occluded by
	/// another block in the direction of the cull face.
	///
	/// The cull face is different from [BakedQuad#direction()], which is equivalent to [#lightFace()]. The
	/// light face is computed based on geometry and must be non-null.
	///
	/// When called, sets [#nominalFace(Direction)] to the same value.
	///
	/// The default value is `null`.
	///
	/// This property is respected only in block contexts. It will not have an effect in other contexts.
	MutableQuadView cullFace(@Nullable Direction face);

	/// Controls how this quad's pixels should be blended with the scene.
	///
	/// If set to `null`, [ItemBlockRenderTypes#getChunkRenderType(BlockState)] will be used to retrieve
	/// the {@linkplain ChunkSectionLayer chunk layer} in block contexts. Set to another value to override this behavior.
	///
	/// In block contexts, a non-null value will be used directly. In item contexts, any value will be converted to a
	/// [RenderType] using [FabricLayerRenderState#setRenderTypeGetter(ItemRenderTypeGetter)].
	///
	/// The default value is `null`.
	MutableQuadView chunkLayer(@Nullable ChunkSectionLayer layer);

	/// When true, this quad will be rendered at full brightness.
	/// Lightmap values provided via [QuadEmitter#lightmap(int)] will be ignored.
	///
	/// This is the preferred method for emissive lighting effects as some renderers
	/// with advanced lighting pipelines may not use lightmaps.
	///
	/// Note that vertex colors will still be modified by diffuse shading and ambient occlusion, unless disabled via
	/// [#diffuseShade(boolean)] and [#ambientOcclusion(TriState)].
	///
	/// The default value is `false`.
	MutableQuadView emissive(boolean emissive);

	/// Controls whether vertex colors should be modified for diffuse shading.
	///
	/// The default value is `true`.
	///
	/// This property is guaranteed to be respected in block contexts. Some renderers may also respect it in item
	/// contexts, but this is not guaranteed.
	MutableQuadView diffuseShade(boolean shade);

	/// Controls whether vertex colors should be modified for ambient occlusion.
	///
	/// If set to [TriState#DEFAULT], ambient occlusion will be used if the block state has
	/// {@linkplain BlockState#getLightEmission() a luminance} of 0. Set to [TriState#TRUE] or [TriState#FALSE]
	/// to override this behavior. [TriState#TRUE] will not have an effect if
	/// {@linkplain Minecraft#useAmbientOcclusion() ambient occlusion is disabled globally}.
	///
	/// The default value is [TriState#DEFAULT].
	///
	/// This property is respected only in block contexts. It will not have an effect in other contexts.
	MutableQuadView ambientOcclusion(TriState ao);

	/// Controls how foil (also known as glint) should be applied.
	///
	/// If set to `null`, foil will be applied in item contexts based on
	/// {@linkplain ItemStackRenderState.LayerRenderState#setFoilType(ItemStackRenderState.FoilType) the foil type of the layer}. Set
	/// to another value to override this behavior.
	///
	/// The default value is `null`.
	///
	/// This property is guaranteed to be respected in item contexts. Some renderers may also respect it in block
	/// contexts, but this is not guaranteed.
	MutableQuadView foilType(ItemStackRenderState.@Nullable FoilType foilType);

	/// A hint to the renderer about how this quad is intended to be shaded, for example through ambient occlusion and
	/// diffuse shading. The renderer is free to ignore this hint.
	///
	/// The default value is [ShadeMode#ENHANCED].
	///
	/// This property is respected only in block contexts. It will not have an effect in other contexts.
	///
	/// @see ShadeMode
	MutableQuadView shadeMode(ShadeMode mode);

	/// Sets the {@linkplain QuadAtlas atlas texture} used by this quad.
	///
	/// In block contexts, this property must be [QuadAtlas#BLOCK]. In item contexts, this property will be
	/// converted to a [RenderType] using [FabricLayerRenderState#setRenderTypeGetter(ItemRenderTypeGetter)].
	///
	/// The default value is [QuadAtlas#BLOCK].
	///
	/// @see QuadAtlas
	MutableQuadView atlas(QuadAtlas quadAtlas);

	/// Sets the tint index, which is used to retrieve the tint color.
	///
	/// The default value is `-1`.
	MutableQuadView tintIndex(int tintIndex);

	/// Sets the tag, which is an arbitrary integer that is meant to be encoded into [Mesh]es to later allow
	/// performing conditional transformation or filtering on their quads.
	///
	/// The default value is `0`.
	MutableQuadView tag(int tag);

	/// Copies all quad data and properties from the given [QuadView] to this quad.
	///
	/// Calling this method does not emit this quad.
	MutableQuadView copyFrom(QuadView quad);

	/// Sets all applicable data and properties of this quad as specified by the given [BakedQuad]. In addition,
	/// this quad's vertex colors and vertex normals will be reset. This quad's existing lightmap values will be ignored.
	///
	/// Calling this method does not emit this quad.
	MutableQuadView fromBakedQuad(BakedQuad quad);
}
