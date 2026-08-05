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

package net.fabricmc.fabric.api.scheduler.v1;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.server.MinecraftServer;

import net.fabricmc.fabric.impl.scheduler.ServerSchedulerHolder;

/**
 * A tick-based task scheduler bound to a {@link MinecraftServer}.
 *
 * <p>Scheduled tasks run on the server thread at the beginning of a server tick,
 * making this a safe way to defer work by a number of ticks or to hand work back
 * to the server thread from another thread. Scheduling methods may be called from
 * any thread; the returned {@link ScheduledTask} may also be cancelled from any thread.
 *
 * <p>Delays are measured in server ticks, not wall-clock time. When no ticks elapse —
 * for example while a dedicated server is paused because it is empty (see
 * {@code pause-when-empty-seconds} in {@code server.properties}) — pending tasks do
 * not advance towards execution.
 *
 * <p>The scheduler lives and dies with its server: tasks still pending when the
 * server stops are discarded. An exception thrown by a task propagates into the
 * server tick and will crash the server, like any other error raised during ticking.
 *
 * <p>Example:
 * <pre>{@code
 * ServerScheduler scheduler = ServerScheduler.get(server);
 *
 * // Run once, one second (20 ticks) from now.
 * scheduler.schedule(() -> player.sendSystemMessage(Component.literal("Welcome!")), 20);
 *
 * // Run every 5 seconds until cancelled.
 * ScheduledTask task = scheduler.scheduleRepeating(() -> broadcastTip(server), 100, 100);
 * }</pre>
 */
@ApiStatus.NonExtendable
public interface ServerScheduler {
	/**
	 * Gets the scheduler bound to the given server.
	 *
	 * @param server the server to schedule tasks on
	 * @return the server's scheduler
	 */
	static ServerScheduler get(MinecraftServer server) {
		Objects.requireNonNull(server, "server can't be null!");

		return ((ServerSchedulerHolder) server).fabric_getScheduler();
	}

	/**
	 * Schedules a task to run once on the server thread.
	 *
	 * <p>The task runs at the beginning of the server tick {@code delayTicks} ticks
	 * from now. A delay of {@code 0} or {@code 1} both run the task at the beginning
	 * of the next tick; a task can never run within the tick that scheduled it.
	 *
	 * @param action the action to run
	 * @param delayTicks the delay in server ticks, must not be negative
	 * @return a handle that can be used to cancel the task
	 */
	ScheduledTask schedule(Runnable action, long delayTicks);

	/**
	 * Schedules a task to run repeatedly on the server thread.
	 *
	 * <p>The task first runs at the beginning of the server tick {@code delayTicks}
	 * ticks from now (with the same semantics as {@link #schedule(Runnable, long)}),
	 * and then again every {@code periodTicks} ticks until cancelled. A repeating
	 * task that throws an exception is not rescheduled.
	 *
	 * @param action the action to run
	 * @param delayTicks the delay in server ticks before the first run, must not be negative
	 * @param periodTicks the number of ticks between runs, must be positive
	 * @return a handle that can be used to cancel the task
	 */
	ScheduledTask scheduleRepeating(Runnable action, long delayTicks, long periodTicks);
}
