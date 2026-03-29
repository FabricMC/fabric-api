package net.fabricmc.fabric.impl.test;

import net.fabricmc.fabric.api.test.EventScope;

import net.minecraft.resources.Identifier;

public class EventScopeImpl<T> implements EventScope {
	private final TestableArrayBackedEvent<T> event;
	private final Identifier phase;
	private final T listener;

	public EventScopeImpl(TestableArrayBackedEvent<T> event, Identifier phase, T listener) {
		this.event = event;
		this.phase = phase;
		this.listener = listener;
	}

	@Override
	public void close() {
		event.unregister(phase, listener);
	}
}
