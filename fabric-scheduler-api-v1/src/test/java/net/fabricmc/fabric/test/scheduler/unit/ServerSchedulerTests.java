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

package net.fabricmc.fabric.test.scheduler.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.fabricmc.fabric.api.scheduler.v1.ScheduledTask;
import net.fabricmc.fabric.impl.scheduler.ServerSchedulerImpl;

public class ServerSchedulerTests {
	private ServerSchedulerImpl scheduler;

	@BeforeEach
	void setUp() {
		scheduler = new ServerSchedulerImpl();
	}

	@Test
	void oneShotRunsAfterExactDelay() {
		List<Long> runTicks = new ArrayList<>();
		long[] tick = {0};

		scheduler.schedule(() -> runTicks.add(tick[0]), 3);

		for (tick[0] = 1; tick[0] <= 5; tick[0]++) {
			scheduler.tick();
		}

		assertEquals(List.of(3L), runTicks);
	}

	@Test
	void zeroDelayRunsOnNextTickOnly() {
		AtomicInteger runs = new AtomicInteger();

		scheduler.schedule(runs::incrementAndGet, 0);
		scheduler.tick();
		assertEquals(1, runs.get());

		scheduler.tick();
		assertEquals(1, runs.get());
	}

	@Test
	void tasksDueOnSameTickRunInSchedulingOrder() {
		List<String> order = new ArrayList<>();

		scheduler.schedule(() -> order.add("a"), 2);
		scheduler.schedule(() -> order.add("b"), 1);
		scheduler.schedule(() -> order.add("c"), 2);

		scheduler.tick();
		scheduler.tick();

		assertEquals(List.of("b", "a", "c"), order);
	}

	@Test
	void cancelledTaskNeverRuns() {
		AtomicInteger runs = new AtomicInteger();
		ScheduledTask task = scheduler.schedule(runs::incrementAndGet, 1);

		task.cancel();

		scheduler.tick();
		scheduler.tick();

		assertEquals(0, runs.get());
		assertTrue(task.isCancelled());
	}

	@Test
	void repeatingTaskRunsAtFixedPeriod() {
		List<Long> runTicks = new ArrayList<>();
		long[] tick = {0};

		scheduler.scheduleRepeating(() -> runTicks.add(tick[0]), 2, 3);

		for (tick[0] = 1; tick[0] <= 9; tick[0]++) {
			scheduler.tick();
		}

		assertEquals(List.of(2L, 5L, 8L), runTicks);
	}

	@Test
	void repeatingTaskCanCancelItself() {
		AtomicInteger runs = new AtomicInteger();
		ScheduledTask[] task = new ScheduledTask[1];

		task[0] = scheduler.scheduleRepeating(() -> {
			if (runs.incrementAndGet() == 2) task[0].cancel();
		}, 1, 1);

		for (int i = 0; i < 5; i++) {
			scheduler.tick();
		}

		assertEquals(2, runs.get());
	}

	@Test
	void taskScheduledFromWithinTaskRunsNextTick() {
		AtomicLong ranAt = new AtomicLong(-1);
		long[] tick = {0};

		scheduler.schedule(() -> scheduler.schedule(() -> ranAt.set(tick[0]), 0), 1);

		for (tick[0] = 1; tick[0] <= 3; tick[0]++) {
			scheduler.tick();
		}

		assertEquals(2, ranAt.get());
	}

	@Test
	void earlierTaskCanCancelLaterTaskOnSameTick() {
		AtomicInteger runs = new AtomicInteger();
		ScheduledTask[] later = new ScheduledTask[1];

		scheduler.schedule(() -> later[0].cancel(), 1);
		later[0] = scheduler.schedule(runs::incrementAndGet, 1);

		scheduler.tick();
		scheduler.tick();

		assertEquals(0, runs.get());
	}

	@Test
	void throwingRepeatingTaskIsNotRescheduled() {
		AtomicInteger runs = new AtomicInteger();

		scheduler.scheduleRepeating(() -> {
			runs.incrementAndGet();
			throw new IllegalStateException("boom");
		}, 1, 1);

		assertThrows(IllegalStateException.class, scheduler::tick);

		scheduler.tick();
		assertEquals(1, runs.get());
	}

	@Test
	void invalidArgumentsThrow() {
		Runnable action = () -> { };

		assertThrows(IllegalArgumentException.class, () -> scheduler.schedule(action, -1));
		assertThrows(IllegalArgumentException.class, () -> scheduler.scheduleRepeating(action, 0, 0));
		assertThrows(IllegalArgumentException.class, () -> scheduler.scheduleRepeating(action, 0, -5));
		assertThrows(NullPointerException.class, () -> scheduler.schedule(null, 0));
	}

	@Test
	void canScheduleFromAnotherThread() throws InterruptedException {
		AtomicInteger runs = new AtomicInteger();
		Thread thread = new Thread(() -> scheduler.schedule(runs::incrementAndGet, 0));

		thread.start();
		thread.join();

		scheduler.tick();
		assertEquals(1, runs.get());
	}

	@Test
	void cancellingAfterRunHasNoEffect() {
		AtomicInteger runs = new AtomicInteger();
		ScheduledTask task = scheduler.schedule(runs::incrementAndGet, 1);

		scheduler.tick();
		assertEquals(1, runs.get());
		assertFalse(task.isCancelled());

		task.cancel();
		scheduler.tick();
		assertEquals(1, runs.get());
	}
}
