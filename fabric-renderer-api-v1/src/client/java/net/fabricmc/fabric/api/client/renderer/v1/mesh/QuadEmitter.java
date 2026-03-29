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

package net.fabricmc.fabric.api.client.renderer.v1.mesh;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;

import net.fabricmc.fabric.api.util.TriState;

/**
 * Specialized {@link MutableQuadView} that supports transformers and
 * sends quads to some destination, such as a mesh builder or rendering.
 *
 * <p>Instances of {@link QuadEmitter} will practically always be
 * thread local and/or reused - do not retain references.
 *
 * <p>Only the renderer should implement or extend this interface.
 */
public interface QuadEmitter extends MutableQuadView {
	@Override
	QuadEmitter pos(int vertexIndex, float x, float y, float z);

	@Override
	default QuadEmitter pos(int vertexIndex, Vector3f pos) {
		MutableQuadView.super.pos(vertexIndex, pos);
		return this;
	}

	@Override
	default QuadEmitter pos(int vertexIndex, Vector3fc pos) {
		MutableQuadView.super.pos(vertexIndex, pos);
		return this;
	}

	@Override
	default QuadEmitter translate(float x, float y, float z) {
		MutableQuadView.super.translate(x, y, z);
		return this;
	}

	@Override
	QuadEmitter color(int vertexIndex, int color);

	@Override
	default QuadEmitter color(int c0, int c1, int c2, int c3) {
		MutableQuadView.super.color(c0, c1, c2, c3);
		return this;
	}

	@Override
	default QuadEmitter multiplyColor(int color) {
		MutableQuadView.super.multiplyColor(color);
		return this;
	}

	@Override
	QuadEmitter uv(int vertexIndex, float u, float v);

	@Override
	default QuadEmitter uv(int vertexIndex, Vector2f uv) {
		MutableQuadView.super.uv(vertexIndex, uv);
		return this;
	}

	@Override
	default QuadEmitter uv(int vertexIndex, Vector2fc uv) {
		MutableQuadView.super.uv(vertexIndex, uv);
		return this;
	}

	@Override
	default MutableQuadView uvUnitSquare() {
		MutableQuadView.super.uvUnitSquare();
		return this;
	}

	@Override
	default QuadEmitter materialBake(Material.Baked material, int bakeFlags) {
		MutableQuadView.super.materialBake(material, bakeFlags);
		return this;
	}

	@Override
	default QuadEmitter postMaterialBake(Material.Baked material) {
		MutableQuadView.super.postMaterialBake(material);
		return this;
	}

	@Override
	QuadEmitter lightmap(int vertexIndex, int lightmap);

	@Override
	default QuadEmitter lightmap(int l0, int l1, int l2, int l3) {
		MutableQuadView.super.lightmap(l0, l1, l2, l3);
		return this;
	}

	default QuadEmitter minLightmap(int lightmap) {
		MutableQuadView.super.minLightmap(lightmap);
		return this;
	}

	@Override
	QuadEmitter normal(int vertexIndex, float x, float y, float z);

	@Override
	default QuadEmitter normal(int vertexIndex, Vector3f normal) {
		MutableQuadView.super.normal(vertexIndex, normal);
		return this;
	}

	@Override
	default QuadEmitter normal(int vertexIndex, Vector3fc normal) {
		MutableQuadView.super.normal(vertexIndex, normal);
		return this;
	}

	@Override
	QuadEmitter nominalFace(@Nullable Direction face);

	@Override
	QuadEmitter cullFace(@Nullable Direction face);

	@Override
	QuadEmitter atlas(QuadAtlas quadAtlas);

	@Override
	QuadEmitter chunkLayer(ChunkSectionLayer layer);

	@Override
	QuadEmitter itemRenderType(RenderType renderType);

	@Override
	QuadEmitter emissive(boolean emissive);

	@Override
	QuadEmitter diffuseShade(boolean shade);

	@Override
	QuadEmitter ambientOcclusion(TriState ao);

	@Override
	QuadEmitter foilType(ItemStackRenderState.@Nullable FoilType foilType);

	@Override
	QuadEmitter shadeMode(ShadeMode mode);

	@Override
	QuadEmitter animated(boolean animated);

	@Override
	QuadEmitter tintIndex(int tintIndex);

	@Override
	QuadEmitter tag(int tag);

	@Override
	QuadEmitter copyFrom(QuadView quad);

	@Override
	QuadEmitter fromBakedQuad(BakedQuad quad);

	@Override
	QuadEmitter clear();

	@Override
	default QuadEmitter square(Direction nominalFace, float left, float bottom, float right, float top, float depth) {
		MutableQuadView.super.square(nominalFace, left, bottom, right, top, depth);
		return this;
	}

	/**
	 * Pushed transforms will be applied immediately after every call to {@link #emit()} and before the quad data is
	 * delivered to its destination. If any transform returns {@code false}, the emitted quad will be discarded and will
	 * not be delivered to its destination.
	 *
	 * <p>You MUST call {@link #popTransform()} once you are done using this emitter in the current scope.
	 *
	 * <p>More than one transformer can be pushed. Transformers are applied in reverse order. (Last pushed is applied
	 * first.)
	 *
	 * <p>Using {@code this} emitter from inside the pushed quad transform is not supported.
	 */
	void pushTransform(QuadTransform transform);

	/**
	 * Removes the transformer added by the last call to {@link #pushTransform(QuadTransform)}. MUST be called once you
	 * are done using this emitter in the current scope.
	 */
	void popTransform();

	/**
	 * In static mesh building, causes quad to be appended to the mesh being built. In a dynamic render context, create
	 * a new quad to be output to rendering. In both cases, current instance is reset to default values.
	 */
	QuadEmitter emit();
}
