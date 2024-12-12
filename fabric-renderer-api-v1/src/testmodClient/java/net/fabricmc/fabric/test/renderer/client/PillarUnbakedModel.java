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

import java.util.List;
import java.util.stream.Stream;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;

import net.fabricmc.fabric.test.renderer.RendererTest;

public class PillarUnbakedModel implements UnbakedModel {
	private static final List<SpriteIdentifier> SPRITES = Stream.of("alone", "bottom", "middle", "top")
			.map(suffix -> new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, RendererTest.id("block/pillar_" + suffix)))
			.toList();

	@Override
	public void resolve(Resolver resolver) {
	}

	@Override
	public BakedModel bake(ModelTextures textures, Baker baker, ModelBakeSettings settings, boolean ambientOcclusion, boolean isSideLit, ModelTransformation transformation) {
		Sprite[] sprites = new Sprite[SPRITES.size()];

		for (int i = 0; i < sprites.length; ++i) {
			sprites[i] = baker.getSpriteGetter().get(SPRITES.get(i));
		}

		return new PillarBakedModel(sprites);
	}
}
