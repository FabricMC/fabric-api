package net.fabricmc.fabric.impl.holder.component;

import net.fabricmc.fabric.api.holder.component.FabricDataComponentInitializer;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Map;

public record FabricDataComponentInitContextImpl(
		HolderLookup.Provider lookupProvider,
		ResourceManager resourceManager,
		Map<ResourceKey<?>, DataComponentMap.Builder> builders
) implements FabricDataComponentInitializer.Context {
	@Override
	public DataComponentMap.Builder builder(ResourceKey<?> key) {
		return builders.computeIfAbsent(key, _ -> DataComponentMap.builder());
	}
}
