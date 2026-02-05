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

package net.fabricmc.fabric.impl.transfer.item;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;

import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;

/**
 * Sidedness-aware wrapper around a {@link ContainerStorageImpl} for sided inventories.
 */
class SidedContainerStorageImpl extends CombinedStorage<ItemVariant, SingleSlotStorage<ItemVariant>> implements ContainerStorage {
	private final ContainerStorageImpl backingStorage;

	SidedContainerStorageImpl(ContainerStorageImpl storage, Direction direction) {
		super(Collections.unmodifiableList(createWrapperList(storage, direction)));
		this.backingStorage = storage;
	}

	@Override
	public List<SingleSlotStorage<ItemVariant>> getSlots() {
		return parts;
	}

	private static List<SingleSlotStorage<ItemVariant>> createWrapperList(ContainerStorageImpl storage, Direction direction) {
		WorldlyContainer inventory = (WorldlyContainer) storage.container;
		int[] availableSlots = inventory.getSlotsForFace(direction);
		WorldlyContainerSlotWrapper[] slots = new WorldlyContainerSlotWrapper[availableSlots.length];

		for (int i = 0; i < availableSlots.length; ++i) {
			slots[i] = new WorldlyContainerSlotWrapper(storage.backingList.get(availableSlots[i]), inventory, direction);
		}

		return Arrays.asList(slots);
	}

	@Override
	public String toString() {
		// These two are the same from the user's perspective.
		return backingStorage.toString();
	}
}
