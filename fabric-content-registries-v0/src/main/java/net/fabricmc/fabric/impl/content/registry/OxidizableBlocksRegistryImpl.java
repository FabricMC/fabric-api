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

package net.fabricmc.fabric.impl.content.registry;

import java.util.Objects;

import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopperBlocks;

public final class OxidizableBlocksRegistryImpl {
	private OxidizableBlocksRegistryImpl() {
	}

	public static void registerNextStage(Block from, Block to) {
		Objects.requireNonNull(from, "Oxidizable block cannot be null!");
		Objects.requireNonNull(to, "Oxidizable block cannot be null!");
		WeatheringCopper.NEXT_BY_BLOCK.get().put(from, to);
		// Fix #4371
		refreshRandomTickCache(from);
		refreshRandomTickCache(to);
	}

	public static void registerWaxable(Block unwaxed, Block waxed) {
		Objects.requireNonNull(unwaxed, "Unwaxed block cannot be null!");
		Objects.requireNonNull(waxed, "Waxed block cannot be null!");
		HoneycombItem.WAXABLES.get().put(unwaxed, waxed);
	}

	public static void registerWeatheringCopperBlocks(WeatheringCopperBlocks copperBlocks) {
		Objects.requireNonNull(copperBlocks, "copperBlocks cannot be null!");
		copperBlocks.weatheringMapping().forEach(OxidizableBlocksRegistryImpl::registerNextStage);
		copperBlocks.waxedMapping().forEach(OxidizableBlocksRegistryImpl::registerWaxable);
	}

	private static void refreshRandomTickCache(Block block) {
		block.getStateDefinition().getPossibleStates().forEach(state -> ((RandomTickCacheRefresher) state).fabric_api$refreshRandomTickCache());
	}

	public interface RandomTickCacheRefresher {
		void fabric_api$refreshRandomTickCache();
	}
}
