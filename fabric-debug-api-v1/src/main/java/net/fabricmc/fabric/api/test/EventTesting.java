package net.fabricmc.fabric.api.test;

import net.fabricmc.fabric.api.event.Event;

import net.fabricmc.fabric.impl.test.EventTestingImpl;

import net.minecraft.resources.Identifier;

public final class EventTesting {
	private EventTesting() {
	}

	public static <T> EventScope registerScoped(Event<T> event, T listener) {
		return registerScoped(event, Event.DEFAULT_PHASE, listener);
	}

	public static <T> EventScope registerScoped(Event<T> event, Identifier phase, T listener) {
		return EventTestingImpl.registerScoped(event, phase, listener);
	}
}
