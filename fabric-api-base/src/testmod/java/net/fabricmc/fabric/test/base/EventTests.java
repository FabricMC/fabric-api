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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.impl.base.toposort.NodeSorting;

public class EventTests {
	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-api-base");

	public static void run() {
		long time1 = System.currentTimeMillis();

		testDefaultPhaseOnly();
		testMultipleDefaultPhases();
		testAddedPhases();
		testCycle();
		NodeSorting.ENABLE_CYCLE_WARNING = false;
		testDeterministicOrdering();
		testTwoCycles();
		NodeSorting.ENABLE_CYCLE_WARNING = true;

		long time2 = System.currentTimeMillis();
		LOGGER.info("Event unit tests succeeded in {} milliseconds.", time2 - time1);
	}

	private static final Function<Test[], Test> INVOKER_FACTORY = listeners -> () -> {
		for (Test test : listeners) {
			test.onTest();
		}
	};

	private static int currentListener = 0;

	private static Event<Test> createEvent() {
		return EventFactory.createArrayBacked(Test.class, INVOKER_FACTORY);
	}

	private static Test ensureOrder(int order) {
		return () -> {
			assertEquals(order, currentListener);
			++currentListener;
		};
	}

	private static void testDefaultPhaseOnly() {
		Event<Test> event = createEvent();

		event.register(ensureOrder(0));
		event.register(Event.DEFAULT_PHASE, ensureOrder(1));
		event.register(ensureOrder(2));

		event.invoker().onTest();
		assertEquals(3, currentListener);
		currentListener = 0;
	}

	private static void testMultipleDefaultPhases() {
		Identifier first = Identifier.fromNamespaceAndPath("fabric", "first");
		Identifier second = Identifier.fromNamespaceAndPath("fabric", "second");
		Event<Test> event = EventFactory.createWithPhases(Test.class, INVOKER_FACTORY, first, second, Event.DEFAULT_PHASE);

		event.register(second, ensureOrder(1));
		event.register(ensureOrder(2));
		event.register(first, ensureOrder(0));

		for (int i = 0; i < 5; ++i) {
			event.invoker().onTest();
			assertEquals(3, currentListener);
			currentListener = 0;
		}
	}

	private static void testAddedPhases() {
		Event<Test> event = createEvent();

		Identifier veryEarly = Identifier.fromNamespaceAndPath("fabric", "very_early");
		Identifier early = Identifier.fromNamespaceAndPath("fabric", "early");
		Identifier late = Identifier.fromNamespaceAndPath("fabric", "late");
		Identifier veryLate = Identifier.fromNamespaceAndPath("fabric", "very_late");

		event.addPhaseOrdering(veryEarly, early);
		event.addPhaseOrdering(early, Event.DEFAULT_PHASE);
		event.addPhaseOrdering(Event.DEFAULT_PHASE, late);
		event.addPhaseOrdering(late, veryLate);

		event.register(ensureOrder(4));
		event.register(ensureOrder(5));
		event.register(veryEarly, ensureOrder(0));
		event.register(early, ensureOrder(2));
		event.register(late, ensureOrder(6));
		event.register(veryLate, ensureOrder(8));
		event.register(veryEarly, ensureOrder(1));
		event.register(veryLate, ensureOrder(9));
		event.register(late, ensureOrder(7));
		event.register(early, ensureOrder(3));

		for (int i = 0; i < 5; ++i) {
			event.invoker().onTest();
			assertEquals(10, currentListener);
			currentListener = 0;
		}
	}

	private static void testCycle() {
		Event<Test> event = createEvent();

		Identifier a = Identifier.fromNamespaceAndPath("fabric", "a");
		Identifier b1 = Identifier.fromNamespaceAndPath("fabric", "b1");
		Identifier b2 = Identifier.fromNamespaceAndPath("fabric", "b2");
		Identifier b3 = Identifier.fromNamespaceAndPath("fabric", "b3");
		Identifier c = Event.DEFAULT_PHASE;

		// A always first and C always last.
		event.register(a, ensureOrder(0));
		event.register(c, ensureOrder(4));
		event.register(b1, ensureOrder(1));
		event.register(b1, ensureOrder(2));
		event.register(b1, ensureOrder(3));

		// A -> B
		event.addPhaseOrdering(a, b1);
		// B -> C
		event.addPhaseOrdering(b3, c);
		// loop
		event.addPhaseOrdering(b1, b2);
		event.addPhaseOrdering(b2, b3);
		event.addPhaseOrdering(b3, b1);

		for (int i = 0; i < 5; ++i) {
			event.invoker().onTest();
			assertEquals(5, currentListener);
			currentListener = 0;
		}
	}

	/**
	 * Ensure that phases get sorted deterministically regardless of the order in which constraints are registered.
	 *
	 * <p>The graph is displayed here as ascii art, and also in the file graph.png.
	 * <pre>
	 *             +-------------------+
	 *             v                   |
	 * +---+     +---+     +---+     +---+
	 * | a | --> | z | --> | b | --> | y |
	 * +---+     +---+     +---+     +---+
	 *             ^
	 *             |
	 *             |
	 * +---+     +---+
	 * | d | --> | e |
	 * +---+     +---+
	 * +---+
	 * | f |
	 * +---+
	 * </pre>
	 * Notice the cycle z -> b -> y -> z. The elements of the cycle are ordered [b, y, z], and the cycle itself is ordered with its lowest id "b".
	 * We get for the final order: [a, d, e, cycle [b, y, z], f].
	 */
	private static void testDeterministicOrdering() {
		Identifier a = Identifier.fromNamespaceAndPath("fabric", "a");
		Identifier b = Identifier.fromNamespaceAndPath("fabric", "b");
		Identifier d = Identifier.fromNamespaceAndPath("fabric", "d");
		Identifier e = Identifier.fromNamespaceAndPath("fabric", "e");
		Identifier f = Identifier.fromNamespaceAndPath("fabric", "f");
		Identifier y = Identifier.fromNamespaceAndPath("fabric", "y");
		Identifier z = Identifier.fromNamespaceAndPath("fabric", "z");

		List<Consumer<Event<Test>>> dependencies = List.of(
				ev -> ev.addPhaseOrdering(a, z),
				ev -> ev.addPhaseOrdering(d, e),
				ev -> ev.addPhaseOrdering(e, z),
				ev -> ev.addPhaseOrdering(z, b),
				ev -> ev.addPhaseOrdering(b, y),
				ev -> ev.addPhaseOrdering(y, z)
		);

		testAllPermutations(new ArrayList<>(), dependencies, selectedDependencies -> {
			Event<Test> event = createEvent();

			for (Consumer<Event<Test>> dependency : selectedDependencies) {
				dependency.accept(event);
			}

			event.register(a, ensureOrder(0));
			event.register(d, ensureOrder(1));
			event.register(e, ensureOrder(2));
			event.register(b, ensureOrder(3));
			event.register(y, ensureOrder(4));
			event.register(z, ensureOrder(5));
			event.register(f, ensureOrder(6));

			event.invoker().onTest();
			assertEquals(7, currentListener);
			currentListener = 0;
		});
	}

	/**
	 * Test deterministic phase sorting with two cycles.
	 * <pre>
	 * e --> a <--> b <-- d <--> c
	 * </pre>
	 */
	private static void testTwoCycles() {
		Identifier a = Identifier.fromNamespaceAndPath("fabric", "a");
		Identifier b = Identifier.fromNamespaceAndPath("fabric", "b");
		Identifier c = Identifier.fromNamespaceAndPath("fabric", "c");
		Identifier d = Identifier.fromNamespaceAndPath("fabric", "d");
		Identifier e = Identifier.fromNamespaceAndPath("fabric", "e");

		List<Consumer<Event<Test>>> dependencies = List.of(
				ev -> ev.addPhaseOrdering(e, a),
				ev -> ev.addPhaseOrdering(a, b),
				ev -> ev.addPhaseOrdering(b, a),
				ev -> ev.addPhaseOrdering(d, b),
				ev -> ev.addPhaseOrdering(d, c),
				ev -> ev.addPhaseOrdering(c, d)
		);

		testAllPermutations(new ArrayList<>(), dependencies, selectedDependencies -> {
			Event<Test> event = createEvent();

			for (Consumer<Event<Test>> dependency : selectedDependencies) {
				dependency.accept(event);
			}

			event.register(c, ensureOrder(0));
			event.register(d, ensureOrder(1));
			event.register(e, ensureOrder(2));
			event.register(a, ensureOrder(3));
			event.register(b, ensureOrder(4));

			event.invoker().onTest();
			assertEquals(5, currentListener);
			currentListener = 0;
		});
	}

	@SuppressWarnings("SuspiciousListRemoveInLoop")
	private static <T> void testAllPermutations(List<T> selected, List<T> toSelect, Consumer<List<T>> action) {
		if (toSelect.size() == 0) {
			action.accept(selected);
		} else {
			for (int i = 0; i < toSelect.size(); ++i) {
				selected.add(toSelect.get(i));
				List<T> remaining = new ArrayList<>(toSelect);
				remaining.remove(i);
				testAllPermutations(selected, remaining, action);
				selected.remove(selected.size()-1);
			}
		}
	}

	@FunctionalInterface
	interface Test {
		void onTest();
	}

	private static void assertEquals(Object expected, Object actual) {
		if (!Objects.equals(expected, actual)) {
			throw new AssertionError(String.format("assertEquals failed%nexpected: %s%n but was: %s", expected, actual));
		}
	}
}
