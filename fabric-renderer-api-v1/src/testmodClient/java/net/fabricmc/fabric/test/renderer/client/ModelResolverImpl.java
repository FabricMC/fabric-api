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

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.renderer.v1.material.ShadeMode;
import net.fabricmc.fabric.test.renderer.RendererTest;

public class ModelResolverImpl implements ModelModifier.OnLoad {
	private static final Identifier FRAME_MODEL_LOCATION = RendererTest.id("block/frame");
	private static final Identifier PILLAR_MODEL_LOCATION = RendererTest.id("block/pillar");
	private static final Identifier OCTAGONAL_COLUMN_MODEL_LOCATION = RendererTest.id("block/octagonal_column");
	private static final Identifier OCTAGONAL_COLUMN_VANILLA_MODEL_LOCATION = RendererTest.id("block/octagonal_column_vanilla");
	private static final Identifier RIVERSTONE_MODEL_LOCATION = RendererTest.id("block/riverstone");

	@Nullable
	public UnbakedModel modifyModelOnLoad(@Nullable UnbakedModel model, Context context) {
		Identifier id = context.id();

		if (FRAME_MODEL_LOCATION.equals(id)) {
			return new FrameUnbakedModel();
		}

		if (PILLAR_MODEL_LOCATION.equals(id)) {
			return new PillarUnbakedModel();
		}

		if (OCTAGONAL_COLUMN_MODEL_LOCATION.equals(id)) {
			return new OctagonalColumnUnbakedModel(ShadeMode.ENHANCED);
		}

		if (OCTAGONAL_COLUMN_VANILLA_MODEL_LOCATION.equals(id)) {
			return new OctagonalColumnUnbakedModel(ShadeMode.VANILLA);
		}

		if (RIVERSTONE_MODEL_LOCATION.equals(id)) {
			return new RiverstoneUnbakedModel();
		}

		return model;
	}
}
