package net.fabricmc.fabric.api.client.debug.v1.debugScreen;

import net.fabricmc.fabric.impl.debug.client.debugScreen.DebugScreenProfileImpl;

import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.resources.Identifier;

public class DebugScreenProfile {
	public static void set(Identifier identifier, net.minecraft.client.gui.components.debug.DebugScreenProfile profile, DebugScreenEntryStatus status) {
		DebugScreenProfileImpl.register(profile, identifier, status);
	}
}
