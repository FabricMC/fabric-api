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

package net.fabricmc.fabric.test.transfer.ingame;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class CreativeStorage<T extends TransferVariant<?>> implements ExtractionOnlyStorage<T>, SingleSlotStorage<T> {
	public static final CreativeStorage<FluidVariant> WATER = new CreativeStorage<>(FluidVariant.of(Fluids.WATER));
	public static final CreativeStorage<FluidVariant> LAVA = new CreativeStorage<>(FluidVariant.of(Fluids.LAVA));
	public static final CreativeStorage<ItemVariant> DIAMONDS = new CreativeStorage<>(ItemVariant.of(Items.DIAMOND));

	private final T infiniteResource;

	private CreativeStorage(T infiniteResource) {
		this.infiniteResource = infiniteResource;
	}

	@Override
	public boolean isResourceBlank() {
		return infiniteResource.isBlank();
	}

	@Override
	public T getResource() {
		return infiniteResource;
	}

	@Override
	public long getAmount() {
		return Long.MAX_VALUE;
	}

	@Override
	public long getCapacity() {
		return getAmount();
	}

	@Override
	public long extract(T resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);

		if (resource.equals(infiniteResource)) {
			return maxAmount;
		} else {
			return 0;
		}
	}

	@Override
	public long getVersion() {
		return 0;
	}
}
