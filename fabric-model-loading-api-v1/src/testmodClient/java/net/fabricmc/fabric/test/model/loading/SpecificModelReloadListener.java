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

package net.fabricmc.fabric.test.model.loading;

import java.util.Collection;
import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

public class SpecificModelReloadListener implements SimpleSynchronousResourceReloadListener {
	public static final SpecificModelReloadListener INSTANCE = new SpecificModelReloadListener();
	public static final Identifier ID = Identifier.of(ModelTestModClient.ID, "specific_model");

	private BlockStateModel specificModel;

	public BlockStateModel getSpecificModel() {
		return specificModel;
	}

	@Override
	public void reload(ResourceManager manager) {
		specificModel = MinecraftClient.getInstance().getBakedModelManager().getModel(ModelTestModClient.HALF_RED_SAND_MODEL_KEY);
	}

	@Override
	public Identifier getFabricId() {
		return ID;
	}

	@Override
	public Collection<Identifier> getFabricDependencies() {
		return List.of(ResourceReloadListenerKeys.MODELS);
	}
}
