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

import org.junit.jupiter.api.Test;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.event.Combine;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.impl.base.event.EventFactoryImpl;

import static org.junit.jupiter.api.Assertions.*;

public class EventDelegatingTest {
	public interface Callback {
		void run(List<String> out, int a, long b, double c, String d);
	}

	public interface Returning {
		boolean run(List<String> out);
	}

	private static Event<Callback> create() {
		return EventFactory.createArrayBacked(Callback.class, Combine.CALL_ALL);
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
		Callback generated = e.invoker();

		e.register(tag("c"));
		assertEquals(3, fire(e).size());
		EventFactoryImpl.awaitInvokerGeneration();
		assertNotEquals(generated.getClass(), e.invoker().getClass());
		assertEquals(3, fire(e).size());
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
		assertSame(only, e.invoker());
	}

	@Test
	void booleanShortCircuitStopsAtFirstFalse() {
		Event<Returning> e = EventFactory.createArrayBacked(Returning.class, Combine.STOP_AT_FALSE);
		List<String> out = new ArrayList<>();
		e.register(o -> o.add("a") || true);
		e.register(o -> o.add("b") && false);
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
