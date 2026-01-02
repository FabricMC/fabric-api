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

package net.fabricmc.fabric.mixin.client.rendering;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.ArmorModelSet;

import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.impl.client.rendering.ModelLayerImpl;

@Mixin(LayerDefinitions.class)
abstract class LayerDefinitionsMixin {
	@Inject(method = "createRoots", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;"))
	private static void registerExtraModelData(CallbackInfoReturnable<Map<ModelLayerLocation, LayerDefinition>> info, @Local(name = "result") ImmutableMap.Builder<ModelLayerLocation, LayerDefinition> result) {
		for (Map.Entry<ModelLayerLocation, ModelLayerRegistry.TexturedLayerDefinitionProvider> entry : ModelLayerImpl.PROVIDERS.entrySet()) {
			result.put(entry.getKey(), entry.getValue().createLayerDefinition());
		}

		for (Map.Entry<ArmorModelSet<ModelLayerLocation>, ModelLayerRegistry.TexturedArmorModelSetProvider> entry : ModelLayerImpl.ARMOR_PROVIDERS.entrySet()) {
			entry.getKey().putFrom(entry.getValue().createArmorModelSet(), result);
		}
	}
}
