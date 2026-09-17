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

package net.fabricmc.fabric.mixin.advancement;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Decoder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceManagerRegistryLoadTask;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import net.fabricmc.fabric.impl.advancement.AdvancementHolderProvider;
import net.fabricmc.fabric.impl.advancement.AdvancementUtil;

@Mixin(ResourceManagerRegistryLoadTask.class)
abstract class ResourceManagerRegistryLoadTaskMixin {
	@Shadow
	@Final
	private ResourceManager resourceManager;

	@SuppressWarnings("unchecked")
	@WrapOperation(method = "lambda$load$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/RegistryLoadTask$PendingRegistration;loadFromResource(Lcom/mojang/serialization/Decoder;Lnet/minecraft/resources/RegistryOps;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/server/packs/resources/Resource;)Lcom/mojang/datafixers/util/Either;"))
	private <T> Either<T, Exception> modifyAdvancement(Decoder<T> decoder, RegistryOps<JsonElement> ops, ResourceKey<T> key, Resource resource, Operation<Either<T, Exception>> original) {
		Either<T, Exception> result = original.call(decoder, ops, key, resource);

		if (!key.isFor(Registries.ADVANCEMENT)) {
			return result;
		}

		HolderLookup.Provider provider = AdvancementUtil.getActiveReloadProvider(this.resourceManager);

		RegistryOps.RegistryInfoLookup registryInfoLookup = ops::getter;

		if (provider == null) {
			return result;
		}

		// Merge the HolderLookup.Provider and RegistryOps.RegistryInfoLookup into a HolderLookup.Provider with access to reloadable registries.
		var fullProvider = new AdvancementHolderProvider(registryInfoLookup, provider);

		return result.mapLeft(value -> (T) AdvancementUtil.modifyAdvancement(
				key.identifier(),
				(Advancement) value,
				AdvancementUtil.determineSource(resource),
				fullProvider
		));
	}
}
