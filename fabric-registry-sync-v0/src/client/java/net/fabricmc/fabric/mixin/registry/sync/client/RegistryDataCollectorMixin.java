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

package net.fabricmc.fabric.mixin.registry.sync.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

import net.minecraft.client.multiplayer.RegistryDataCollector;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceProvider;

import net.fabricmc.fabric.impl.registry.sync.DynamicRegistriesImpl;

@Mixin(RegistryDataCollector.class)
public class RegistryDataCollectorMixin {
	/**
	 * Keep the pre-24w04a behavior of removing empty registries, even if the client knows that registry.
	 */
	@WrapOperation(method = "loadNewElementsAndTags", at = @At(value = "FIELD", target = "Lnet/minecraft/resources/RegistryDataLoader;SYNCHRONIZED_REGISTRIES:Ljava/util/List;", opcode = Opcodes.GETSTATIC))
	private List<RegistryDataLoader.RegistryData<?>> skipEmptyRegistries(Operation<List<RegistryDataLoader.RegistryData<?>>> operation, ResourceProvider resourceFactory, @Coerce ClientRegistriesDynamicRegistriesAccessor storage, boolean bl) {
		Map<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> dynamicRegistries = storage.getDynamicRegistries();

		List<RegistryDataLoader.RegistryData<?>> result = new ArrayList<>(operation.call());
		result.removeIf(entry -> DynamicRegistriesImpl.SKIP_EMPTY_SYNC_REGISTRIES.contains(entry.key()) && !dynamicRegistries.containsKey(entry.key()));
		return result;
	}
}
