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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.impl.base.event.CombinedInvokerFactory;
import net.fabricmc.fabric.impl.base.event.CombinedInvokerFactory.Mode;
import net.fabricmc.fabric.impl.base.event.EventFactoryImpl;

public class EventDelegatingTest {
	public interface Callback {
		void run(List<String> out, int a, long b, double c, String d);
	}

	public interface Returning {
		boolean run(List<String> out);
	}

	private static Event<Callback> create() {
		return EventFactory.create(Callback.class);
	}

	private static Callback tag(String s) {
		return (out, a, b, c, d) -> out.add(s + a + b + c + d);
	}

	private static List<String> fire(Event<Callback> e) {
		List<String> out = new ArrayList<>();
		e.invoker().run(out, 1, 2L, 3.5, "x");
		return out;
	}

	@Test
	void generatedInvokerMatchesLoop() {
		for (int n = 0; n <= 20; n++) {
			Event<Callback> e = create();
			List<String> expected = new ArrayList<>();

			for (int i = 0; i < n; i++) {
				e.register(tag("l" + i + ":"));
				expected.add("l" + i + ":" + "12" + "3.5" + "x");
			}

			// Before and after the background upgrade, the behavior is identical.
			assertEquals(expected, fire(e), "n=" + n + " before upgrade");
			EventFactoryImpl.awaitInvokerGeneration();
			assertEquals(expected, fire(e), "n=" + n + " after upgrade");
		}
	}

	@Test
	void registeringAfterUpgradeStillWorks() {
		Event<Callback> e = create();
		e.register(tag("a"));
		e.register(tag("b"));
		EventFactoryImpl.awaitInvokerGeneration();
		Callback generated = EventFactoryImpl.underlyingInvoker(e);

		e.register(tag("c"));
		assertEquals(3, fire(e).size());
		EventFactoryImpl.awaitInvokerGeneration();
		assertNotEquals(generated.getClass(), EventFactoryImpl.underlyingInvoker(e).getClass());
		assertEquals(3, fire(e).size());
	}

	@Test
	void invokerNeverChangesAndFollowsTheListeners() {
		Event<Callback> e = create();
		Callback invoker = e.invoker();
		assertEquals(List.of(), fire(e));

		for (int n = 1; n <= 20; n++) {
			e.register(tag("l" + n + ":"));
			assertSame(invoker, e.invoker(), "invoker() changed after registering " + n + " listeners");
			assertEquals(n, fire(e).size());

			if (n == 3) {
				// Also after the background thread swapped in the unrolled invoker.
				EventFactoryImpl.awaitInvokerGeneration();
				assertSame(invoker, e.invoker());
				assertEquals(3, fire(e).size());
			}
		}

		EventFactoryImpl.awaitInvokerGeneration();
		assertSame(invoker, e.invoker());
		assertEquals(20, fire(e).size());
	}

	@Test
	void registeringOnTheLoopKeepsTheTarget() {
		Event<Callback> e = create();
		int onLoop;
		Runnable unblock = EventFactoryImpl.blockInvokerGeneration();

		try {
			int initial = EventFactoryImpl.retargetCount(e);
			e.register(tag("a")); // the listener itself
			e.register(tag("b")); // the loop invoker of the event
			onLoop = EventFactoryImpl.retargetCount(e);
			assertEquals(initial + 2, onLoop);

			for (int i = 0; i < 10; i++) {
				e.register(tag("c" + i));
				assertEquals(i + 3, fire(e).size());
			}

			// Only the listener array of the loop invoker changed: no new target, nothing to recompile.
			assertEquals(onLoop, EventFactoryImpl.retargetCount(e));
		} finally {
			unblock.run();
		}

		// Once registrations settled: a single new target, the unrolled invoker.
		EventFactoryImpl.awaitInvokerGeneration();
		assertEquals(onLoop + 1, EventFactoryImpl.retargetCount(e));
		assertEquals(12, fire(e).size());
	}

	@Test
	void createWithBehaviorAndPhases() {
		Identifier early = Identifier.fromNamespaceAndPath("test", "early");
		Event<Returning> e = EventFactory.createWithPhases(Returning.class, EventFactory.Behavior.STOP_ON_FALSE, early, Event.DEFAULT_PHASE);
		List<String> out = new ArrayList<>();
		e.register(o -> !o.add("default"));
		e.register(early, o -> o.add("early"));
		assertEquals(false, e.invoker().run(out));
		assertEquals(List.of("early", "default"), out);
	}

	@Test
	void generatedInvokerFactoryWorksOnItsOwn() {
		Function<Returning[], Returning> invokers = CombinedInvokerFactory.create(Returning.class, Mode.SHORT_CIRCUIT, Boolean.TRUE, -1);
		List<String> out = new ArrayList<>();
		Returning allow = o -> o.add("allow");
		Returning deny = o -> !o.add("deny");

		assertEquals(true, invokers.apply(new Returning[0]).run(out));
		assertSame(allow, invokers.apply(new Returning[] {allow}));
		assertEquals(false, invokers.apply(new Returning[] {allow, deny, allow}).run(out));
		assertEquals(List.of("allow", "deny"), out);

		// One invoker factory can be shared by several events.
		Event<Returning> first = EventFactory.createArrayBacked(Returning.class, invokers);
		Event<Returning> second = EventFactory.createWithPhases(Returning.class, invokers, Event.DEFAULT_PHASE);
		first.register(deny);
		second.register(allow);
		assertEquals(false, first.invoker().run(new ArrayList<>()));
		assertEquals(true, second.invoker().run(new ArrayList<>()));
	}

	@Test
	void exceptionStopsLaterListeners() {
		Event<Callback> e = create();
		List<String> out = new ArrayList<>();
		e.register((o, a, b, c, d) -> o.add("first"));
		e.register((o, a, b, c, d) -> {
			throw new IllegalStateException("boom");
		});
		e.register((o, a, b, c, d) -> o.add("never"));
		EventFactoryImpl.awaitInvokerGeneration();
		assertThrows(IllegalStateException.class, () -> e.invoker().run(out, 0, 0, 0, ""));
		assertEquals(List.of("first"), out);
	}

	@Test
	void singleListenerIsUsedDirectly() {
		Event<Callback> e = create();
		Callback only = tag("only");
		e.register(only);
		assertSame(only, EventFactoryImpl.underlyingInvoker(e));
	}

	@Test
	void booleanShortCircuitStopsAtFirstFalse() {
		Event<Returning> e = EventFactory.create(Returning.class, EventFactory.Behavior.STOP_ON_FALSE);
		List<String> out = new ArrayList<>();
		e.register(o -> {
			o.add("a");
			return true;
		});
		e.register(o -> {
			o.add("b");
			return false;
		});
		e.register(o -> o.add("c"));
		EventFactoryImpl.awaitInvokerGeneration();
		assertEquals(false, e.invoker().run(out));
		assertEquals(List.of("a", "b"), out);
	}

	@Test
	void phasesAreRespected() {
		Identifier early = Identifier.fromNamespaceAndPath("test", "early");
		Event<Callback> e = create();
		e.register(tag("d1"));
		e.register(early, tag("e1"));
		e.register(tag("d2"));
		e.addPhaseOrdering(early, Event.DEFAULT_PHASE);
		EventFactoryImpl.awaitInvokerGeneration();
		assertEquals(3, fire(e).size());
		assertEquals("e1", fire(e).getFirst().substring(0, 2));
	}
}
