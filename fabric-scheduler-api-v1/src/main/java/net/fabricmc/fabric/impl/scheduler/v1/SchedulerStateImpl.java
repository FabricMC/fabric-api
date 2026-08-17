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

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.ExtraCodecs;

import net.fabricmc.fabric.api.scheduler.v1.SchedulerState;

public record SchedulerStateImpl(String stateId, int ticks) implements SchedulerState {
	public static final Codec<SchedulerState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("state_id").forGetter(state -> ((SchedulerStateImpl) state).stateId()),
			ExtraCodecs.NON_NEGATIVE_INT.fieldOf("ticks").forGetter(state -> ((SchedulerStateImpl) state).ticks())
	).apply(instance, SchedulerStateImpl::new));
	public static final SchedulerStateImpl START = new SchedulerStateImpl("fabric:start", 0);
	public static final SchedulerStateImpl END = new SchedulerStateImpl("fabric:end", 0);

	public SchedulerStateImpl {
		Preconditions.checkArgument(ticks >= 0, "ticks must be non-negative");
	}
}
