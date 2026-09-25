package net.fabricmc.fabric.test.base;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.EventScope;

public class EventScopeTest {
	private static final Event<Foo> EVENT = EventFactory.createArrayBacked(
			Foo.class,
			listeners -> () -> {
				for (Foo listener : listeners) {
					if (!listener.doSomething()) {
						return false;
					}
				}

				return true;
			}
	);

	@BeforeAll
	static void bootstrap() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void testEventScope() {
		Foo foo = () -> false;

		try (EventScope _ = EventScope.create(EVENT, foo)) {
			assertFalse(EVENT.invoker().doSomething(), "Event Foo in EventScope was not registered.");
		}

		assertTrue(EVENT.invoker().doSomething(), "EventScope did not unregister event Foo after closing.");
	}

	private interface Foo {
		boolean doSomething();
	}
}
