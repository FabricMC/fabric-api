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

import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.model.SimpleBlockStateModel;
import net.minecraft.client.render.model.json.ModelVariant;
import net.minecraft.util.Identifier;

public class RiverstoneUnbakedBlockStateModel implements BlockStateModel.Unbaked {
	private static final Identifier STONE_MODEL_ID = Identifier.ofVanilla("block/stone");
	private static final Identifier GOLD_BLOCK_MODEL_ID = Identifier.ofVanilla("block/gold_block");

	@Override
	public void resolve(Resolver resolver) {
		resolver.markDependency(STONE_MODEL_ID);
		resolver.markDependency(GOLD_BLOCK_MODEL_ID);
	}

	@Override
	public BlockStateModel bake(Baker baker) {
		BlockStateModel stoneModel = new SimpleBlockStateModel.Unbaked(new ModelVariant(STONE_MODEL_ID)).bake(baker);
		BlockStateModel goldBlockModel = new SimpleBlockStateModel.Unbaked(new ModelVariant(GOLD_BLOCK_MODEL_ID)).bake(baker);
		return new RiverstoneBlockStateModel(stoneModel, goldBlockModel);
	}
}
