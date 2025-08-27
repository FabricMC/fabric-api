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

import com.google.gson.GsonBuilder;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;

import net.fabricmc.fabric.impl.client.model.loading.UnbakedModelJsonDeserializer;

@Mixin(JsonUnbakedModel.class)
abstract class JsonUnbakedModelMixin {
	@ModifyExpressionValue(method = "<clinit>()V", at = @At(value = "NEW", target = "com/google/gson/GsonBuilder"))
	private static GsonBuilder addUnbakedModelAdapter(GsonBuilder builder) {
		return builder.registerTypeHierarchyAdapter(UnbakedModel.class, new UnbakedModelJsonDeserializer());
	}
}
