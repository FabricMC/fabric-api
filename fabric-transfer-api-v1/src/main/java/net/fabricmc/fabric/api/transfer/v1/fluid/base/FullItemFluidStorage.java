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

package net.fabricmc.fabric.api.transfer.v1.fluid.base;

import java.util.function.Function;

import net.minecraft.world.item.Item;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/**
 * Base implementation of a fluid storage for a full item.
 * The full item contains some fixed amount of a fluid variant, which can be extracted entirely to yield an empty item.
 * The default behavior is to copy the components from the full item to the empty item,
 * however there is a second constructor that allows customizing the mapping.
 *
 * <p>This is used similarly to {@link EmptyItemFluidStorage}.
 */
public final class FullItemFluidStorage implements ExtractionOnlyStorage<FluidVariant>, SingleSlotStorage<FluidVariant> {
	private final ContainerItemContext context;
	private final Item fullItem;
	private final Function<ItemVariant, ItemVariant> fullToEmptyMapping;
	private final FluidVariant containedFluid;
	private final long containedAmount;

	/**
	 * Create a new instance.
	 *
	 * @param context The current context.
	 * @param emptyItem The new item after a successful extract operation.
	 * @param containedFluid The contained fluid variant.
	 * @param containedAmount How much of {@code containedFluid} is contained.
	 */
	public FullItemFluidStorage(ContainerItemContext context, Item emptyItem, FluidVariant containedFluid, long containedAmount) {
		this(context, fullVariant -> ItemVariant.of(emptyItem, fullVariant.getComponents()), containedFluid, containedAmount);
	}

	/**
	 * Create a new instance, with a custom mapping function.
	 * The mapping function allows customizing how the components of the empty item depends on the components of the full item.
	 * The default behavior with the other constructor is to just copy the full components.
	 *
	 * @param context The current context.
	 * @param fullToEmptyMapping A function mapping the full item variant, to the variant that should be used
	 *                           for the empty item after a successful extract operation.
	 * @param containedFluid The contained fluid variant.
	 * @param containedAmount How much of {@code containedFluid} is contained.
	 */
	public FullItemFluidStorage(ContainerItemContext context, Function<ItemVariant, ItemVariant> fullToEmptyMapping, FluidVariant containedFluid, long containedAmount) {
		StoragePreconditions.notBlankNotNegative(containedFluid, containedAmount);

		this.context = context;
		this.fullItem = context.getItemVariant().getItem();
		this.fullToEmptyMapping = fullToEmptyMapping;
		this.containedFluid = containedFluid;
		this.containedAmount = containedAmount;
	}

	@Override
	public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);

		// If the context's item is not fullItem anymore, can't extract!
		if (!context.getItemVariant().isOf(fullItem)) return 0;

		// Make sure that the fluid and the amount match.
		if (resource.equals(containedFluid) && maxAmount >= containedAmount) {
			// If that's ok, just convert one of the full item into the empty item, copying the nbt.
			ItemVariant newVariant = fullToEmptyMapping.apply(context.getItemVariant());

			if (context.exchange(newVariant, 1, transaction) == 1) {
				// Conversion ok!
				return containedAmount;
			}
		}

		return 0;
	}

	@Override
	public boolean isResourceBlank() {
		return getResource().isBlank();
	}

	@Override
	public FluidVariant getResource() {
		// Only contains a resource if the item of the context is still this one.
		if (context.getItemVariant().isOf(fullItem)) {
			return containedFluid;
		} else {
			return FluidVariant.blank();
		}
	}

	@Override
	public long getAmount() {
		if (context.getItemVariant().isOf(fullItem)) {
			return containedAmount;
		} else {
			return 0;
		}
	}

	@Override
	public long getCapacity() {
		// Capacity is the same as the amount.
		return getAmount();
	}

	@Override
	public String toString() {
		return "FullItemFluidStorage[context=%s, fluid=%s, amount=%d]"
				.formatted(context, containedFluid, containedAmount);
	}
}
