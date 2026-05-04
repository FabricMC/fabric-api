package net.fabricmc.fabric.api.holder.component;

import net.fabricmc.fabric.impl.holder.component.FabricDataComponentInitializersImpl;

public final class FabricDataComponentInitializers {
	private FabricDataComponentInitializers() {
	}

	public static void register(FabricDataComponentInitializer initializer) {
		FabricDataComponentInitializersImpl.INITIALIZERS.add(initializer);
	}
}
