package net.fabricmc.fabric.impl.test;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.test.EventScope;
import net.fabricmc.loader.api.FabricLoader;

public final class EventTestingImpl {
	private EventTestingImpl() {
	}

	public static <T> EventScope registerScoped(Event<T> event, Identifier phase, T listener) {
		if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
			throw new IllegalArgumentException("EventScopes only work in development environments!");
		}

		if (!(event instanceof TestableArrayBackedEvent<T> testableEvent)) {
			throw new IllegalArgumentException("Event is not testable, something has gone very wrong!");
		}

		event.register(phase, listener);
		return new EventScopeImpl<>(testableEvent, phase, listener);
	}
}
