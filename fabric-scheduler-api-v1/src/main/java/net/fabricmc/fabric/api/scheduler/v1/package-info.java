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

/**
 * The Scheduler API, for running tasks on the server thread after a delay
 * measured in server ticks.
 *
 * <p>Without this API, mods that need "run this in n ticks" or "run this every
 * n ticks" each maintain their own counter inside a
 * {@link net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents} listener,
 * or bounce work through an executor and back onto the server thread. This
 * module provides that primitive once, with well-defined ordering, cancellation
 * and lifecycle semantics.
 *
 * <p>Obtain a scheduler with
 * {@link net.fabricmc.fabric.api.scheduler.v1.ServerScheduler#get(net.minecraft.server.MinecraftServer)},
 * then schedule one-shot tasks with
 * {@link net.fabricmc.fabric.api.scheduler.v1.ServerScheduler#schedule(Runnable, long)}
 * or repeating tasks with
 * {@link net.fabricmc.fabric.api.scheduler.v1.ServerScheduler#scheduleRepeating(Runnable, long, long)}.
 * Both return a {@link net.fabricmc.fabric.api.scheduler.v1.ScheduledTask} handle
 * that can be cancelled from any thread.
 *
 * <p>Scheduling is thread-safe, which makes the scheduler a convenient bridge for
 * completing asynchronous work on the server thread:
 * <pre>{@code
 * CompletableFuture.supplyAsync(database::loadProfile)
 *     .thenAccept(profile -> ServerScheduler.get(server).schedule(() -> apply(profile), 0));
 * }</pre>
 */
@ApiStatus.Experimental
@NullMarked
package net.fabricmc.fabric.api.scheduler.v1;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
