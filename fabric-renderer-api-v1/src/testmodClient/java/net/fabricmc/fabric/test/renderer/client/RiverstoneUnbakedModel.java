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
import net.minecraft.util.Identifier;

public class RiverstoneUnbakedModel implements UnbakedModel {
	private static final Identifier STONE_MODEL_ID = Identifier.ofVanilla("block/stone");
	private static final Identifier GOLD_BLOCK_MODEL_ID = Identifier.ofVanilla("block/gold_block");

	@Override
	public void resolve(Resolver resolver) {
		resolver.resolve(STONE_MODEL_ID);
		resolver.resolve(GOLD_BLOCK_MODEL_ID);
	}

	@Override
	public BakedModel bake(ModelTextures textures, Baker baker, ModelBakeSettings settings, boolean ambientOcclusion, boolean isSideLit, ModelTransformation transformation) {
		BakedModel stoneModel = baker.bake(STONE_MODEL_ID, settings);
		BakedModel goldBlockModel = baker.bake(GOLD_BLOCK_MODEL_ID, settings);
		return new RiverstoneBakedModel(stoneModel, goldBlockModel);
	}
}
