package net.fabricmc.fabric.impl.holder.component;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.holder.component.FabricDataComponentInitializers;

public class HolderComponentEntrypoint implements ModInitializer {
	@Override
	public void onInitialize() {
		FabricDataComponentInitializers.register(new DataHolderComponentInitializer());
	}
}
