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

package net.fabricmc.fabric.impl.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;

import net.fabricmc.fabric.api.scheduler.v1.ScheduledTask;
import net.fabricmc.fabric.api.scheduler.v1.ServerScheduler;

public final class ServerSchedulerImpl implements ServerScheduler {
	private final Object lock = new Object();
	private final PriorityQueue<TaskImpl> queue = new PriorityQueue<>();
	private long currentTick = 0;
	private long nextSequence = 0;

	@Override
	public ScheduledTask schedule(Runnable action, long delayTicks) {
		return this.add(action, delayTicks, 0);
	}

	@Override
	public ScheduledTask scheduleRepeating(Runnable action, long delayTicks, long periodTicks) {
		if (periodTicks <= 0) throw new IllegalArgumentException("periodTicks must be positive, got " + periodTicks);

		return this.add(action, delayTicks, periodTicks);
	}

	/**
	 * Runs all due tasks. Called once at the beginning of every server tick, on the server thread.
	 */
	public void tick() {
		List<TaskImpl> due = null;

		synchronized (this.lock) {
			this.currentTick++;

			// Snapshot the due tasks before running any, so that a task scheduled with no
			// delay from within another task runs on the next tick, never on the current one.
			while (!this.queue.isEmpty() && this.queue.peek().dueTick <= this.currentTick) {
				TaskImpl task = this.queue.poll();

				if (!task.isCancelled()) {
					if (due == null) due = new ArrayList<>();

					due.add(task);
				}
			}
		}

		if (due == null) return;

		for (TaskImpl task : due) {
			// A task that ran earlier this tick may have cancelled a later one.
			if (task.isCancelled()) continue;

			task.action.run();

			if (task.periodTicks > 0 && !task.isCancelled()) {
				synchronized (this.lock) {
					task.dueTick = this.currentTick + task.periodTicks;
					this.queue.add(task);
				}
			}
		}
	}

	private ScheduledTask add(Runnable action, long delayTicks, long periodTicks) {
		Objects.requireNonNull(action, "action can't be null!");

		if (delayTicks < 0) throw new IllegalArgumentException("delayTicks can't be negative, got " + delayTicks);

		synchronized (this.lock) {
			TaskImpl task = new TaskImpl(action, this.currentTick + delayTicks, periodTicks, this.nextSequence++);
			this.queue.add(task);
			return task;
		}
	}

	private static final class TaskImpl implements ScheduledTask, Comparable<TaskImpl> {
		final Runnable action;
		final long periodTicks;
		final long sequence;
		long dueTick;
		private volatile boolean cancelled;

		TaskImpl(Runnable action, long dueTick, long periodTicks, long sequence) {
			this.action = action;
			this.dueTick = dueTick;
			this.periodTicks = periodTicks;
			this.sequence = sequence;
		}

		@Override
		public void cancel() {
			this.cancelled = true;
		}

		@Override
		public boolean isCancelled() {
			return this.cancelled;
		}

		@Override
		public int compareTo(TaskImpl other) {
			int result = Long.compare(this.dueTick, other.dueTick);

			return result != 0 ? result : Long.compare(this.sequence, other.sequence);
		}
	}
}
