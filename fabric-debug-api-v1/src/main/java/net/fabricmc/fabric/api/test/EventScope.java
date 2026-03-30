package net.fabricmc.fabric.api.test;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.test.EventTestingImpl;

/**
 * Represents a wrapper around a short-lived {@link Event}.
 * This class implements {@link AutoCloseable} and is intended to be used in a try-with-resources statement. When
 * closed, the Event be unregistered.
 */
@ApiStatus.NonExtendable
public interface EventScope extends AutoCloseable {
	@Override
	void close();

	static <T> EventScope registerScoped(Event<T> event, T listener) {
		return registerScoped(event, Event.DEFAULT_PHASE, listener);
	}

	static <T> EventScope registerScoped(Event<T> event, Identifier phase, T listener) {
		return EventTestingImpl.registerScoped(event, phase, listener);
	}
}
