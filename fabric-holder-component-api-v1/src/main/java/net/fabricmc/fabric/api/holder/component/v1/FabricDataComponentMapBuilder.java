package net.fabricmc.fabric.api.holder.component.v1;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;

/// Extensions for [DataComponentMap.Builder]. Implemented via interface injection, do not implement yourself!
public interface FabricDataComponentMapBuilder {
	default DataComponentMap.Builder apply(DataComponentPatch patch) {
		throw new UnsupportedOperationException("Implemented via mixin.");
	}

	default DataComponentMap.Builder clear() {
		throw new UnsupportedOperationException("Implemented via mixin.");
	}
}
