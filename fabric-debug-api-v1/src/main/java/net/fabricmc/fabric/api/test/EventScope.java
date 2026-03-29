package net.fabricmc.fabric.api.test;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.test.EventTestingImpl;

@ApiStatus.NonExtendable
@FunctionalInterface
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
