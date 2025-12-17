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

import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.core.Direction;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.MeshBakedGeometry;
import net.fabricmc.fabric.api.renderer.v1.model.ModelBakeSettingsHelper;

public record PillarGeometry() implements UnbakedGeometry {
	@Override
	public QuadCollection bake(TextureSlots textures, ModelBaker baker, ModelState settings, ModelDebugName model) {
		MutableMesh builder = Renderer.get().mutableMesh();
		QuadEmitter emitter = builder.emitter();
		emitter.pushTransform(ModelBakeSettingsHelper.asQuadTransform(settings, baker.sprites()));

		TextureAtlasSprite sprite = baker.sprites().get(textures.getMaterial("pillar"), model);

		for (Direction side : Direction.values()) {
			emitter.square(side, 0, 0, 1, 1, 0);
			emitter.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV);
			emitter.emit();
		}

		return new MeshBakedGeometry(builder.immutableCopy());
	}
}
