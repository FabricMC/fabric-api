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

package net.fabricmc.fabric.mixin.resource.conditions;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.registry.RegistryOps;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;

@Mixin(JsonDataLoader.class)
public class JsonDataLoaderMixin {
	@Unique
	private static final Object SKIP_DATA_MARKER = new Object();

	@WrapOperation(method = "load(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/resource/ResourceFinder;Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;Ljava/util/Map;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;", remap = false))
	private static DataResult<?> applyResourceConditions(Codec<?> instance, DynamicOps<JsonElement> dynamicOps, Object object, Operation<DataResult<?>> original,
														@Local(argsOnly = true) ResourceFinder resourceFinder,
														@Local Map.Entry<Identifier, Resource> entry) {
		final JsonElement resourceData = (JsonElement) object;
		@Nullable RegistryOps.RegistryInfoGetter registryInfo = null;

		if (dynamicOps instanceof RegistryOpsAccessor registryOps) {
			registryInfo = registryOps.getRegistryInfoGetter();
		}

		if (resourceData.isJsonObject()) {
			JsonObject obj = resourceData.getAsJsonObject();

			final String dataType = ((ResourceFinderAccessor) resourceFinder).getDirectoryName();

			if (!ResourceConditionsImpl.applyResourceConditions(obj, dataType, entry.getKey(), registryInfo)) {
				return DataResult.success(SKIP_DATA_MARKER);
			}
		}

		return original.call(instance, dynamicOps, object);
	}

	// parse.ifSuccess
	@Inject(method = "method_63568", at = @At("HEAD"), cancellable = true)
	private static void skipData(Map<?, ?> map, Identifier identifier, Object object, CallbackInfo ci) {
		if (object == SKIP_DATA_MARKER) {
			ci.cancel();
		}
	}
}
