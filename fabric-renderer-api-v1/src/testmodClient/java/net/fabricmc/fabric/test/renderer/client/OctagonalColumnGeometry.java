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
import net.minecraft.client.renderer.item.ItemStackRenderState;
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
import net.fabricmc.fabric.api.client.renderer.v1.mesh.ShadeMode;
import net.fabricmc.fabric.api.client.renderer.v1.model.MeshQuadCollection;
import net.fabricmc.fabric.api.client.renderer.v1.model.ModelStateHelper;

public record OctagonalColumnGeometry(ShadeMode shadeMode) implements UnbakedGeometry {
	// (B - A) is the side length of a regular octagon that fits in a unit square.
	// The line from A to B is centered on the line from 0 to 1.
	private static final float A = (float) (1 - Math.sqrt(2) / 2);
	private static final float B = (float) (Math.sqrt(2) / 2);

	@Override
	public QuadCollection bake(TextureSlots textures, ModelBaker baker, ModelState settings, ModelDebugName model) {
		MutableMesh builder = Renderer.get().mutableMesh();
		QuadEmitter emitter = builder.emitter();
		emitter.pushTransform(ModelStateHelper.asQuadTransform(settings, baker.materials()));

		Material.Baked material = baker.materials()
				.get(Objects.requireNonNull(textures.getMaterial("column")), model);

		// up

		emitter.pos(0, A, 1, 0)
				.pos(1, 0.5f, 1, 0.5f)
				.pos(2, 1, 1, A)
				.pos(3, B, 1, 0)
				.cullFace(Direction.UP)
				.materialBake(material, MutableQuadView.BAKE_LOCK_UV)
				.shadeMode(shadeMode)
				.emit();

		emitter.pos(0, 0, 1, A)
				.pos(1, 0, 1, B)
				.pos(2, 0.5f, 1, 0.5f)
				.pos(3, A, 1, 0)
				.cullFace(Direction.UP)
				.materialBake(material, MutableQuadView.BAKE_LOCK_UV)
				.shadeMode(shadeMode)
				.emit();

		emitter.pos(0, 0, 1, B)
				.pos(1, A, 1, 1)
				.pos(2, B, 1, 1)
				.pos(3, 0.5f, 1, 0.5f)
				.cullFace(Direction.UP)
				.materialBake(material, MutableQuadView.BAKE_LOCK_UV)
				.shadeMode(shadeMode)
				.emit();

		emitter.pos(0, 0.5f, 1, 0.5f)
				.pos(1, B, 1, 1)
				.pos(2, 1, 1, B)
				.pos(3, 1, 1, A)
				.cullFace(Direction.UP)
				.materialBake(material, MutableQuadView.BAKE_LOCK_UV)
				.shadeMode(shadeMode)
				.emit();

		// down

		emitter.pos(0, A, 0, 1)
				.pos(1, 0.5f, 0, 0.5f)
				.pos(2, 1, 0, B)
				.pos(3, B, 0, 1)
				.cullFace(Direction.DOWN)
				.materialBake(material, MutableQuadView.BAKE_LOCK_UV)
				.shadeMode(shadeMode)
				.emit();

		emitter.pos(0, 0, 0, B)
				.pos(1, 0, 0, A)
				.pos(2, 0.5f, 0, 0.5f)
				.pos(3, A, 0, 1)
				.cullFace(Direction.DOWN)
				.materialBake(material, MutableQuadView.BAKE_LOCK_UV)
				.shadeMode(shadeMode)
				.emit();

		emitter.pos(0, 0, 0, A)
				.pos(1, A, 0, 0)
				.pos(2, B, 0, 0)
				.pos(3, 0.5f, 0, 0.5f)
				.cullFace(Direction.DOWN)
				.materialBake(material, MutableQuadView.BAKE_LOCK_UV)
				.shadeMode(shadeMode)
				.emit();

		emitter.pos(0, 0.5f, 0, 0.5f)
				.pos(1, B, 0, 0)
				.pos(2, 1, 0, A)
				.pos(3, 1, 0, B)
				.cullFace(Direction.DOWN)
				.materialBake(material, MutableQuadView.BAKE_LOCK_UV)
				.shadeMode(shadeMode)
				.emit();

		// north
		emitter.pos(0, B, 1, 0)
				.pos(1, B, 0, 0)
				.pos(2, A, 0, 0)
				.pos(3, A, 1, 0)
				.cullFace(Direction.NORTH)
				.materialBake(material, MutableQuadView.BAKE_LOCK_UV)
				.shadeMode(shadeMode)
				.foilType(ItemStackRenderState.FoilType.STANDARD)
				.emit();

		// northwest
		emitter.pos(0, A, 1, 0)
				.pos(1, A, 0, 0)
				.pos(2, 0, 0, A)
				.pos(3, 0, 1, A);
		cornerSprite(emitter, material);
		emitter.shadeMode(shadeMode)
				.foilType(ItemStackRenderState.FoilType.STANDARD)
				.emit();

		// west
		emitter.pos(0, 0, 1, A)
				.pos(1, 0, 0, A)
				.pos(2, 0, 0, B)
				.pos(3, 0, 1, B)
				.cullFace(Direction.WEST)
				.materialBake(material, MutableQuadView.BAKE_LOCK_UV)
				.shadeMode(shadeMode)
				.foilType(ItemStackRenderState.FoilType.STANDARD)
				.emit();

		// southwest
		emitter.pos(0, 0, 1, B)
				.pos(1, 0, 0, B)
				.pos(2, A, 0, 1)
				.pos(3, A, 1, 1);
		cornerSprite(emitter, material);
		emitter.shadeMode(shadeMode)
				.foilType(ItemStackRenderState.FoilType.STANDARD)
				.emit();

		// south
		emitter.pos(0, A, 1, 1)
				.pos(1, A, 0, 1)
				.pos(2, B, 0, 1)
				.pos(3, B, 1, 1)
				.cullFace(Direction.SOUTH)
				.materialBake(material, MutableQuadView.BAKE_LOCK_UV)
				.shadeMode(shadeMode)
				.foilType(ItemStackRenderState.FoilType.STANDARD)
				.emit();

		// southeast
		emitter.pos(0, B, 1, 1)
				.pos(1, B, 0, 1)
				.pos(2, 1, 0, B)
				.pos(3, 1, 1, B);
		cornerSprite(emitter, material);
		emitter.shadeMode(shadeMode)
				.foilType(ItemStackRenderState.FoilType.STANDARD)
				.emit();

		// east
		emitter.pos(0, 1, 1, B)
				.pos(1, 1, 0, B)
				.pos(2, 1, 0, A)
				.pos(3, 1, 1, A)
				.cullFace(Direction.EAST)
				.materialBake(material, MutableQuadView.BAKE_LOCK_UV)
				.shadeMode(shadeMode)
				.foilType(ItemStackRenderState.FoilType.STANDARD)
				.emit();

		// northeast
		emitter.pos(0, 1, 1, A)
				.pos(1, 1, 0, A)
				.pos(2, B, 0, 0)
				.pos(3, B, 1, 0);
		cornerSprite(emitter, material);
		emitter.shadeMode(shadeMode)
				.foilType(ItemStackRenderState.FoilType.STANDARD)
				.emit();

		return new MeshQuadCollection(builder.immutableCopy());
	}

	private static void cornerSprite(QuadEmitter emitter, Material.Baked material) {
		// Assign uvs for a corner face in such a way that the texture is not stretched, using coordinates in [0, 1].
		emitter.uv(0, A, 0)
				.uv(1, A, 1)
				.uv(2, B, 1)
				.uv(3, B, 0);
		// Map [0, 1] coordinates to sprite atlas coordinates. spriteBake assumes [0, 16] unless we pass the BAKE_NORMALIZED flag.
		emitter.materialBake(material, MutableQuadView.BAKE_NORMALIZED);
	}
}
