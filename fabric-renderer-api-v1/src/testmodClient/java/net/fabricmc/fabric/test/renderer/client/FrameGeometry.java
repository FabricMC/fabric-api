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
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;

import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.MeshQuadCollection;
import net.fabricmc.fabric.api.client.renderer.v1.model.ModelStateHelper;

public record FrameGeometry(boolean emissive) implements UnbakedGeometry {
	@Override
	public QuadCollection bake(TextureSlots textures, ModelBaker baker, ModelState settings, ModelDebugName model) {
		MutableMesh builder = Renderer.get().mutableMesh();
		QuadEmitter emitter = builder.emitter();
		emitter.pushTransform(ModelStateHelper.asQuadTransform(settings, baker.materials()));

		Material.Baked material = baker.materials()
				.get(Objects.requireNonNull(textures.getMaterial("frame")), model);

		for (Direction direction : Direction.values()) {
			// Draw outer frame
			emitter.square(direction, 0.0F, 0.9F, 0.9F, 1.0F, 0.0F)
					.materialBake(material, MutableQuadView.BAKE_LOCK_UV)
					.emissive(emissive)
					.emit();

			emitter.square(direction, 0.0F, 0.0F, 0.1F, 0.9F, 0.0F)
					.materialBake(material, MutableQuadView.BAKE_LOCK_UV)
					.emissive(emissive)
					.emit();

			emitter.square(direction, 0.9F, 0.1F, 1.0F, 1.0F, 0.0F)
					.materialBake(material, MutableQuadView.BAKE_LOCK_UV)
					.emissive(emissive)
					.emit();

			emitter.square(direction, 0.1F, 0.0F, 1.0F, 0.1F, 0.0F)
					.materialBake(material, MutableQuadView.BAKE_LOCK_UV)
					.emissive(emissive)
					.emit();

			// Draw inner frame - inset by 0.9 so the frame looks like an actual mesh
			emitter.square(direction, 0.0F, 0.9F, 0.9F, 1.0F, 0.9F)
					.materialBake(material, MutableQuadView.BAKE_LOCK_UV)
					.emissive(emissive)
					.emit();

			emitter.square(direction, 0.0F, 0.0F, 0.1F, 0.9F, 0.9F)
					.materialBake(material, MutableQuadView.BAKE_LOCK_UV)
					.emissive(emissive)
					.emit();

			emitter.square(direction, 0.9F, 0.1F, 1.0F, 1.0F, 0.9F)
					.materialBake(material, MutableQuadView.BAKE_LOCK_UV)
					.emissive(emissive)
					.emit();

			emitter.square(direction, 0.1F, 0.0F, 1.0F, 0.1F, 0.9F)
					.materialBake(material, MutableQuadView.BAKE_LOCK_UV)
					.emissive(emissive)
					.emit();
		}

		return new MeshQuadCollection(builder.immutableCopy());
	}
}
