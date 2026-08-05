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

package net.fabricmc.fabric.impl.scheduler.v1;

import java.util.HashMap;
import java.util.Map;

import com.mojang.datafixers.util.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.scheduler.v1.Scheduler;
import net.fabricmc.fabric.api.scheduler.v1.SchedulerState;
import net.fabricmc.fabric.impl.scheduler.v1.instruction.SchedulerInstructionImpl;
import net.fabricmc.fabric.impl.scheduler.v1.node.EndNode;
import net.fabricmc.fabric.impl.scheduler.v1.node.SchedulerNode;

public final class SchedulerImpl implements Scheduler {
	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-scheduler-api-v1");
	private SchedulerStateImpl state = SchedulerStateImpl.END;
	private final Map<String, SchedulerNode> nodeByStateId = new HashMap<>();

	public SchedulerImpl(SchedulerInstructionImpl instruction) {
		SchedulerNode startNode = instruction.compile(EndNode.INSTANCE, nodeByStateId, new HashMap<>());

		if (nodeByStateId.containsKey(SchedulerStateImpl.START.stateId())) {
			throw new IllegalStateException("Instructions contain explicit start node");
		}

		if (nodeByStateId.containsKey(SchedulerStateImpl.END.stateId())) {
			throw new IllegalStateException("Instructions contain explicit end node");
		}

		nodeByStateId.put(SchedulerStateImpl.START.stateId(), startNode);
		nodeByStateId.put(SchedulerStateImpl.END.stateId(), EndNode.INSTANCE);
	}

	@Override
	public void run() {
		state = SchedulerStateImpl.START;
	}

	@Override
	public boolean isRunning() {
		return !state.equals(SchedulerStateImpl.END);
	}

	@Override
	public void stop() {
		state = SchedulerStateImpl.END;
	}

	@Override
	public void tick() {
		SchedulerNode node = nodeByStateId.get(state.stateId());

		while (true) {
			Either<SchedulerNode, SchedulerStateImpl> result = node.execute(state);

			if (result.left().isPresent()) {
				node = result.left().get();
			} else {
				state = result.right().get();
				break;
			}
		}
	}

	@Override
	public SchedulerState getState() {
		return state;
	}

	@Override
	public void setState(SchedulerState state) {
		SchedulerStateImpl stateImpl = (SchedulerStateImpl) state;

		if (nodeByStateId.containsKey(stateImpl.stateId())) {
			this.state = stateImpl;
		} else {
			LOGGER.warn("Tried to load invalid scheduler state: {}", stateImpl.stateId());
			this.state = SchedulerStateImpl.END;
		}
	}
}
