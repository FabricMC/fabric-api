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

package net.fabricmc.fabric.impl.scheduler.v1.node;

import java.util.function.BooleanSupplier;

import com.mojang.datafixers.util.Either;

import net.fabricmc.fabric.impl.scheduler.v1.SchedulerStateImpl;

public final class WaitForNode implements SchedulerNode {
	private final String stateId;
	private final BooleanSupplier condition;
	private final SchedulerNode next;

	public WaitForNode(String stateId, BooleanSupplier condition, SchedulerNode next) {
		this.stateId = stateId;
		this.condition = condition;
		this.next = next;
	}

	@Override
	public Either<SchedulerNode, SchedulerStateImpl> execute(SchedulerStateImpl state) {
		if (condition.getAsBoolean()) {
			return Either.left(next);
		} else {
			return Either.right(new SchedulerStateImpl(stateId, 0));
		}
	}
}
