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

package net.fabricmc.fabric.mixin.loot;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.loot.LootTable;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.impl.loot.FabricLootTable;
import net.fabricmc.fabric.impl.loot.LootUtil;

/**
 * Implements the events from {@link LootTableEvents}.
 */
@Mixin(ReloadableServerRegistries.class)
abstract class ReloadableServerRegistriesMixin {
	@WrapOperation(method = "reload", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/RegistryDataLoader;load(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Ljava/util/List;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
	private static CompletableFuture<RegistryAccess.Frozen> modifyLootTables(ResourceManager resourceManager, List<HolderLookup.RegistryLookup<?>> contextRegistries, List<RegistryDataLoader.RegistryData<?>> registriesToLoad, Executor executor, Operation<CompletableFuture<RegistryAccess.Frozen>> original, @Local(name = "loadingContextWithTags") HolderLookup.Provider provider) {
		LootUtil.startReload(resourceManager, provider);

		return original.call(resourceManager, contextRegistries, registriesToLoad, executor)
				.thenApply(registries -> {
					Registry<LootTable> lootTableRegistry = registries.lookupOrThrow(Registries.LOOT_TABLE);
					LootTableEvents.ALL_LOADED.invoker().onLootTablesLoaded(resourceManager, lootTableRegistry);
					lootTableRegistry.listElements().forEach(reference -> ((FabricLootTable) reference.value()).fabric$setHolder(reference));
					return registries;
				})
				.whenComplete((registries, throwable) -> LootUtil.endReload(resourceManager));
	}
}
