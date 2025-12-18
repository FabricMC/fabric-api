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

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadAtlas;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.MeshBakedGeometry;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;

public record OverlayedGeometry(Identifier parentId) implements UnbakedGeometry {
	@Override
	public QuadCollection bake(TextureSlots textures, ModelBaker modelBaker, ModelState modelState, ModelDebugName name) {
		MutableMesh mutableMesh = Renderer.get().mutableMesh();
		QuadEmitter emitter = mutableMesh.emitter();

		ResolvedModel parentModel = modelBaker.getModel(parentId);
		QuadCollection parentQuads = parentModel.bakeTopGeometry(parentModel.getTopTextureSlots(), modelBaker, modelState);
		TextureAtlasSprite overlaySprite = modelBaker.sprites().get(textures.getMaterial("overlay"), name);
		QuadAtlas overlayAtlas = QuadAtlas.of(overlaySprite.atlasLocation());

		if (overlayAtlas == null) {
			return parentQuads;
		}

		if (parentQuads instanceof MeshBakedGeometry meshBakedGeometry) {
			meshBakedGeometry.getMesh().forEach(quad -> {
				emitter.copyFrom(quad).emit();
				emitter.copyFrom(quad);
				emitter.atlas(overlayAtlas);

				TextureAtlasSprite sprite = modelBaker.sprites().spriteFinder(emitter.atlas()).find(emitter);

				for (int i = 0; i < 4; i++) {
					emitter.uv(
							i,
							overlaySprite.getU(Mth.inverseLerp(emitter.u(i), sprite.getU0(), sprite.getU1())),
							overlaySprite.getV(Mth.inverseLerp(emitter.v(i), sprite.getV0(), sprite.getV1()))
					);
				}

				emitter.emit();
			});
		} else {
			for (int i = 0; i < ModelHelper.NULL_FACE_ID; i++) {
				Direction cullFace = ModelHelper.faceFromIndex(i);

				for (BakedQuad bakedQuad : parentQuads.getQuads(cullFace)) {
					emitter.fromBakedQuad(bakedQuad).cullFace(cullFace).emit();
					emitter.fromBakedQuad(bakedQuad).cullFace(cullFace);
					emitter.atlas(overlayAtlas);

					TextureAtlasSprite sprite = bakedQuad.sprite();

					for (int j = 0; j < 4; j++) {
						emitter.uv(
								j,
								overlaySprite.getU(Mth.inverseLerp(emitter.u(j), sprite.getU0(), sprite.getU1())),
								overlaySprite.getV(Mth.inverseLerp(emitter.v(j), sprite.getV0(), sprite.getV1()))
						);
					}

					emitter.emit();
				}
			}
		}

		return new MeshBakedGeometry(mutableMesh.immutableCopy());
	}
}
