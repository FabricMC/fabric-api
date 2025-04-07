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

package net.fabricmc.fabric.mixin.client.model.loading;

import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.render.model.ModelBaker;

import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.impl.client.model.loading.BakedModelsHooks;

@Mixin(ModelBaker.BakedModels.class)
abstract class ModelBakerBakedModelsMixin implements BakedModelsHooks {
	@Unique
	@Nullable
	private Map<ExtraModelKey<?>, ?> extraModels;

	@Override
	@Nullable
	public Map<ExtraModelKey<?>, ?> fabric_getExtraModels() {
		return extraModels;
	}

	@Override
	public void fabric_setExtraModels(@Nullable Map<ExtraModelKey<?>, ?> extraModels) {
		this.extraModels = extraModels;
	}
}
