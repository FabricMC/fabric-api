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

import net.minecraft.client.render.model.BakedGeometry;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.Geometry;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.render.model.SimpleModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.math.Direction;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.MeshBakedGeometry;
import net.fabricmc.fabric.api.renderer.v1.model.ModelBakeSettingsHelper;

public record FrameGeometry(boolean emissive) implements Geometry {
	@Override
	public BakedGeometry bake(ModelTextures textures, Baker baker, ModelBakeSettings settings, SimpleModel model) {
		MutableMesh builder = Renderer.get().mutableMesh();
		QuadEmitter emitter = builder.emitter();
		emitter.pushTransform(ModelBakeSettingsHelper.asQuadTransform(settings, baker.getSpriteGetter().spriteFinder(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)));

		Sprite sprite = baker.getSpriteGetter().get(textures.get("frame"), model);

		for (Direction direction : Direction.values()) {
			// Draw outer frame
			emitter.square(direction, 0.0F, 0.9F, 0.9F, 1.0F, 0.0F)
					.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV)
					.emissive(emissive)
					.emit();

			emitter.square(direction, 0.0F, 0.0F, 0.1F, 0.9F, 0.0F)
					.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV)
					.emissive(emissive)
					.emit();

			emitter.square(direction, 0.9F, 0.1F, 1.0F, 1.0F, 0.0F)
					.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV)
					.emissive(emissive)
					.emit();

			emitter.square(direction, 0.1F, 0.0F, 1.0F, 0.1F, 0.0F)
					.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV)
					.emissive(emissive)
					.emit();

			// Draw inner frame - inset by 0.9 so the frame looks like an actual mesh
			emitter.square(direction, 0.0F, 0.9F, 0.9F, 1.0F, 0.9F)
					.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV)
					.emissive(emissive)
					.emit();

			emitter.square(direction, 0.0F, 0.0F, 0.1F, 0.9F, 0.9F)
					.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV)
					.emissive(emissive)
					.emit();

			emitter.square(direction, 0.9F, 0.1F, 1.0F, 1.0F, 0.9F)
					.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV)
					.emissive(emissive)
					.emit();

			emitter.square(direction, 0.1F, 0.0F, 1.0F, 0.1F, 0.9F)
					.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV)
					.emissive(emissive)
					.emit();
		}

		return new MeshBakedGeometry(builder.immutableCopy());
	}
}
