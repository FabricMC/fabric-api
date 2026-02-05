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

package net.fabricmc.fabric.impl.registry.sync.trackers.vanilla;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.mixin.registry.sync.DebugLevelSourceAccessor;

public final class BlockInitTracker {
	public static void postFreeze() {
		final List<BlockState> blockStateList = BuiltInRegistries.BLOCK.stream()
				.flatMap((block) -> block.getStateDefinition().getPossibleStates().stream())
				.toList();

		final int xLength = Mth.ceil(Mth.sqrt(blockStateList.size()));
		final int zLength = Mth.ceil(blockStateList.size() / (float) xLength);

		DebugLevelSourceAccessor.setALL_BLOCKS(blockStateList);
		DebugLevelSourceAccessor.setGRID_WIDTH(xLength);
		DebugLevelSourceAccessor.setGRID_HEIGHT(zLength);
	}
}
