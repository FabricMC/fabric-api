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

package net.fabricmc.fabric.impl.scheduler.v1.instruction;

import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.impl.scheduler.v1.node.RunNode;
import net.fabricmc.fabric.impl.scheduler.v1.node.SchedulerNode;

public final class RunInstruction implements SchedulerInstructionImpl {
	private final Runnable runnable;

	public RunInstruction(Runnable runnable) {
		this.runnable = runnable;
	}

	@Override
	public SchedulerNode compile(SchedulerNode successor, Map<String, SchedulerNode> nodeByStateId, Map<@Nullable String, List<UnboundExitNode>> unboundExitNodes) {
		return new RunNode(runnable, successor);
	}
}
