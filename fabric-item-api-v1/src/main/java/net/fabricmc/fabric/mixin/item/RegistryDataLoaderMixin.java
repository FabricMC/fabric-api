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

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Decoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.enchantment.Enchantment;

import net.fabricmc.fabric.impl.item.EnchantmentUtil;

@Mixin(RegistryDataLoader.class)
abstract class RegistryDataLoaderMixin {
	@WrapOperation(
			method = "loadElementFromResource",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/core/WritableRegistry;register(Lnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lnet/minecraft/core/RegistrationInfo;)Lnet/minecraft/core/Holder$Reference;"
			)
	)
	@SuppressWarnings("unchecked")
	private static <T> Holder.Reference<T> enchantmentKey(
			WritableRegistry<T> instance,
			ResourceKey<T> objectKey,
			Object object,
			RegistrationInfo registryEntryInfo,
			Operation<Holder.Reference<T>> original,
			WritableRegistry<T> registry,
			Decoder<T> decoder,
			RegistryOps<JsonElement> ops,
			ResourceKey<T> registryKey,
			Resource resource,
			RegistrationInfo entryInfo
	) {
		if (object instanceof Enchantment enchantment) {
			Enchantment modified = EnchantmentUtil.modify((ResourceKey<Enchantment>) objectKey, enchantment, EnchantmentUtil.determineSource(resource));

			if (modified != null) {
				object = modified;

				// Clear the knownPackInfo to force the server to sync the data pack to the client
				registryEntryInfo = new RegistrationInfo(Optional.empty(), registryEntryInfo.lifecycle());
			}
		}

		return original.call(instance, registryKey, object, registryEntryInfo);
	}
}
