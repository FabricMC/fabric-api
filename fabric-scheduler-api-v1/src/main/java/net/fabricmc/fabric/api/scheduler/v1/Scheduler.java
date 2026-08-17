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

import java.util.Arrays;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

import org.jetbrains.annotations.ApiStatus;

import net.fabricmc.fabric.impl.scheduler.v1.SchedulerImpl;
import net.fabricmc.fabric.impl.scheduler.v1.instruction.BreakInstruction;
import net.fabricmc.fabric.impl.scheduler.v1.instruction.ConditionInstruction;
import net.fabricmc.fabric.impl.scheduler.v1.instruction.ContinueInstruction;
import net.fabricmc.fabric.impl.scheduler.v1.instruction.DelayInstruction;
import net.fabricmc.fabric.impl.scheduler.v1.instruction.LoopInstruction;
import net.fabricmc.fabric.impl.scheduler.v1.instruction.LoopWhileInstruction;
import net.fabricmc.fabric.impl.scheduler.v1.instruction.RunInstruction;
import net.fabricmc.fabric.impl.scheduler.v1.instruction.SchedulerInstructionImpl;
import net.fabricmc.fabric.impl.scheduler.v1.instruction.SequenceInstruction;
import net.fabricmc.fabric.impl.scheduler.v1.instruction.WaitForInstruction;

@ApiStatus.NonExtendable
public interface Scheduler {
	static Scheduler createSequence(SchedulerInstruction... instructions) {
		return new SchedulerImpl((SchedulerInstructionImpl) sequence(instructions));
	}

	static Scheduler createLoop(SchedulerInstruction... instructions) {
		return new SchedulerImpl((SchedulerInstructionImpl) loop(instructions));
	}

	static SchedulerInstruction sequence(SchedulerInstruction... instructions) {
		return new SequenceInstruction(Arrays.copyOf(instructions, instructions.length, SchedulerInstructionImpl[].class));
	}

	static SchedulerInstruction condition(BooleanSupplier condition, SchedulerInstruction then) {
		return new ConditionInstruction(condition, (SchedulerInstructionImpl) then, null);
	}

	static SchedulerInstruction condition(BooleanSupplier condition, SchedulerInstruction then, SchedulerInstruction otherwise) {
		return new ConditionInstruction(condition, (SchedulerInstructionImpl) then, (SchedulerInstructionImpl) otherwise);
	}

	static SchedulerInstruction loop(SchedulerInstruction... instructions) {
		return new LoopInstruction(null, (SchedulerInstructionImpl) sequence(instructions));
	}

	static SchedulerInstruction loop(String label, SchedulerInstruction... instructions) {
		return new LoopInstruction(label, (SchedulerInstructionImpl) sequence(instructions));
	}

	static SchedulerInstruction loopWhile(BooleanSupplier condition, SchedulerInstruction body) {
		return new LoopWhileInstruction(null, condition, (SchedulerInstructionImpl) body);
	}

	static SchedulerInstruction loopWhile(String label, BooleanSupplier condition, SchedulerInstruction body) {
		return new LoopWhileInstruction(label, condition, (SchedulerInstructionImpl) body);
	}

	static SchedulerInstruction breakLoop() {
		return new BreakInstruction(null);
	}

	static SchedulerInstruction breakLoop(String label) {
		return new BreakInstruction(label);
	}

	static SchedulerInstruction continueLoop() {
		return new ContinueInstruction(null);
	}

	static SchedulerInstruction continueLoop(String label) {
		return new ContinueInstruction(label);
	}

	static SchedulerInstruction run(Runnable runnable) {
		return new RunInstruction(runnable);
	}

	static SchedulerInstruction delay(String stateId, int ticks) {
		return delay(stateId, () -> ticks);
	}

	static SchedulerInstruction delay(String stateId, IntSupplier ticks) {
		return new DelayInstruction(stateId, ticks);
	}

	static SchedulerInstruction waitFor(String stateId, BooleanSupplier condition) {
		return new WaitForInstruction(stateId, condition);
	}

	void run();

	boolean isRunning();

	void stop();

	void tick();

	SchedulerState getState();

	void setState(SchedulerState state);
}
