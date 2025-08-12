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

import net.minecraft.block.Block;
import net.minecraft.block.CopperBlockSet;
import net.minecraft.block.Oxidizable;
import net.minecraft.item.HoneycombItem;

public final class OxidizableBlocksRegistryImpl {
	private OxidizableBlocksRegistryImpl() {
	}

	public static void registerOxidizableBlockPair(Block less, Block more) {
		Objects.requireNonNull(less, "Oxidizable block cannot be null!");
		Objects.requireNonNull(more, "Oxidizable block cannot be null!");
		Oxidizable.OXIDATION_LEVEL_INCREASES.get().put(less, more);
		// Fix #4371
		refreshRandomTickCache(less);
		refreshRandomTickCache(more);
	}

	public static void registerWaxableBlockPair(Block unwaxed, Block waxed) {
		Objects.requireNonNull(unwaxed, "Unwaxed block cannot be null!");
		Objects.requireNonNull(waxed, "Waxed block cannot be null!");
		HoneycombItem.UNWAXED_TO_WAXED_BLOCKS.get().put(unwaxed, waxed);
	}

	public static void registerCopperBlockSet(CopperBlockSet blockSet) {
		Objects.requireNonNull(blockSet, "blockSet cannot be null!");
		blockSet.getOxidizingMap().forEach(OxidizableBlocksRegistryImpl::registerOxidizableBlockPair);
		blockSet.getWaxingMap().forEach(OxidizableBlocksRegistryImpl::registerWaxableBlockPair);
	}

	private static void refreshRandomTickCache(Block block) {
		block.getStateManager().getStates().forEach(state -> ((RandomTickCacheRefresher) state).fabric_api$refreshRandomTickCache());
	}

	public interface RandomTickCacheRefresher {
		void fabric_api$refreshRandomTickCache();
	}
}
