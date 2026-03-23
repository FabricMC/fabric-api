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

package net.fabricmc.fabric.test.renderer.client;

import java.util.Objects;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadAtlas;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.MeshQuadCollection;
import net.fabricmc.fabric.api.client.renderer.v1.model.ModelHelper;

public record OverlayedGeometry(Identifier parentId) implements UnbakedGeometry {
	@Override
	public QuadCollection bake(TextureSlots textures, ModelBaker modelBaker, ModelState modelState, ModelDebugName name) {
		MutableMesh mutableMesh = Renderer.get().mutableMesh();
		QuadEmitter emitter = mutableMesh.emitter();

		ResolvedModel parentModel = modelBaker.getModel(parentId);
		QuadCollection parentQuads = parentModel.bakeTopGeometry(parentModel.getTopTextureSlots(), modelBaker, modelState);
		Material.Baked overlayMaterial = modelBaker.materials()
				.get(Objects.requireNonNull(textures.getMaterial("overlay")), name);
		TextureAtlasSprite overlaySprite = overlayMaterial.sprite();
		QuadAtlas overlayAtlas = QuadAtlas.ofLocation(overlaySprite.atlasLocation());

		if (overlayAtlas == null) {
			return parentQuads;
		}

		if (parentQuads instanceof MeshQuadCollection meshQuadCollection) {
			meshQuadCollection.getMesh().forEach(quad -> {
				emitter.copyFrom(quad).emit();
				emitter.copyFrom(quad);
				emitter.materialBake(overlayMaterial, MutableQuadView.BAKE_LOCK_UV);
				emitter.emit();
			});
		} else {
			for (int i = 0; i < ModelHelper.NULL_FACE_ID; i++) {
				Direction cullFace = ModelHelper.faceFromIndex(i);

				for (BakedQuad bakedQuad : parentQuads.getQuads(cullFace)) {
					emitter.fromBakedQuad(bakedQuad).cullFace(cullFace).emit();
					emitter.fromBakedQuad(bakedQuad).cullFace(cullFace);
					emitter.materialBake(overlayMaterial, MutableQuadView.BAKE_LOCK_UV);
					emitter.emit();
				}
			}
		}

		return new MeshQuadCollection(mutableMesh.immutableCopy());
	}
}
