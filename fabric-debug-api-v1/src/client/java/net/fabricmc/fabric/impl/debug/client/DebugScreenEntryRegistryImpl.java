package net.fabricmc.fabric.impl.debug.client;

import net.fabricmc.fabric.api.client.debug.v1.DebugKeyBindingRegistry;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DebugScreenEntryRegistryImpl {
	private static final Map<Identifier, DebugScreenEntry> ADDITIONAL_DEBUG_SCREEN_ENTRIES = new ConcurrentHashMap<>();

	public static void register(Identifier identifier, DebugScreenEntry debugScreenEntry) {
		if (ADDITIONAL_DEBUG_SCREEN_ENTRIES.containsKey(identifier)) {
			throw new IllegalStateException(
					"Identifier `" + identifier.toString() + "` is already registered"
			);
		}
		ADDITIONAL_DEBUG_SCREEN_ENTRIES.put(identifier, debugScreenEntry);
	}

	public static void addEntries(Map<Identifier, DebugScreenEntry> entries) {
		entries.putAll(ADDITIONAL_DEBUG_SCREEN_ENTRIES);
	}

}
