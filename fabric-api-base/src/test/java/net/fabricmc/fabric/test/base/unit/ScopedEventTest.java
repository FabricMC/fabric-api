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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.test.base.EventScope;
import net.fabricmc.fabric.test.base.ModMussScopedEvent;

public class ScopedEventTest {
	private static final Event<SimpleEvent> SIMPLE_EVENT = EventFactory.createArrayBacked(SimpleEvent.class, callbacks -> arg -> {
		for (SimpleEvent callback : callbacks) {
			callback.onEvent(arg);
		}
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

	private static final ModMussScopedEvent<SimpleEvent> SIMPLE_EVENT_SCOPE = new ModMussScopedEvent<>(SIMPLE_EVENT, SimpleEvent.class);

	@Test
	void sylvEventScopeTest() {
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
	void sylvReturnEventScopeTest() {
		try (var eventScope = new EventScope()) {
			eventScope.register(RETURN_EVENT, arg -> {
				System.out.println(arg);
				return arg.toUpperCase(Locale.ROOT);
			});
			assertEquals("HELLO WORLD", RETURN_EVENT.invoker().onEvent("Hello World"));
		}

		// this would be ignored if the scoped listener still worked
		RETURN_EVENT.register(arg -> arg);
		assertEquals("Hello World", RETURN_EVENT.invoker().onEvent("Hello World"));
	}

	@Test
	void eventScopeTest() {
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
	void returnEventScopeTest() {
		// moved this because it was interfering with sylvReturnEventScopeTest
		final ModMussScopedEvent<ReturnEvent> RETURN_EVENT_SCOPE = new ModMussScopedEvent<>(RETURN_EVENT, ReturnEvent.class, "Hello World");

		assertEquals("Hello World", RETURN_EVENT.invoker().onEvent("Hello World"));

		try (var ignored = RETURN_EVENT_SCOPE.register(arg -> arg.toUpperCase(Locale.ROOT))) {
			assertEquals("HELLO WORLD", RETURN_EVENT.invoker().onEvent("Hello World"));
		}

		assertEquals("Hello World", RETURN_EVENT.invoker().onEvent("Hello World"));
	}

	interface SimpleEvent {
		void onEvent(String arg);
	}

	interface  ReturnEvent {
		String onEvent(String arg);
	}
}
