package net.fabricmc.fabric.impl.debug.client.debugScreen;

import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class DebugScreenProfileImpl {
	private static final Map<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>> pending = new HashMap<>();
	private static boolean frozen = false;

	public static void register(DebugScreenProfile profile, Identifier identifier, DebugScreenEntryStatus status) {
		checkNotFrozen();
		pending.computeIfAbsent(profile, p -> new HashMap<>()).put(identifier, status);
	}

	private static void checkNotFrozen() {
		if (frozen) {
			throw new IllegalStateException(
					"Cannot register debug profile entries after DebugScreenEntries has initialized. Register during mod init.");
		}
	}

	public static Map<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>> invoke(
			Map<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>> base
	) {
		frozen = true;

		Map<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>> result = new HashMap<>();
		base.forEach((profile, entries) -> result.put(profile, new HashMap<>(entries)));

		pending.forEach((profile, extra) ->
				result.computeIfAbsent(profile, p -> new HashMap<>()).putAll(extra));

		Map<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>> ImmutableCopy = new HashMap<>();
		result.forEach((profile, entries) -> ImmutableCopy.put(profile, Map.copyOf(entries)));

		return Map.copyOf(ImmutableCopy);
	}

}
