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

package net.fabricmc.fabric.impl.base.event;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import com.google.common.collect.MapMaker;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.event.Event;

public final class EventFactoryImpl {
	private static final Set<ArrayBackedEvent<?>> ARRAY_BACKED_EVENTS
			= Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());

	/**
	 * The most listeners for which the invoker of an event made with {@link net.fabricmc.fabric.api.event.EventInvokers} is unrolled. Meant for tests.
	 */
	public static final int MAX_UNROLLED_LISTENERS = CombinedInvokerFactory.MAX_UNROLLED;

	private EventFactoryImpl() { }

	public static void invalidate() {
		ARRAY_BACKED_EVENTS.forEach(ArrayBackedEvent::update);
	}

	public static <T> Event<T> createArrayBacked(Class<? super T> type, Function<T[], T> invokerFactory) {
		ArrayBackedEvent<T> event;

		if (invokerFactory instanceof CombinedInvokerFactory<T> combined) {
			if (combined.type != type) {
				throw new IllegalArgumentException("The invoker factory was created for " + combined.type.getName() + ", but the event is for " + type.getName());
			}

			event = StableInvoker.createEvent(type, combined);
		} else {
			event = new ArrayBackedEvent<>(type, invokerFactory);
		}

		ARRAY_BACKED_EVENTS.add(event);
		return event;
	}

	/**
	 * The invoker that is currently installed in an event of Fabric API, which is not the one that {@code invoker()} returns
	 * if the event has a stable invoker. Meant for tests.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T underlyingInvoker(Event<T> event) {
		return ((ArrayBackedEvent<T>) event).currentInvoker();
	}

	/**
	 * How many times the call site of an event with a stable invoker got a new target. Meant for tests.
	 */
	public static int retargetCount(Event<?> event) {
		StableInvoker<?> stable = ((ArrayBackedEvent<?>) event).stable();
		return stable == null ? 0 : stable.retargets;
	}

	/**
	 * Holds back the generation of unrolled invokers until the returned action is run. Meant for tests.
	 */
	public static Runnable blockInvokerGeneration() {
		CountDownLatch latch = new CountDownLatch(1);

		ArrayBackedEvent.UPGRADES.execute(() -> {
			try {
				latch.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});

		return latch::countDown;
	}

	/**
	 * Waits until all pending generated invokers have been installed. Meant for tests and benchmarks.
	 */
	public static void awaitInvokerGeneration() {
		try {
			ArrayBackedEvent.UPGRADES.submit(() -> { }).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Registers the ordering of the default phases of a freshly created event, with a single sort.
	 */
	public static <T> void addDefaultPhases(Event<T> event, Identifier[] defaultPhases) {
		if (event instanceof ArrayBackedEvent<T> arrayBacked) {
			arrayBacked.addPhaseOrderings(defaultPhases);
		} else {
			for (int i = 1; i < defaultPhases.length; ++i) {
				event.addPhaseOrdering(defaultPhases[i - 1], defaultPhases[i]);
			}
		}
	}

	public static void ensureContainsDefault(Identifier[] defaultPhases) {
		for (Identifier id : defaultPhases) {
			if (id.equals(Event.DEFAULT_PHASE)) {
				return;
			}
		}

		throw new IllegalArgumentException("The event phases must contain Event.DEFAULT_PHASE.");
	}

	public static void ensureNoDuplicates(Identifier[] defaultPhases) {
		for (int i = 0; i < defaultPhases.length; ++i) {
			for (int j = i+1; j < defaultPhases.length; ++j) {
				if (defaultPhases[i].equals(defaultPhases[j])) {
					throw new IllegalArgumentException("Duplicate event phase: " + defaultPhases[i]);
				}
			}
		}
	}
}
