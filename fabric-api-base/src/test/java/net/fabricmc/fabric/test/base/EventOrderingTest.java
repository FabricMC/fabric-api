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

package net.fabricmc.fabric.test.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class EventOrderingTest {
	private static final Identifier FIRST = Identifier.fromNamespaceAndPath("test", "first");
	private static final Identifier SECOND = Identifier.fromNamespaceAndPath("test", "second");
	private static final Identifier THIRD = Identifier.fromNamespaceAndPath("test", "third");

	private interface Callback {
		void run(List<String> out);
	}

	private static Event<Callback> create() {
		return EventFactory.createArrayBacked(Callback.class, listeners -> out -> {
			for (Callback c : listeners) c.run(out);
		});
	}

	private static Callback tag(String s) {
		return out -> out.add(s);
	}

	private static List<String> fire(Event<Callback> event) {
		List<String> out = new ArrayList<>();
		event.invoker().run(out);
		return out;
	}

	@Test
	void registrationOrderWithinPhase() {
		Event<Callback> e = create();
		e.register(tag("a"));
		e.register(tag("b"));
		e.register(tag("c"));
		assertEquals(List.of("a", "b", "c"), fire(e));
	}

	@Test
	void phasesOrderedAfterListenersRegistered() {
		Event<Callback> e = create();
		e.register(tag("d1"));
		e.register(SECOND, tag("s1"));
		e.register(FIRST, tag("f1"));
		e.register(tag("d2"));
		e.register(FIRST, tag("f2"));
		// No ordering: sorted by id. default < test:first < test:second.
		assertEquals(List.of("d1", "d2", "f1", "f2", "s1"), fire(e));

		e.addPhaseOrdering(SECOND, FIRST);
		e.addPhaseOrdering(FIRST, Event.DEFAULT_PHASE);
		assertEquals(List.of("s1", "f1", "f2", "d1", "d2"), fire(e));

		// Duplicate ordering and registration into existing phase keep things stable.
		e.addPhaseOrdering(SECOND, FIRST);
		e.register(SECOND, tag("s2"));
		e.register(THIRD, tag("t1"));
		assertEquals(List.of("s1", "s2", "f1", "f2", "d1", "d2", "t1"), fire(e));
	}

	@Test
	void ordersWithEmptyPhasesDoNotAffectInvoker() {
		Event<Callback> e = create();
		e.register(tag("a"));
		Callback before = e.invoker();
		e.addPhaseOrdering(FIRST, SECOND);
		assertEquals(List.of("a"), fire(e));
		e.register(SECOND, tag("s"));
		e.register(FIRST, tag("f"));
		assertEquals(List.of("a", "f", "s"), fire(e));
		assertNotSame(before, e.invoker());
	}

	@Test
	void defaultPhases() {
		Event<Callback> e = EventFactory.createWithPhases(Callback.class, listeners -> out -> {
			for (Callback c : listeners) c.run(out);
		}, SECOND, FIRST, Event.DEFAULT_PHASE, THIRD);
		e.register(THIRD, tag("t"));
		e.register(tag("d"));
		e.register(FIRST, tag("f"));
		e.register(SECOND, tag("s"));
		assertEquals(List.of("s", "f", "d", "t"), fire(e));
	}
}
