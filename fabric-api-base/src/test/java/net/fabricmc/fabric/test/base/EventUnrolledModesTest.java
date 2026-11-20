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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.EventFactory.Behavior;
import net.fabricmc.fabric.impl.base.event.CombinedInvokerFactory;
import net.fabricmc.fabric.impl.base.event.CombinedInvokerFactory.Mode;
import net.fabricmc.fabric.impl.base.event.EventFactoryImpl;

/**
 * Compares the generated invokers of every mode with the plain loops they replace.
 * The reference loops are written the way the events in Fabric API write them.
 */
public class EventUnrolledModesTest {
	private static final Random RNG = new Random(0xFAB1C);

	/**
	 * Every count up to 20, and the ones around the limit of unrolled invokers.
	 */
	private static final int[] COUNTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 32, 64, 127, 128, 129, 140};

	public record Res(int id) { }

	private static final Res PASS = new Res(-1);

	public interface VoidCb {
		void run(List<String> log, int a, long b, double c, String d);
	}

	public interface RefCb {
		Res run(List<String> log, long b, double c);
	}

	public interface BoolCb {
		boolean run(List<String> log, long b, double c);
	}

	public interface ChainStr {
		String run(List<String> log, long a, String s, double d);
	}

	public interface ChainLong {
		long run(List<String> log, int a, double b, long v);
	}

	/**
	 * Runs the event with every kind of invoker against the reference loop, which is the specification of the behavior.
	 */
	@SuppressWarnings("unchecked")
	private static <T> void fuzz(Class<T> type, Supplier<Event<T>> newEvent, Function<T[], T> reference,
			BiFunction<Integer, Random, T> make, BiFunction<T, List<String>, Object> fire) {
		for (int n : COUNTS) {
			for (int rep = 0; rep < (n <= 20 ? 25 : 3); rep++) {
				T[] listeners = (T[]) Array.newInstance(type, n);
				Event<T> event = newEvent.get();

				for (int i = 0; i < n; i++) {
					listeners[i] = make.apply(i, RNG);
					event.register(listeners[i]);
				}

				String context = type.getSimpleName() + " n=" + n + " rep=" + rep;
				T expectedInvoker = reference.apply(listeners);

				// Right after registration, the invoker may be the loop or the unrolled one, depending on the background thread.
				compare(expectedInvoker, event.invoker(), fire, context + " (right after registration)");

				EventFactoryImpl.awaitInvokerGeneration();
				compare(expectedInvoker, event.invoker(), fire, context);

				String name = EventFactoryImpl.underlyingInvoker(event).getClass().getName();

				if (n == 1) {
					assertSame(listeners[0], EventFactoryImpl.underlyingInvoker(event), context + " single listener");
				} else if (n == 0) {
					assertTrue(name.contains("FabricEvent$Invoker"), context + " " + name);
				} else if (n >= 2 && n <= EventFactoryImpl.MAX_UNROLLED_LISTENERS) {
					assertTrue(name.contains("FabricEvent$Unrolled" + n), context + " " + name);
				} else {
					assertTrue(name.contains("FabricEvent$Loop"), context + " " + name);
				}
			}
		}
	}

	private static <T> void compare(T expectedInvoker, T actualInvoker, BiFunction<T, List<String>, Object> fire, String context) {
		List<String> expectedLog = new ArrayList<>();
		List<String> actualLog = new ArrayList<>();
		Object expected = fire.apply(expectedInvoker, expectedLog);
		Object actual = fire.apply(actualInvoker, actualLog);
		assertEquals(expected, actual, context + " result");
		assertEquals(expectedLog, actualLog, context + " calls");
	}

	@Test
	void voidMode() {
		Function<VoidCb[], VoidCb> reference = listeners -> (log, a, b, c, d) -> {
			for (VoidCb l : listeners) l.run(log, a, b, c, d);
		};
		fuzz(VoidCb.class, () -> EventFactory.create(VoidCb.class), reference,
				(i, rng) -> (log, a, b, c, d) -> log.add(i + ":" + a + b + c + d),
				(inv, log) -> {
					inv.run(log, 7, 8L, 9.5, "x");
					return null;
				});
	}

	private static Res randomRes(int i, Random rng, Res pass) {
		int r = rng.nextInt(10);
		if (r < 6) return pass;
		return r < 8 ? null : new Res(i);
	}

	@Test
	void shortCircuitReference() {
		Function<RefCb[], RefCb> reference = listeners -> (log, b, c) -> {
			for (RefCb l : listeners) {
				Res result = l.run(log, b, c);
				if (result != PASS) return result;
			}

			return PASS;
		};
		fuzz(RefCb.class, () -> EventFactory.createArrayBacked(RefCb.class, CombinedInvokerFactory.create(RefCb.class, Mode.SHORT_CIRCUIT, PASS, -1)), reference,
				(i, rng) -> {
					Res result = randomRes(i, rng, PASS);
					return (log, b, c) -> {
						log.add(i + ":" + b + c);
						return result;
					};
				},
				(inv, log) -> inv.run(log, 5L, 1.5));
	}

	@Test
	void shortCircuitNull() {
		Function<RefCb[], RefCb> reference = listeners -> (log, b, c) -> {
			for (RefCb l : listeners) {
				Res result = l.run(log, b, c);
				if (result != null) return result;
			}

			return null;
		};
		fuzz(RefCb.class, () -> EventFactory.create(RefCb.class, Behavior.FIRST_NON_NULL), reference,
				(i, rng) -> {
					Res result = rng.nextInt(10) < 7 ? null : new Res(i);
					return (log, b, c) -> {
						log.add(i + ":" + b + c);
						return result;
					};
				},
				(inv, log) -> inv.run(log, 5L, 1.5));
	}

	@Test
	void shortCircuitBooleanStopAtFalse() {
		Function<BoolCb[], BoolCb> reference = listeners -> (log, b, c) -> {
			for (BoolCb l : listeners) {
				if (!l.run(log, b, c)) return false;
			}

			return true;
		};
		fuzz(BoolCb.class, () -> EventFactory.create(BoolCb.class, Behavior.STOP_ON_FALSE), reference,
				(i, rng) -> {
					boolean result = rng.nextInt(10) < 8;
					return (log, b, c) -> {
						log.add(i + ":" + b + c);
						return result;
					};
				},
				(inv, log) -> inv.run(log, 5L, 1.5));
	}

	@Test
	void shortCircuitBooleanStopAtTrue() {
		Function<BoolCb[], BoolCb> reference = listeners -> (log, b, c) -> {
			for (BoolCb l : listeners) {
				if (l.run(log, b, c)) return true;
			}

			return false;
		};
		fuzz(BoolCb.class, () -> EventFactory.create(BoolCb.class, Behavior.STOP_ON_TRUE), reference,
				(i, rng) -> {
					boolean result = rng.nextInt(10) < 2;
					return (log, b, c) -> {
						log.add(i + ":" + b + c);
						return result;
					};
				},
				(inv, log) -> inv.run(log, 5L, 1.5));
	}

	@Test
	void allMatch() {
		Function<BoolCb[], BoolCb> reference = listeners -> (log, b, c) -> {
			boolean allow = true;

			for (BoolCb l : listeners) {
				allow &= l.run(log, b, c);
			}

			return allow;
		};
		fuzz(BoolCb.class, () -> EventFactory.create(BoolCb.class, Behavior.ALL_TRUE), reference,
				(i, rng) -> {
					boolean result = rng.nextInt(10) < 8;
					return (log, b, c) -> {
						log.add(i + ":" + b + c);
						return result;
					};
				},
				(inv, log) -> inv.run(log, 5L, 1.5));
	}

	@Test
	void anyMatch() {
		Function<BoolCb[], BoolCb> reference = listeners -> (log, b, c) -> {
			boolean result = false;

			for (BoolCb l : listeners) {
				result |= l.run(log, b, c);
			}

			return result;
		};
		fuzz(BoolCb.class, () -> EventFactory.create(BoolCb.class, Behavior.ANY_TRUE), reference,
				(i, rng) -> {
					boolean result = rng.nextInt(10) < 2;
					return (log, b, c) -> {
						log.add(i + ":" + b + c);
						return result;
					};
				},
				(inv, log) -> inv.run(log, 5L, 1.5));
	}

	@Test
	void chainReference() {
		Function<ChainStr[], ChainStr> reference = listeners -> (log, a, s, d) -> {
			for (ChainStr l : listeners) {
				s = l.run(log, a, s, d);
			}

			return s;
		};
		fuzz(ChainStr.class, () -> EventFactory.createArrayBacked(ChainStr.class, CombinedInvokerFactory.create(ChainStr.class, Mode.CHAIN, null, 2)), reference,
				(i, rng) -> (log, a, s, d) -> {
					log.add(i + ":" + a + s + d);
					return s + "|" + i;
				},
				(inv, log) -> inv.run(log, 3L, "start", 2.5));
	}

	@Test
	void chainPrimitiveAfterWideParameters() {
		// The threaded parameter comes after an int and a double, so its slot is not simply its index.
		Function<ChainLong[], ChainLong> reference = listeners -> (log, a, b, v) -> {
			for (ChainLong l : listeners) {
				v = l.run(log, a, b, v);
			}

			return v;
		};
		fuzz(ChainLong.class, () -> EventFactory.createArrayBacked(ChainLong.class, CombinedInvokerFactory.create(ChainLong.class, Mode.CHAIN, null, 3)), reference,
				(i, rng) -> (log, a, b, v) -> {
					log.add(i + ":" + a + b + v);
					return v * 31 + i;
				},
				(inv, log) -> inv.run(log, 3, 2.5, 100L));
	}

	@Test
	@SuppressWarnings({"unchecked", "rawtypes"})
	void invalidCombinationsAreRejected() {
		// Behavior that does not fit the return type: rejected when the event is created.
		assertThrows(IllegalArgumentException.class, () -> EventFactory.create(VoidCb.class, Behavior.FIRST_NON_NULL));
		assertThrows(IllegalArgumentException.class, () -> EventFactory.create(RefCb.class, Behavior.ALL_TRUE));
		assertThrows(IllegalArgumentException.class, () -> EventFactory.create(VoidCb.class, Behavior.ANY_TRUE));
		assertThrows(IllegalArgumentException.class, () -> EventFactory.create(BoolCb.class));
		assertThrows(IllegalArgumentException.class, () -> EventFactory.create(BoolCb.class, Behavior.FIRST_NON_NULL));
		assertThrows(IllegalArgumentException.class, () -> EventFactory.create(RefCb.class, Behavior.STOP_ON_FALSE));
		// Pass value of the wrong type (not exposed, generator only).
		assertThrows(IllegalArgumentException.class, () -> CombinedInvokerFactory.create(RefCb.class, Mode.SHORT_CIRCUIT, "not a Res", -1));
		// Threaded parameter must exist and have the type of the result (not exposed, generator only).
		assertThrows(IllegalArgumentException.class, () -> CombinedInvokerFactory.create(ChainStr.class, Mode.CHAIN, null, 1));
		assertThrows(IllegalArgumentException.class, () -> CombinedInvokerFactory.create(ChainStr.class, Mode.CHAIN, null, 9));
		assertThrows(IllegalArgumentException.class, () -> CombinedInvokerFactory.create(ChainStr.class, Mode.CHAIN, null, -1));
		// Not a single-method interface.
		assertThrows(IllegalArgumentException.class, () -> EventFactory.create(Object.class));
		// Not accessible: java.base does not open java.lang.
		assertThrows(IllegalArgumentException.class, () -> EventFactory.create(Runnable.class));
		// A generated invoker factory for another listener type than the event.
		assertThrows(IllegalArgumentException.class, () -> EventFactory.createArrayBacked((Class) BoolCb.class,
				(Function) CombinedInvokerFactory.create(VoidCb.class, Mode.VOID, null, -1)));
	}
}
