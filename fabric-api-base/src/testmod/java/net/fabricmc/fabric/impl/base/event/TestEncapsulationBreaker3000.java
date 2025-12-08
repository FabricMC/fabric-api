package net.fabricmc.fabric.impl.base.event;

import net.fabricmc.fabric.api.event.Event;

public class TestEncapsulationBreaker3000 {
	private TestEncapsulationBreaker3000() {}

	public static <T> Class<T> getClassOfEvent(Event<T> event) {
		// spooky!
		if (!(event instanceof ArrayBackedEvent<T> arrayBackedEvent)) {
			throw new IllegalArgumentException("This event isn't an ArrayBackedEvent, something has gone horribly wrong");
		}

		return arrayBackedEvent.getInnerClass();
	}
}
