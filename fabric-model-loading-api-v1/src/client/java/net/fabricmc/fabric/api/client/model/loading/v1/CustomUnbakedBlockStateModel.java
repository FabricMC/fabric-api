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

package net.fabricmc.fabric.api.client.model.loading.v1;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.client.model.loading.CustomUnbakedBlockStateModelRegistry;

public interface CustomUnbakedBlockStateModel extends BlockStateModel.Unbaked {
	static void register(Identifier id, MapCodec<? extends CustomUnbakedBlockStateModel> codec) {
		CustomUnbakedBlockStateModelRegistry.register(id, codec);
	}

	MapCodec<? extends CustomUnbakedBlockStateModel> codec();
}
