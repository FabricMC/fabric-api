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

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.material.GlintMode;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.material.ShadeMode;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

public class OctagonalColumnUnbakedModel implements UnbakedModel {
	private static final SpriteIdentifier WHITE_CONCRETE_SPRITE_ID = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.ofVanilla("block/white_concrete"));

	// (B - A) is the side length of a regular octagon that fits in a unit square.
	// The line from A to B is centered on the line from 0 to 1.
	private static final float A = (float) (1 - Math.sqrt(2) / 2);
	private static final float B = (float) (Math.sqrt(2) / 2);

	private final ShadeMode shadeMode;

	public OctagonalColumnUnbakedModel(ShadeMode shadeMode) {
		this.shadeMode = shadeMode;
	}

	@Override
	public void resolve(Resolver resolver) {
	}

	@Override
	public BakedModel bake(ModelTextures textures, Baker baker, ModelBakeSettings settings, boolean ambientOcclusion, boolean isSideLit, ModelTransformation transformation) {
		Sprite whiteConcreteSprite = baker.getSpriteGetter().get(WHITE_CONCRETE_SPRITE_ID);

		MaterialFinder finder = Renderer.get().materialFinder();
		RenderMaterial glintMaterial = finder.glintMode(GlintMode.STANDARD).shadeMode(shadeMode).find();

		MutableMesh builder = Renderer.get().mutableMesh();
		QuadEmitter emitter = builder.emitter();

		// up

		emitter.pos(0, A, 1, 0);
		emitter.pos(1, 0.5f, 1, 0.5f);
		emitter.pos(2, 1, 1, A);
		emitter.pos(3, B, 1, 0);
		emitter.cullFace(Direction.UP);
		emitter.spriteBake(whiteConcreteSprite, MutableQuadView.BAKE_LOCK_UV);
		emitter.emit();

		emitter.pos(0, 0, 1, A);
		emitter.pos(1, 0, 1, B);
		emitter.pos(2, 0.5f, 1, 0.5f);
		emitter.pos(3, A, 1, 0);
		emitter.cullFace(Direction.UP);
		emitter.spriteBake(whiteConcreteSprite, MutableQuadView.BAKE_LOCK_UV);
		emitter.emit();

		emitter.pos(0, 0, 1, B);
		emitter.pos(1, A, 1, 1);
		emitter.pos(2, B, 1, 1);
		emitter.pos(3, 0.5f, 1, 0.5f);
		emitter.cullFace(Direction.UP);
		emitter.spriteBake(whiteConcreteSprite, MutableQuadView.BAKE_LOCK_UV);
		emitter.emit();

		emitter.pos(0, 0.5f, 1, 0.5f);
		emitter.pos(1, B, 1, 1);
		emitter.pos(2, 1, 1, B);
		emitter.pos(3, 1, 1, A);
		emitter.cullFace(Direction.UP);
		emitter.spriteBake(whiteConcreteSprite, MutableQuadView.BAKE_LOCK_UV);
		emitter.emit();

		// down

		emitter.pos(0, A, 0, 1);
		emitter.pos(1, 0.5f, 0, 0.5f);
		emitter.pos(2, 1, 0, B);
		emitter.pos(3, B, 0, 1);
		emitter.cullFace(Direction.DOWN);
		emitter.spriteBake(whiteConcreteSprite, MutableQuadView.BAKE_LOCK_UV);
		emitter.emit();

		emitter.pos(0, 0, 0, B);
		emitter.pos(1, 0, 0, A);
		emitter.pos(2, 0.5f, 0, 0.5f);
		emitter.pos(3, A, 0, 1);
		emitter.cullFace(Direction.DOWN);
		emitter.spriteBake(whiteConcreteSprite, MutableQuadView.BAKE_LOCK_UV);
		emitter.emit();

		emitter.pos(0, 0, 0, A);
		emitter.pos(1, A, 0, 0);
		emitter.pos(2, B, 0, 0);
		emitter.pos(3, 0.5f, 0, 0.5f);
		emitter.cullFace(Direction.DOWN);
		emitter.spriteBake(whiteConcreteSprite, MutableQuadView.BAKE_LOCK_UV);
		emitter.emit();

		emitter.pos(0, 0.5f, 0, 0.5f);
		emitter.pos(1, B, 0, 0);
		emitter.pos(2, 1, 0, A);
		emitter.pos(3, 1, 0, B);
		emitter.cullFace(Direction.DOWN);
		emitter.spriteBake(whiteConcreteSprite, MutableQuadView.BAKE_LOCK_UV);
		emitter.emit();

		// north
		emitter.pos(0, B, 1, 0);
		emitter.pos(1, B, 0, 0);
		emitter.pos(2, A, 0, 0);
		emitter.pos(3, A, 1, 0);
		emitter.cullFace(Direction.NORTH);
		emitter.spriteBake(whiteConcreteSprite, MutableQuadView.BAKE_LOCK_UV);
		emitter.material(glintMaterial);
		emitter.emit();

		// northwest
		emitter.pos(0, A, 1, 0);
		emitter.pos(1, A, 0, 0);
		emitter.pos(2, 0, 0, A);
		emitter.pos(3, 0, 1, A);
		cornerSprite(emitter, whiteConcreteSprite);
		emitter.material(glintMaterial);
		emitter.emit();

		// west
		emitter.pos(0, 0, 1, A);
		emitter.pos(1, 0, 0, A);
		emitter.pos(2, 0, 0, B);
		emitter.pos(3, 0, 1, B);
		emitter.cullFace(Direction.WEST);
		emitter.spriteBake(whiteConcreteSprite, MutableQuadView.BAKE_LOCK_UV);
		emitter.material(glintMaterial);
		emitter.emit();

		// southwest
		emitter.pos(0, 0, 1, B);
		emitter.pos(1, 0, 0, B);
		emitter.pos(2, A, 0, 1);
		emitter.pos(3, A, 1, 1);
		cornerSprite(emitter, whiteConcreteSprite);
		emitter.material(glintMaterial);
		emitter.emit();

		// south
		emitter.pos(0, A, 1, 1);
		emitter.pos(1, A, 0, 1);
		emitter.pos(2, B, 0, 1);
		emitter.pos(3, B, 1, 1);
		emitter.cullFace(Direction.SOUTH);
		emitter.spriteBake(whiteConcreteSprite, MutableQuadView.BAKE_LOCK_UV);
		emitter.material(glintMaterial);
		emitter.emit();

		// southeast
		emitter.pos(0, B, 1, 1);
		emitter.pos(1, B, 0, 1);
		emitter.pos(2, 1, 0, B);
		emitter.pos(3, 1, 1, B);
		cornerSprite(emitter, whiteConcreteSprite);
		emitter.material(glintMaterial);
		emitter.emit();

		// east
		emitter.pos(0, 1, 1, B);
		emitter.pos(1, 1, 0, B);
		emitter.pos(2, 1, 0, A);
		emitter.pos(3, 1, 1, A);
		emitter.cullFace(Direction.EAST);
		emitter.spriteBake(whiteConcreteSprite, MutableQuadView.BAKE_LOCK_UV);
		emitter.material(glintMaterial);
		emitter.emit();

		// northeast
		emitter.pos(0, 1, 1, A);
		emitter.pos(1, 1, 0, A);
		emitter.pos(2, B, 0, 0);
		emitter.pos(3, B, 1, 0);
		cornerSprite(emitter, whiteConcreteSprite);
		emitter.material(glintMaterial);
		emitter.emit();

		return new SingleMeshBakedModel(builder.immutableCopy(), whiteConcreteSprite);
	}

	private static void cornerSprite(QuadEmitter emitter, Sprite sprite) {
		// Assign uvs for a corner face in such a way that the texture is not stretched, using coordinates in [0, 1].
		emitter.uv(0, A, 0);
		emitter.uv(1, A, 1);
		emitter.uv(2, B, 1);
		emitter.uv(3, B, 0);
		// Map [0, 1] coordinates to sprite atlas coordinates. spriteBake assumes [0, 16] unless we pass the BAKE_NORMALIZED flag.
		emitter.spriteBake(sprite, MutableQuadView.BAKE_NORMALIZED);
	}
}
