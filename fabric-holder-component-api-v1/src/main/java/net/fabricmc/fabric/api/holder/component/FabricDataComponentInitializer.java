package net.fabricmc.fabric.api.holder.component;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;

/// Extended version of [DataComponentInitializers.Initializer] that allows adding components to arbitrary holders instead of having to define what holders are to be given components at registration.
@FunctionalInterface
public interface FabricDataComponentInitializer {
	void run(Context context);

	interface Context {
		HolderLookup.Provider lookupProvider();
		ResourceManager resourceManager();

		DataComponentMap.Builder builder(ResourceKey<?> key);
	}
}
