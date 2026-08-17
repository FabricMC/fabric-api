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
import java.util.function.BooleanSupplier;

import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.impl.scheduler.v1.node.SchedulerNode;
import net.fabricmc.fabric.impl.scheduler.v1.node.WaitForNode;

public final class WaitForInstruction implements SchedulerInstructionImpl {
	private final String stateId;
	private final BooleanSupplier condition;

	public WaitForInstruction(String stateId, BooleanSupplier condition) {
		this.stateId = stateId;
		this.condition = condition;
	}

	@Override
	public SchedulerNode compile(SchedulerNode successor, Map<String, SchedulerNode> nodeByStateId, Map<@Nullable String, List<UnboundExitNode>> unboundExitNodes) {
		WaitForNode node = new WaitForNode(stateId, condition, successor);

		if (nodeByStateId.put(stateId, node) != null) {
			throw new IllegalStateException("Duplicate state id: " + stateId);
		}

		return node;
	}
}
