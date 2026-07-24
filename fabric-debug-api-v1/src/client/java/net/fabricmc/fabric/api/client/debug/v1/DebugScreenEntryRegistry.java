package net.fabricmc.fabric.api.client.debug.v1;

import net.fabricmc.fabric.impl.debug.client.DebugScreenEntryRegistryImpl;

import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;

public final class DebugScreenEntryRegistry {
	public static void register(Identifier identifier, DebugScreenEntry debugScreenEntry) {
		DebugScreenEntryRegistryImpl.register(identifier, debugScreenEntry);
	}
}
