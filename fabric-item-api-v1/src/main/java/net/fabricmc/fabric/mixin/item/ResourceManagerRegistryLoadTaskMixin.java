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

package net.fabricmc.fabric.mixin.item;

import java.util.Optional;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Either;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.RegistryLoadTask;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceManagerRegistryLoadTask;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.enchantment.Enchantment;

import net.fabricmc.fabric.impl.item.EnchantmentUtil;

@Mixin(ResourceManagerRegistryLoadTask.class)
public class ResourceManagerRegistryLoadTaskMixin {
	@WrapOperation(method = "lambda$load$2", at = @At(value = "NEW", target = "net/minecraft/resources/RegistryLoadTask$PendingRegistration"))
	private <T> RegistryLoadTask.PendingRegistration<?> modify(ResourceKey<T> key, Either<T, Exception> value, RegistrationInfo registrationInfo, Operation<RegistryLoadTask.PendingRegistration<T>> original, @Local(argsOnly = true) Resource resource) {
		if (value.left().isPresent()) {
			if (value.left().get() instanceof Enchantment enchantment) {
				Enchantment modified = EnchantmentUtil.modify((ResourceKey<Enchantment>) key, enchantment, EnchantmentUtil.determineSource(resource));

				if (modified != null) {
					// Clear the knownPackInfo to force the server to sync the data pack to the client
					registrationInfo = new RegistrationInfo(Optional.empty(), registrationInfo.lifecycle());
					value = Either.left((T) modified);
				}
			}
		}

		return original.call(key, value, registrationInfo);
	}
}
