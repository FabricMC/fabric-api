package net.fabricmc.fabric.test.debug.dev;

import net.minecraft.gametest.framework.GameTestHelper;

import net.fabricmc.fabric.api.debug.dev.v1.EventScope;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.gametest.v1.GameTest;

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

	@GameTest
	public void testEventScope(GameTestHelper helper) {
		Foo foo = () -> false;

		try (EventScope _ = EventScope.registerScoped(EVENT, foo)) {
			helper.assertFalse(EVENT.invoker().doSomething(), "Event Foo in EventScope was not registered.");
		}

		helper.assertTrue(EVENT.invoker().doSomething(), "EventScope did not unregister event Foo after closing.");
		helper.succeed();
	}

	private interface Foo {
		boolean doSomething();
	}
}
