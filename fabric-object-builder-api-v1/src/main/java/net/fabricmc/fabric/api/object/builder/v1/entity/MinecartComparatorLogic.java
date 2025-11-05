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

package net.fabricmc.fabric.api.object.builder.v1.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Provides custom comparator output for minecarts resting on detector rails.
 * @param <T> the handled minecart type
 */
@FunctionalInterface
public interface MinecartComparatorLogic<T extends AbstractMinecart> {
	/**
	 * Compute the comparator output of a detector rail when a minecart is resting
	 * on top of it. Called from {@link net.minecraft.world.level.block.DetectorRailBlock#getAnalogOutputSignal}.
	 * @param minecart The minecart on the rail
	 * @param state Block state of the rail
	 * @param pos Position of the rail
	 * @return A redstone power value {@literal >=} 0 to use, else a value {@literal <} 0 to try the next minecart with
	 * 	a registered logic. If no logic chooses to provide a value, vanilla's logic is invoked.
	 */
	int getComparatorValue(T minecart, BlockState state, BlockPos pos);
}
