package net.fabricmc.fabric.impl.test;

import net.fabricmc.fabric.impl.base.event.ArrayBackedEvent;

import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.function.Function;

class TestableArrayBackedEvent<T> extends ArrayBackedEvent<T> {
	TestableArrayBackedEvent(Class<? super T> type, Function<T[], T> invokerFactory) {
		super(type, invokerFactory);
	}

	public void unregister(Identifier phaseIdentifier, T listener) {
		Objects.requireNonNull(phaseIdentifier, "Tried to unregister a listener for a null phase!");
		Objects.requireNonNull(listener, "Tried to unregister a null listener!");

		synchronized (lock) {
			if (getOrCreatePhase(phaseIdentifier, false).removeListener(listener)) {
				rebuildInvoker(handlers.length - 1);
			}
		}
	}
}
