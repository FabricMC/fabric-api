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

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Decoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.resource.Resource;

import net.fabricmc.fabric.impl.item.EnchantmentUtil;

@Mixin(RegistryLoader.class)
abstract class RegistryLoaderMixin {
	@WrapOperation(
			method = "parseAndAdd",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/registry/MutableRegistry;add(Lnet/minecraft/registry/RegistryKey;Ljava/lang/Object;Lnet/minecraft/registry/entry/RegistryEntryInfo;)Lnet/minecraft/registry/entry/RegistryEntry$Reference;"
			)
	)
	@SuppressWarnings("unchecked")
	private static <T> RegistryEntry.Reference<T> enchantmentKey(
			MutableRegistry<T> instance,
			RegistryKey<T> objectKey,
			Object object,
			RegistryEntryInfo registryEntryInfo,
			Operation<RegistryEntry.Reference<T>> original,
			MutableRegistry<T> registry,
			Decoder<T> decoder,
			RegistryOps<JsonElement> ops,
			RegistryKey<T> registryKey,
			Resource resource,
			RegistryEntryInfo entryInfo
	) {
		if (object instanceof Enchantment enchantment) {
			object = EnchantmentUtil.modify((RegistryKey<Enchantment>) objectKey, enchantment, EnchantmentUtil.determineSource(resource));
		}

		return original.call(instance, registryKey, object, registryEntryInfo);
	}
}
