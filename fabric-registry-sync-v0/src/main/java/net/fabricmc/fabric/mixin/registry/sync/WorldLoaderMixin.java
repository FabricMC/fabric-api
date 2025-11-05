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

package net.fabricmc.fabric.mixin.registry.sync;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.WorldLoader;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

// Implements dynamic registry loading.
@Mixin(WorldLoader.class)
abstract class WorldLoaderMixin {
	@ModifyArg(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/RegistryDataLoader;load(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Ljava/util/List;)Lnet/minecraft/core/RegistryAccess$Frozen;", ordinal = 0), index = 2, allow = 1)
	private static List<RegistryDataLoader.RegistryData<?>> modifyLoadedEntries(List<RegistryDataLoader.RegistryData<?>> entries) {
		return DynamicRegistries.getDynamicRegistries();
	}
}
