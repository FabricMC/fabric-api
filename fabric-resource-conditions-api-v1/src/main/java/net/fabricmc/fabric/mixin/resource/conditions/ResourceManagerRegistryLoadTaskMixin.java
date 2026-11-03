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

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Decoder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.resources.RegistryLoadTask;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceManagerRegistryLoadTask;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;

@Mixin(ResourceManagerRegistryLoadTask.class)
class ResourceManagerRegistryLoadTaskMixin {
	@Shadow
	@Final
	private ResourceManager resourceManager;

	@WrapOperation(method = "lambda$load$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/RegistryLoadTask$PendingRegistration;loadFromResource(Lcom/mojang/serialization/Decoder;Lnet/minecraft/resources/RegistryOps;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/server/packs/resources/Resource;)Lcom/mojang/datafixers/util/Either;"))
	private <T> Either<T, Exception> loadFromResource(Decoder<T> decoder, RegistryOps<JsonElement> ops, ResourceKey<T> elementKey, Resource resource, Operation<Either<T, Exception>> original) {
		return ScopedValue.where(ResourceConditionsImpl.CURRENT_RESOURCE_MANAGER, resourceManager)
				.call(() -> original.call(decoder, ops, elementKey, resource));
	}

	@ModifyExpressionValue(method = "lambda$load$2", at = @At(value = "NEW", target = "net/minecraft/resources/RegistryLoadTask$PendingRegistration"))
	private RegistryLoadTask.PendingRegistration<?> load(RegistryLoadTask.PendingRegistration<?> original) {
		if (original.value().right().isPresent() && original.value().right().get() == ResourceConditionsImpl.DISABLED_RESOURCE_EXCEPTION) {
			return null;
		}

		return original;
	}
}
