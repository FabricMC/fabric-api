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

import org.junit.jupiter.api.Test;

import net.fabricmc.fabric.api.event.Combine;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.impl.base.event.EventFactoryImpl;

/**
 * Compares the generated invokers of every mode with the plain loops they replace.
 * The reference loops are written the way the events in Fabric API write them.
 */
public class EventUnrolledModesTest {
	private static final Random RNG = new Random(0xFAB1C);

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
	 * Runs the event with every kind of invoker against the reference loop, which is the specification of the {@link Combine}.
	 */
	private static <T> void fuzz(Class<T> type, Combine combine, Function<T[], T> reference,
			BiFunction<Integer, Random, T> make, BiFunction<T, List<String>, Object> fire) {
		for (boolean reflective : new boolean[] {false, true}) {
			EventFactoryImpl.setForceReflectiveInvokers(reflective);

			try {
				fuzz(type, combine, reference, make, fire, reflective);
			} finally {
				EventFactoryImpl.setForceReflectiveInvokers(false);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> void fuzz(Class<T> type, Combine combine, Function<T[], T> reference,
			BiFunction<Integer, Random, T> make, BiFunction<T, List<String>, Object> fire, boolean reflective) {
		for (int n = 0; n <= 20; n++) {
			for (int rep = 0; rep < 25; rep++) {
				T[] listeners = (T[]) Array.newInstance(type, n);
				Event<T> event = EventFactory.createArrayBacked(type, combine);

				for (int i = 0; i < n; i++) {
					listeners[i] = make.apply(i, RNG);
					event.register(listeners[i]);
				}

				String context = type.getSimpleName() + " n=" + n + " rep=" + rep + (reflective ? " reflective" : "");
				T expectedInvoker = reference.apply(listeners);

				// Right after registration, the invoker may be the loop or the unrolled one, depending on the background thread.
				compare(expectedInvoker, event.invoker(), fire, context + " (right after registration)");

				EventFactoryImpl.awaitInvokerGeneration();
				compare(expectedInvoker, event.invoker(), fire, context);

				String name = event.invoker().getClass().getName();

				if (n == 1) {
					assertSame(listeners[0], event.invoker(), context + " single listener");
				} else if (reflective) {
					assertTrue(name.contains("Proxy"), context + " " + name);
				} else if (n >= 2 && n <= 16) {
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
		fuzz(VoidCb.class, Combine.CALL_ALL, reference,
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
		fuzz(RefCb.class, Combine.firstOtherThan(PASS), reference,
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
		fuzz(RefCb.class, Combine.FIRST_NON_NULL, reference,
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
		fuzz(BoolCb.class, Combine.STOP_AT_FALSE, reference,
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
		fuzz(BoolCb.class, Combine.STOP_AT_TRUE, reference,
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
		fuzz(BoolCb.class, Combine.ALL_TRUE, reference,
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
	void chainReference() {
		Function<ChainStr[], ChainStr> reference = listeners -> (log, a, s, d) -> {
			for (ChainStr l : listeners) {
				s = l.run(log, a, s, d);
			}

			return s;
		};
		fuzz(ChainStr.class, Combine.chain(2), reference,
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
		fuzz(ChainLong.class, Combine.chain(3), reference,
				(i, rng) -> (log, a, b, v) -> {
					log.add(i + ":" + a + b + v);
					return v * 31 + i;
				},
				(inv, log) -> inv.run(log, 3, 2.5, 100L));
	}

	@Test
	void invalidCombinationsAreRejected() {
		// Wrong combination for the return type.
		assertThrows(IllegalArgumentException.class, () -> EventFactory.createArrayBacked(VoidCb.class, Combine.FIRST_NON_NULL));
		assertThrows(IllegalArgumentException.class, () -> EventFactory.createArrayBacked(RefCb.class, Combine.ALL_TRUE));
		assertThrows(IllegalArgumentException.class, () -> EventFactory.createArrayBacked(BoolCb.class, Combine.CALL_ALL));
		assertThrows(IllegalArgumentException.class, () -> EventFactory.createArrayBacked(BoolCb.class, Combine.FIRST_NON_NULL));
		// Pass value of the wrong type.
		assertThrows(IllegalArgumentException.class, () -> EventFactory.createArrayBacked(RefCb.class, Combine.firstOtherThan("not a Res")));
		// Threaded parameter must exist and have the type of the result.
		assertThrows(IllegalArgumentException.class, () -> EventFactory.createArrayBacked(ChainStr.class, Combine.chain(1)));
		assertThrows(IllegalArgumentException.class, () -> EventFactory.createArrayBacked(ChainStr.class, Combine.chain(9)));
		assertThrows(IllegalArgumentException.class, () -> Combine.chain(-1));
		// Not a single-method interface.
		assertThrows(IllegalArgumentException.class, () -> EventFactory.createArrayBacked(Runnable.class, Combine.FIRST_NON_NULL));
		assertThrows(IllegalArgumentException.class, () -> EventFactory.createArrayBacked(Object.class, Combine.CALL_ALL));
	}
}
