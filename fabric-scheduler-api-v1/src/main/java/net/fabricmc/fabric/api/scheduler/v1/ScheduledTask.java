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

import org.jetbrains.annotations.ApiStatus;

/**
 * A handle to a task scheduled through a {@link ServerScheduler}.
 */
@ApiStatus.NonExtendable
public interface ScheduledTask {
	/**
	 * Cancels this task. A cancelled task never runs again; cancelling a task
	 * that already ran, or was already cancelled, has no effect.
	 *
	 * <p>May be called from any thread, including from within the task's own action.
	 * A repeating task that cancels itself while running is not rescheduled.
	 */
	void cancel();

	/**
	 * {@return whether this task has been cancelled}
	 */
	boolean isCancelled();
}
