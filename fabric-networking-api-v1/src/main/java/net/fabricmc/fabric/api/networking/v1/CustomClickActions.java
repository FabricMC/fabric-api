package net.fabricmc.fabric.api.networking.v1;

import java.util.Objects;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.networking.CustomClickEventHandlerRegistry;

import net.minecraft.util.Identifier;

public final class CustomClickActions {
	public static Event<CustomClickActionListener> getListenerEvent(Identifier id) {
		Objects.requireNonNull(id, "ID cannot be null");
		return CustomClickEventHandlerRegistry.getOrCreateListenerEvent(id);
	}

	private CustomClickActions() {
	}
}
