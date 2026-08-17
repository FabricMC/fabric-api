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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.impl.scheduler.v1.node.ExitNode;
import net.fabricmc.fabric.impl.scheduler.v1.node.SchedulerNode;

public final class LoopInstruction implements SchedulerInstructionImpl {
	@Nullable
	private final String label;
	private final SchedulerInstructionImpl body;

	public LoopInstruction(@Nullable String label, SchedulerInstructionImpl body) {
		this.label = label;
		this.body = body;
	}

	@Override
	public SchedulerNode compile(SchedulerNode successor, Map<String, SchedulerNode> nodeByStateId, Map<@Nullable String, List<UnboundExitNode>> unboundExitNodes) {
		List<UnboundExitNode> exits = new ArrayList<>();
		@Nullable List<UnboundExitNode> prevExits = unboundExitNodes.put(label, exits);
		ExitNode loopbackNode = new ExitNode();
		SchedulerNode compiledBody = body.compile(loopbackNode, nodeByStateId, unboundExitNodes);
		loopbackNode.target = compiledBody;
		unboundExitNodes.put(label, prevExits);

		for (UnboundExitNode exit : exits) {
			if (exit.instruction() instanceof BreakInstruction) {
				exit.exitNode().target = successor;
			} else {
				exit.exitNode().target = compiledBody;
			}
		}

		return compiledBody;
	}
}
