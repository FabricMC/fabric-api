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
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Decoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resources.RegistryLoadTask;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;

import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;

@Mixin(RegistryLoadTask.PendingRegistration.class)
public abstract class RegistryLoadTaskPendingRegistrationMixin {
	@Inject(method = "loadFromResource", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Decoder;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"), cancellable = true)
	private static <T> void loadFromResource(Decoder<T> elementDecoder, RegistryOps<JsonElement> ops, ResourceKey<T> elementKey, Resource thunk, CallbackInfoReturnable<Either<T, Exception>> cir, @Local(name = "json") JsonElement json) {
		if (json.isJsonObject() && !ResourceConditionsImpl.applyResourceConditions(json.getAsJsonObject(), elementKey.registry().toString(), elementKey.identifier(), ops.lookupProvider)) {
			cir.setReturnValue(Either.right(ResourceConditionsImpl.DISABLED_RESOURCE_EXCEPTION));
		}
	}
}
