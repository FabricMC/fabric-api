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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class SpecificModelReloadListener implements ResourceManagerReloadListener {
	public static final SpecificModelReloadListener INSTANCE = new SpecificModelReloadListener();
	public static final Identifier ID = Identifier.fromNamespaceAndPath(ModelTestModClient.ID, "specific_model");

	private BlockStateModel specificModel;

	public BlockStateModel getSpecificModel() {
		return specificModel;
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		specificModel = Minecraft.getInstance().getModelManager().getModel(ModelTestModClient.HALF_RED_SAND_MODEL_KEY);
	}
}
