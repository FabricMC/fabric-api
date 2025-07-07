package net.fabricmc.fabric.impl.networking;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.CustomClickActionListener;

public class CustomClickEventHandlerRegistry {
	private static final Map<Identifier, Event<CustomClickActionListener>> REGISTRY = new HashMap<>();

	public static Event<CustomClickActionListener> getOrCreateListenerEvent(Identifier id) {
		return REGISTRY.computeIfAbsent(
				id,
				idx -> {
					return EventFactory.createArrayBacked(
							CustomClickActionListener.class,
							listeners -> (player, payload) -> {
								for (CustomClickActionListener listener : listeners) {
									listener.handleCustomClickAction(player, payload);
								}
							}
					);
				}
		);
	}

	public static void invokeListenerEvent(Identifier id, ServerPlayerEntity player, Optional<NbtElement> payload) {
		Event<CustomClickActionListener> event = REGISTRY.get(id);
		if (event != null) {
			event.invoker().handleCustomClickAction(player, payload.orElse(null));
		}
	}

	private CustomClickEventHandlerRegistry() {
	}
}
