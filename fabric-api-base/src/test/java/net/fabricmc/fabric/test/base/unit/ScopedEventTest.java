/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.test.base.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.test.base.EventScope;
import net.fabricmc.fabric.test.base.ScopedEvent;

public class ScopedEventTest {
	private static final Event<SimpleEvent> SIMPLE_EVENT = EventFactory.createArrayBacked(SimpleEvent.class, callbacks -> arg -> {
		for (SimpleEvent callback : callbacks) {
			callback.onEvent(arg);
		}
	});

	private static final Event<BoolEvent> NON_TERMINAL_EVENT = EventFactory.createArrayBacked(BoolEvent.class, callbacks -> () -> {
		for (BoolEvent callback : callbacks) {
			// In a non-terminal event, the invoker passes to the next callback
			// given a 'pass condition'. In this case, the pass condition is
			// returning true.
			if (!callback.onEvent()) {
				return false;
			}
		}

		return true;
	});

	// Terminating early return events like this are janky with scoped events,
	// so we need multiple.
	// The reason why these are considered terminating is because it is
	// generally expected in the tests that they return a default value rather
	// than the pass condition of null. In theory they are non-terminal; in
	// practice, they terminate early.
	// TODO: Should we mention that scoped events don't support early-terminating events?
	private static final Event<ReturnEvent> LAMBDA_RETURN_EVENT = EventFactory.createArrayBacked(ReturnEvent.class, callbacks -> arg -> {
		for (ReturnEvent callback : callbacks) {
			String result = callback.onEvent(arg);

			if (result != null) {
				return result;
			}
		}

		return null;
	});

	private static final Event<ReturnEvent> RETURN_EVENT = EventFactory.createArrayBacked(ReturnEvent.class, callbacks -> arg -> {
		for (ReturnEvent callback : callbacks) {
			String result = callback.onEvent(arg);

			if (result != null) {
				return result;
			}
		}

		return null;
	});

	private static final Event<ReturnEvent> SCOPED_EVENT_RETURN_EVENT = EventFactory.createArrayBacked(ReturnEvent.class, callbacks -> arg -> {
		for (ReturnEvent callback : callbacks) {
			String result = callback.onEvent(arg);

			if (result != null) {
				return result;
			}
		}

		return null;
	});

	private static final ScopedEvent<SimpleEvent> SIMPLE_EVENT_SCOPE = new ScopedEvent<>(SIMPLE_EVENT, SimpleEvent.class);

	@Test
	void eventScopeTest() {
		List<String> results = new ArrayList<>();

		try (var eventScope = new EventScope()) {
			eventScope.register(SIMPLE_EVENT, results::add);

			SIMPLE_EVENT.invoker().onEvent("2");
		}

		SIMPLE_EVENT.invoker().onEvent("3");

		assertEquals(1, results.size());
		assertEquals("2", results.getFirst());
	}

	@Test
	void returnEventScopeLambdaTest() {
		assertNull(LAMBDA_RETURN_EVENT.invoker().onEvent("Hello World"));

		try (var eventScope = new EventScope()) {
			eventScope.register(LAMBDA_RETURN_EVENT, arg -> arg.toUpperCase(Locale.ROOT));
			assertEquals("HELLO WORLD", LAMBDA_RETURN_EVENT.invoker().onEvent("Hello World"));
		}

		try (var eventScope = new EventScope()) {
			eventScope.registerEarlyReturn(
					LAMBDA_RETURN_EVENT,
					arg -> arg.toLowerCase(Locale.ROOT),
					args -> {
						String arg = (String) args[0];

						if (arg.toLowerCase(Locale.ROOT).equals("hello worlde")) {
							return "meow!";
						}

						return "Hello World";
					}
			);
			assertEquals("hello worlde", LAMBDA_RETURN_EVENT.invoker().onEvent("Hello Worlde"));
		}

		assertNotEquals("hello worlde", LAMBDA_RETURN_EVENT.invoker().onEvent("Hello Worlde"));
		assertEquals("meow!", LAMBDA_RETURN_EVENT.invoker().onEvent("Hello Worlde"));

		assertEquals("Hello World", LAMBDA_RETURN_EVENT.invoker().onEvent(" Hasdfello World plus some other nonsense"));
	}

	@Test
	void returnEventScopeTest() {
		// We do not yet have a default value.
		assertNull(RETURN_EVENT.invoker().onEvent("Hello World"));

		try (var scope = new EventScope()) {
			scope.registerEarlyReturn(RETURN_EVENT, arg -> arg.toUpperCase(Locale.ROOT), "Hello World");
			assertEquals("HELLO WORLD", RETURN_EVENT.invoker().onEvent("Hello World"));
		}

		// We have a default value now.
		assertEquals("Hello World", RETURN_EVENT.invoker().onEvent("Hello World"));
	}

	@Test
	void nonTerminalEventScopeTest() {
		assertTrue(NON_TERMINAL_EVENT.invoker().onEvent());

		try (var eventScope = new EventScope()) {
			// a default value of true ensures the invoker passes to the next callbacks
			eventScope.registerEarlyReturn(NON_TERMINAL_EVENT, () -> false, true);
			assertFalse(NON_TERMINAL_EVENT.invoker().onEvent());
		}

		assertTrue(NON_TERMINAL_EVENT.invoker().onEvent());
	}

	@Test
	void scopedEventTest() {
		List<String> results = new ArrayList<>();

		SIMPLE_EVENT.invoker().onEvent("1");

		try (var ignored = SIMPLE_EVENT_SCOPE.register(results::add)) {
			SIMPLE_EVENT.invoker().onEvent("2");
		}

		SIMPLE_EVENT.invoker().onEvent("3");

		assertEquals(1, results.size());
		assertEquals("2", results.getFirst());
	}

	@Test
	void returnScopedEventTest() {
		// These interfere with event callback invocation.
		// *Do not* define these in static fields and then leave them.
		final ScopedEvent<ReturnEvent> returnScopedEvent = new ScopedEvent<>(
				SCOPED_EVENT_RETURN_EVENT, ReturnEvent.class, "Hello World");

		assertEquals("Hello World", SCOPED_EVENT_RETURN_EVENT.invoker().onEvent("Hello World"));

		try (var ignored = returnScopedEvent.register(arg -> arg.toUpperCase(Locale.ROOT))) {
			assertEquals("HELLO WORLD", SCOPED_EVENT_RETURN_EVENT.invoker().onEvent("Hello World"));
		}

		assertEquals("Hello World", SCOPED_EVENT_RETURN_EVENT.invoker().onEvent("Hello World"));
	}

	interface SimpleEvent {
		void onEvent(String arg);
	}

	interface ReturnEvent {
		String onEvent(String arg);
	}

	interface BoolEvent {
		boolean onEvent();
	}
}
