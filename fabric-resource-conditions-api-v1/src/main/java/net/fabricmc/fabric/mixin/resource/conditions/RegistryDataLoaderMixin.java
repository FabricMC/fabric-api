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

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Decoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;

@Mixin(RegistryDataLoader.class)
public class RegistryDataLoaderMixin {
	@Unique
	private static final ThreadLocal<RegistryOps.RegistryInfoLookup> REGISTRY_INFO = new ThreadLocal<>();

	/**
	 * Capture the current registries, so they can be passed to the resource conditions.
	 */
	@Inject(method = "loadContentsFromManager(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/RegistryOps$RegistryInfoLookup;Lnet/minecraft/core/WritableRegistry;Lcom/mojang/serialization/Decoder;Ljava/util/Map;)V", at = @At("HEAD"))
	private static <E> void captureRegistries(ResourceManager resourceManager, RegistryOps.RegistryInfoLookup infoGetter, WritableRegistry<E> registry, Decoder<E> elementDecoder, Map<ResourceKey<?>, Exception> errors, CallbackInfo ci) {
		REGISTRY_INFO.set(infoGetter);
	}

	@Inject(method = "loadContentsFromManager(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/RegistryOps$RegistryInfoLookup;Lnet/minecraft/core/WritableRegistry;Lcom/mojang/serialization/Decoder;Ljava/util/Map;)V", at = @At("RETURN"))
	private static <E> void releaseRegistries(ResourceManager resourceManager, RegistryOps.RegistryInfoLookup infoGetter, WritableRegistry<E> registry, Decoder<E> elementDecoder, Map<ResourceKey<?>, Exception> errors, CallbackInfo ci) {
		REGISTRY_INFO.remove();
	}

	@Inject(
			method = "loadElementFromResource(Lnet/minecraft/core/WritableRegistry;Lcom/mojang/serialization/Decoder;Lnet/minecraft/resources/RegistryOps;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/server/packs/resources/Resource;Lnet/minecraft/core/RegistrationInfo;)V",
			at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Decoder;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"),
			cancellable = true
	)
	private static <E> void checkResourceCondition(
			WritableRegistry<E> registry, Decoder<E> decoder, RegistryOps<JsonElement> ops, ResourceKey<E> key, Resource resource, RegistrationInfo entryInfo,
			CallbackInfo ci, @Local Reader reader, @Local JsonElement jsonElement
	) throws IOException {
		// This method is called both on the server (when loading resources) and on the client (when syncing from the
		// server). We only want to apply resource conditions when loading via loadFromResource.
		RegistryOps.RegistryInfoLookup registryInfoGetter = REGISTRY_INFO.get();
		if (registryInfoGetter == null) return;

		if (jsonElement.isJsonObject() && !ResourceConditionsImpl.applyResourceConditions(jsonElement.getAsJsonObject(), key.registry().toString(), key.identifier(), registryInfoGetter)) {
			reader.close();
			ci.cancel();
		}
	}
}
