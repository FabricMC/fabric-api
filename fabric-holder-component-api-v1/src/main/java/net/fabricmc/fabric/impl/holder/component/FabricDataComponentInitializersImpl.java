package net.fabricmc.fabric.impl.holder.component;

import net.fabricmc.fabric.api.holder.component.FabricDataComponentInitializer;

import net.minecraft.server.packs.resources.ResourceManager;

import java.util.ArrayList;
import java.util.List;

public class FabricDataComponentInitializersImpl {
	public static final List<FabricDataComponentInitializer> INITIALIZERS = new ArrayList<>();

	public static final ScopedValue<ResourceManager> RESOURCE_MANAGER = ScopedValue.newInstance();
}
