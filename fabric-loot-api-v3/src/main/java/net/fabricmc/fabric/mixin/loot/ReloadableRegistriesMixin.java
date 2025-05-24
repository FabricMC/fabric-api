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
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.DynamicOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.ReloadableRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.loot.v3.FabricLootTableBuilder;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.fabricmc.fabric.impl.loot.FabricLootTable;
import net.fabricmc.fabric.impl.loot.LootUtil;

/**
 * Implements the events from {@link LootTableEvents}.
 */
@Mixin(ReloadableRegistries.class)
abstract class ReloadableRegistriesMixin {
	/**
	 * Due to possible cross-thread handling, this uses WeakHashMap instead of ThreadLocal.
	 */
	@Unique
	private static final WeakHashMap<RegistryOps<JsonElement>, RegistryWrapper.WrapperLookup> WRAPPERS = new WeakHashMap<>();

	@WrapOperation(method = "reload", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;getOps(Lcom/mojang/serialization/DynamicOps;)Lnet/minecraft/registry/RegistryOps;"))
	private static RegistryOps<JsonElement> storeOps(RegistryWrapper.WrapperLookup registries, DynamicOps<JsonElement> ops, Operation<RegistryOps<JsonElement>> original) {
		RegistryOps<JsonElement> created = original.call(registries, ops);
		WRAPPERS.put(created, registries);
		return created;
	}

	@WrapOperation(method = "reload", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenApplyAsync(Ljava/util/function/Function;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
	private static CompletableFuture<CombinedDynamicRegistries<ServerDynamicRegistryType>> removeOps(CompletableFuture<List<MutableRegistry<?>>> future, Function<? super List<MutableRegistry<?>>, ? extends CombinedDynamicRegistries<ServerDynamicRegistryType>> fn, Executor executor, Operation<CompletableFuture<CombinedDynamicRegistries<ServerDynamicRegistryType>>> original, @Local RegistryOps<JsonElement> ops) {
		return original.call(future.thenApply(v -> {
			WRAPPERS.remove(ops);
			return v;
		}), fn, executor);
	}

	@Inject(method = "method_61240", at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"))
	private static <T> void modifyLootTable(LootDataType<T> lootDataType, ResourceManager resourceManager, RegistryOps<JsonElement> registryOps, CallbackInfoReturnable<MutableRegistry<?>> cir, @Local Map<Identifier, T> map) {
		map.replaceAll((identifier, t) -> modifyLootTable(t, identifier, registryOps));
	}

	@Unique
	private static <T> T modifyLootTable(T value, Identifier id, RegistryOps<JsonElement> ops) {
		if (!(value instanceof LootTable table)) return value;

		RegistryKey<LootTable> key = RegistryKey.of(RegistryKeys.LOOT_TABLE, id);
		// Populated above.
		RegistryWrapper.WrapperLookup registries = WRAPPERS.get(ops);
		// Populated inside JsonDataLoaderMixin
		LootTableSource source = LootUtil.SOURCES.get().getOrDefault(id, LootTableSource.DATA_PACK);
		// Invoke the REPLACE event for the current loot table.
		LootTable replacement = LootTableEvents.REPLACE.invoker().replaceLootTable(key, table, source, registries);

		if (replacement != null) {
			// Set the loot table to MODIFY to be the replacement loot table.
			// The MODIFY event will also see it as a replaced loot table via the source.
			table = replacement;
			source = LootTableSource.REPLACED;
		}

		// Turn the current table into a modifiable builder and invoke the MODIFY event.
		LootTable.Builder builder = FabricLootTableBuilder.copyOf(table);
		LootTableEvents.MODIFY.invoker().modifyLootTable(key, builder, source, registries);

		return (T) builder.build();
	}

	@SuppressWarnings("unchecked")
	@Inject(method = "method_61240", at = @At("RETURN"))
	private static <T> void onLootTablesLoaded(LootDataType<T> lootDataType, ResourceManager resourceManager, RegistryOps<JsonElement> registryOps, CallbackInfoReturnable<MutableRegistry<?>> cir) {
		if (lootDataType != LootDataType.LOOT_TABLES) return;

		Registry<LootTable> lootTableRegistry = (Registry<LootTable>) cir.getReturnValue();

		LootTableEvents.ALL_LOADED.invoker().onLootTablesLoaded(resourceManager, lootTableRegistry);
		LootUtil.SOURCES.remove();
		lootTableRegistry.streamEntries().forEach(reference -> ((FabricLootTable) reference.value()).fabric$setRegistryEntry(reference));
	}
}
