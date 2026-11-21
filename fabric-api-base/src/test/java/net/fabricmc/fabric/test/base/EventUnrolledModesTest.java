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
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.EventFactory.Behavior;
import net.fabricmc.fabric.api.event.EventFactory.ExceptionHandling;
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
	private static <T> void fuzz(Class<T> type, Supplier<Event<T>> newEvent, Function<T[], T> reference,
			BiFunction<Integer, Random, T> make, BiFunction<T, List<String>, Object> fire) {
		fuzz(type, newEvent, reference, make, fire, false, COUNTS);
	}

	@SuppressWarnings("unchecked")
	private static <T> void fuzz(Class<T> type, Supplier<Event<T>> newEvent, Function<T[], T> reference,
			BiFunction<Integer, Random, T> make, BiFunction<T, List<String>, Object> fire, boolean catching, int[] counts) {
		for (int n : counts) {
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

				if (n == 1 && !catching) {
					assertSame(listeners[0], EventFactoryImpl.underlyingInvoker(event), context + " single listener");
				} else if (n == 0) {
					assertTrue(name.contains("FabricEvent$Invoker"), context + " " + name);
				} else if (n <= EventFactoryImpl.MAX_UNROLLED_LISTENERS) {
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
		Object expected = outcome(expectedInvoker, expectedLog, fire);
		Object actual = outcome(actualInvoker, actualLog, fire);
		assertEquals(expected, actual, context + " result");
		assertEquals(expectedLog, actualLog, context + " calls");
	}

	/**
	 * The result of firing the invoker, or the listener failure it threw, with the ones it suppressed.
	 */
	private static <T> Object outcome(T invoker, List<String> log, BiFunction<T, List<String>, Object> fire) {
		try {
			return fire.apply(invoker, log);
		} catch (ListenerFailure e) {
			StringBuilder thrown = new StringBuilder("threw ").append(e.getMessage());

			for (Throwable suppressed : e.getSuppressed()) {
				thrown.append(" + ").append(suppressed.getMessage());
			}

			return thrown.toString();
		}
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

	// ---- Exception handling: every mode against a reference loop that wraps each listener in try/catch.

	/**
	 * Fewer counts than {@link #COUNTS}, failures may be logged.
	 */
	private static final int[] CATCHING_COUNTS = {0, 1, 2, 3, 5, 8, 17, 128, 129};
	private static final ExceptionHandling[] HANDLING = {ExceptionHandling.SUPPRESS, ExceptionHandling.SUPPRESS_AND_LOG, ExceptionHandling.ADD_SUPPRESSED};

	/**
	 * Thrown by a failing listener. Without a stack trace, to keep the log of the test short.
	 */
	private static final class ListenerFailure extends RuntimeException {
		ListenerFailure(int i) {
			super("listener " + i, null, true, false);
		}
	}

	private static boolean fails(Random rng) {
		return rng.nextInt(8) == 0;
	}

	/**
	 * What the reference loops do with a failure: keep the first one, with the later ones suppressed.
	 */
	private static Exception collect(Exception pending, Exception e) {
		if (pending == null) return e;
		pending.addSuppressed(e);
		return pending;
	}

	/**
	 * What the reference loops do before returning: rethrow the failures with {@link ExceptionHandling#ADD_SUPPRESSED}.
	 */
	private static <R> R finish(ExceptionHandling handling, Exception pending, R result) {
		if (handling == ExceptionHandling.ADD_SUPPRESSED && pending != null) throw (RuntimeException) pending;
		return result;
	}

	private static BoolCb failingBool(int i, Random rng, int trueOutOfTen) {
		boolean result = rng.nextInt(10) < trueOutOfTen;
		boolean fails = fails(rng);
		return (log, b, c) -> {
			log.add(i + ":" + b + c);
			if (fails) throw new ListenerFailure(i);
			return result;
		};
	}

	private static RefCb failingRef(int i, Random rng, Res pass) {
		Res result = randomRes(i, rng, pass);
		boolean fails = fails(rng);
		return (log, b, c) -> {
			log.add(i + ":" + b + c);
			if (fails) throw new ListenerFailure(i);
			return result;
		};
	}

	@Test
	void handlingVoid() {
		for (ExceptionHandling handling : HANDLING) {
			Function<VoidCb[], VoidCb> reference = listeners -> (log, a, b, c, d) -> {
				Exception pending = null;

				for (VoidCb l : listeners) {
					try {
						l.run(log, a, b, c, d);
					} catch (Exception e) {
						pending = collect(pending, e);
					}
				}

				finish(handling, pending, null);
			};
			fuzz(VoidCb.class, () -> EventFactory.create(VoidCb.class, Behavior.INVOKE_ALL, handling), reference,
					(i, rng) -> {
						boolean fails = fails(rng);
						return (log, a, b, c, d) -> {
							log.add(i + ":" + a + b + c + d);
							if (fails) throw new ListenerFailure(i);
						};
					},
					(inv, log) -> {
						inv.run(log, 7, 8L, 9.5, "x");
						return null;
					}, true, CATCHING_COUNTS);
		}
	}

	@Test
	void handlingStopOnFalseAndTrue() {
		for (ExceptionHandling handling : HANDLING) {
			for (boolean pass : new boolean[] {true, false}) {
				Function<BoolCb[], BoolCb> reference = listeners -> (log, b, c) -> {
					Exception pending = null;

					for (BoolCb l : listeners) {
						boolean result;

						try {
							result = l.run(log, b, c);
						} catch (Exception e) {
							pending = collect(pending, e);
							continue;
						}

						if (result != pass) return finish(handling, pending, result);
					}

					return finish(handling, pending, pass);
				};
				Behavior behavior = pass ? Behavior.STOP_ON_FALSE : Behavior.STOP_ON_TRUE;
				fuzz(BoolCb.class, () -> EventFactory.create(BoolCb.class, behavior, handling), reference,
						(i, rng) -> failingBool(i, rng, pass ? 8 : 2),
						(inv, log) -> inv.run(log, 5L, 1.5), true, CATCHING_COUNTS);
			}
		}
	}

	@Test
	void handlingAllAndAnyTrue() {
		for (ExceptionHandling handling : HANDLING) {
			for (boolean all : new boolean[] {true, false}) {
				Function<BoolCb[], BoolCb> reference = listeners -> (log, b, c) -> {
					Exception pending = null;
					boolean result = all;

					for (BoolCb l : listeners) {
						try {
							boolean returned = l.run(log, b, c);
							result = all ? result & returned : result | returned;
						} catch (Exception e) {
							pending = collect(pending, e);
						}
					}

					return finish(handling, pending, result);
				};
				Behavior behavior = all ? Behavior.ALL_TRUE : Behavior.ANY_TRUE;
				fuzz(BoolCb.class, () -> EventFactory.create(BoolCb.class, behavior, handling), reference,
						(i, rng) -> failingBool(i, rng, all ? 8 : 2),
						(inv, log) -> inv.run(log, 5L, 1.5), true, CATCHING_COUNTS);
			}
		}
	}

	@Test
	void handlingFirstNonNullAndPassValue() {
		for (ExceptionHandling handling : HANDLING) {
			for (Res pass : new Res[] {null, PASS}) {
				Function<RefCb[], RefCb> reference = listeners -> (log, b, c) -> {
					Exception pending = null;

					for (RefCb l : listeners) {
						Res result;

						try {
							result = l.run(log, b, c);
						} catch (Exception e) {
							pending = collect(pending, e);
							continue;
						}

						if (result != pass) return finish(handling, pending, result);
					}

					return finish(handling, pending, pass);
				};
				fuzz(RefCb.class, () -> EventFactory.createArrayBacked(RefCb.class, CombinedInvokerFactory.create(RefCb.class, Mode.SHORT_CIRCUIT, pass, -1, handling)), reference,
						(i, rng) -> failingRef(i, rng, pass),
						(inv, log) -> inv.run(log, 5L, 1.5), true, CATCHING_COUNTS);
			}
		}
	}

	@Test
	void handlingChainKeepsTheValue() {
		// Like the model modifiers of the model loading API: a failing listener leaves the value unchanged.
		for (ExceptionHandling handling : HANDLING) {
			Function<ChainStr[], ChainStr> reference = listeners -> (log, a, s, d) -> {
				Exception pending = null;

				for (ChainStr l : listeners) {
					try {
						s = l.run(log, a, s, d);
					} catch (Exception e) {
						pending = collect(pending, e);
					}
				}

				return finish(handling, pending, s);
			};
			fuzz(ChainStr.class, () -> EventFactory.createArrayBacked(ChainStr.class, CombinedInvokerFactory.create(ChainStr.class, Mode.CHAIN, null, 2, handling)), reference,
					(i, rng) -> {
						boolean fails = fails(rng);
						return (log, a, s, d) -> {
							log.add(i + ":" + a + s + d);
							if (fails) throw new ListenerFailure(i);
							return s + "|" + i;
						};
					},
					(inv, log) -> inv.run(log, 3L, "start", 2.5), true, CATCHING_COUNTS);
		}
	}

	@Test
	void handlingDoesNotCatchErrors() {
		for (ExceptionHandling handling : ExceptionHandling.values()) {
			Event<VoidCb> event = EventFactory.create(VoidCb.class, Behavior.INVOKE_ALL, handling);
			event.register((log, a, b, c, d) -> log.add("first"));
			event.register((log, a, b, c, d) -> {
				throw new AssertionError("not an exception");
			});
			event.register((log, a, b, c, d) -> log.add("third"));
			List<String> log = new ArrayList<>();
			assertThrows(AssertionError.class, () -> event.invoker().run(log, 1, 2L, 3.0, "x"), handling.name());
			EventFactoryImpl.awaitInvokerGeneration();
			assertThrows(AssertionError.class, () -> event.invoker().run(log, 1, 2L, 3.0, "x"), handling.name());
			assertEquals(List.of("first", "first"), log, handling.name());
		}
	}

	@Test
	void propagateStopsAtTheException() {
		Event<VoidCb> event = EventFactory.create(VoidCb.class, Behavior.INVOKE_ALL, ExceptionHandling.PROPAGATE);
		event.register((log, a, b, c, d) -> log.add("first"));
		event.register((log, a, b, c, d) -> {
			throw new ListenerFailure(1);
		});
		event.register((log, a, b, c, d) -> log.add("third"));
		EventFactoryImpl.awaitInvokerGeneration();
		List<String> log = new ArrayList<>();
		assertThrows(ListenerFailure.class, () -> event.invoker().run(log, 1, 2L, 3.0, "x"));
		assertEquals(List.of("first"), log);
	}

	@Test
	void addSuppressedRethrowsCheckedExceptions() throws Exception {
		// Checked exceptions are a Java language rule, a listener in another language can throw one without declaring it.
		Exception checked = new java.io.IOException("checked");
		Event<VoidCb> event = EventFactory.create(VoidCb.class, Behavior.INVOKE_ALL, ExceptionHandling.ADD_SUPPRESSED);
		event.register((log, a, b, c, d) -> sneakyThrow(checked));
		event.register((log, a, b, c, d) -> log.add("second"));
		EventFactoryImpl.awaitInvokerGeneration();
		List<String> log = new ArrayList<>();
		Exception thrown = assertThrows(Exception.class, () -> event.invoker().run(log, 1, 2L, 3.0, "x"));
		assertSame(checked, thrown);
		assertEquals(List.of("second"), log);
	}

	@Test
	void addSuppressedWithTheSameExceptionTwice() {
		// Like the preallocated exceptions of the JIT: an exception cannot suppress itself.
		ListenerFailure failure = new ListenerFailure(0);
		Event<VoidCb> event = EventFactory.create(VoidCb.class, Behavior.INVOKE_ALL, ExceptionHandling.ADD_SUPPRESSED);

		for (int i = 0; i < 3; i++) {
			event.register((log, a, b, c, d) -> {
				throw failure;
			});
		}

		assertSame(failure, assertThrows(ListenerFailure.class, () -> event.invoker().run(new ArrayList<>(), 1, 2L, 3.0, "x")));
		EventFactoryImpl.awaitInvokerGeneration();
		assertSame(failure, assertThrows(ListenerFailure.class, () -> event.invoker().run(new ArrayList<>(), 1, 2L, 3.0, "x")));
		assertEquals(0, failure.getSuppressed().length);
	}

	public interface RaceCb {
		boolean run(List<String> log);
	}

	@Test
	void concurrentEventsShareOneLoopClass() throws Exception {
		// Every behavior and exception handling has its own loop class, generated by the first event that needs it.
		// Events created at the same time must all use the same one: registering again replaces the listeners of the loop
		// through the setter of the cached class, which must be the class the loop was created from.
		int threads = 8;
		ExecutorService pool = Executors.newFixedThreadPool(threads);

		try {
			for (Behavior behavior : new Behavior[] {Behavior.STOP_ON_FALSE, Behavior.STOP_ON_TRUE, Behavior.ALL_TRUE, Behavior.ANY_TRUE}) {
				for (ExceptionHandling handling : ExceptionHandling.values()) {
					CyclicBarrier barrier = new CyclicBarrier(threads);
					List<Future<Event<RaceCb>>> events = new ArrayList<>();

					for (int t = 0; t < threads; t++) {
						events.add(pool.submit(() -> {
							Event<RaceCb> event = EventFactory.create(RaceCb.class, behavior, handling);
							event.register(log -> log.add("a"));
							barrier.await();
							event.register(log -> log.add("b")); // creates the loop, from all threads at once
							return event;
						}));
					}

					for (Future<Event<RaceCb>> future : events) {
						Event<RaceCb> event = future.get();
						event.register(log -> log.add("c")); // replaces the listeners of the loop
						event.invoker().run(new ArrayList<>());
					}
				}
			}
		} finally {
			pool.shutdownNow();
		}
	}

	public interface ManyParams {
		void run(List<String> log, long a, long b, long c, long d, long e, long f, long g, long h, long i, long j, long k, long l, long m, long n, long o, long p);
	}

	@Test
	void unrolledInvokerTooLargeForTheJitKeepsTheLoop() {
		// 16 long parameters and exception handlers: 128 unrolled listeners would be over the 8000 bytes of bytecode the JIT compiles.
		for (int count : new int[] {64, 128}) {
			Event<ManyParams> event = EventFactory.create(ManyParams.class, Behavior.INVOKE_ALL, ExceptionHandling.ADD_SUPPRESSED);

			for (int x = 0; x < count; x++) {
				String id = Integer.toString(x);
				event.register((log, a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p) -> log.add(id));
			}

			EventFactoryImpl.awaitInvokerGeneration();
			String name = EventFactoryImpl.underlyingInvoker(event).getClass().getName();
			assertTrue(name.contains(count == 64 ? "FabricEvent$Unrolled64" : "FabricEvent$Loop"), count + " listeners: " + name);

			List<String> log = new ArrayList<>();
			event.invoker().run(log, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
			assertEquals(count, log.size());
		}
	}

	@SuppressWarnings("unchecked")
	private static <E extends Throwable> void sneakyThrow(Throwable t) throws E {
		throw (E) t;
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
