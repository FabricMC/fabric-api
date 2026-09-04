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

package net.fabricmc.fabric.api.registry.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.FluidState;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Callback for when any fluid tries to flow or has a neighbor update.
 *
 * <p>The default behavior is to return {@code true}, which means that the fluid is allowed to flow.
 * <br/>By returning {@code false}, you are saying that you are preventing the fluid from flowing, canceling all further callbacks.
 *
 * <p>Note: Not updating the block state at the fluid position and returning {@code false} will likely cause unintended behavior, as fluids that could flow won't.
 *
 * <pre>{@code
 * FluidFlowCallback.EVENT.register((fluid, level, fluidPosition) -> {
 *     // For example, check if this is a specific fluid
 *     if (!fluid.is(Tags.MY_FLUID)) return true;
 *
 *     // For example, check if the block below matches some tag
 *     if (!level.getBlockState(fluidPosition.below()).is(Tags.MY_BLOCK)) return true;
 *
 *     // Perform some logic for fluid interaction ...
 *     return false;
 * });
 * }</pre>
 */
@FunctionalInterface
public interface AllowFluidFlow {
	Event<AllowFluidFlow> EVENT = EventFactory.createArrayBacked(AllowFluidFlow.class, fluidFlowInteractionEvents -> (fluid, level, fluidPosition) -> {
		for (AllowFluidFlow event : fluidFlowInteractionEvents) {
			if (!event.allowFlow(fluid, level, fluidPosition)) {
				return false;
			}
		}

		return true;
	});

	/**
	 * Called when a fluid flows.
	 *
	 * @param fluid         The fluid state of the flowing fluid.
	 * @param level         The level the event took place in.
	 * @param fluidPosition The position in the level that the fluid flowed into.
	 * @return {@code true} if the fluid is allowed to flow into the position. {@code false} if the fluid is not allowed to flow, such as a block was created.
	 */
	boolean allowFlow(FluidState fluid, LevelAccessor level, BlockPos fluidPosition);
}
