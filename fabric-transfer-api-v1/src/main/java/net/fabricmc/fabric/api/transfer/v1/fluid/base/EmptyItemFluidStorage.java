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

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.BlankVariantView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/**
 * Base implementation of a fluid storage for an empty item.
 * The empty item can be filled with an exact amount of some fluid to yield a full item instead.
 * The default behavior is to copy the components from the empty item to the full item,
 * however there is a second constructor that allows customizing the mapping.
 *
 * <p>For example, an empty bucket could be registered to accept exactly 81000 droplets of water and turn into a water bucket, like that:
 * <pre>{@code
 * FluidStorage.combinedItemApiProvider(Items.BUCKET) // Go through the combined API provider to make sure other mods can provide storages for empty buckets.
 *             .register(context -> {// Register a provider for the bucket, returning a new storage every time:
 *                 return new EmptyItemFluidStorage(
 *                     context, // Pass the context.
 *                     Items.WATER_BUCKET, // The result after fluid is inserted.
 *                     Fluids.WATER, // Which fluid to accept.
 *                     FluidConstants.BUCKET // How much fluid to accept.
 *                 );
 *             });
 * }</pre>
 * (This is just for illustration purposes! In practice, Fabric API already registers storages for most buckets,
 * and it is inefficient to have one storage registered per fluid
 * so Fabric API has a storage that accepts any fluid with a corresponding full bucket).
 */
public final class EmptyItemFluidStorage implements InsertionOnlyStorage<FluidVariant> {
	private final ContainerItemContext context;
	private final Item emptyItem;
	private final Function<ItemVariant, ItemVariant> emptyToFullMapping;
	private final Fluid insertableFluid;
	private final long insertableAmount;
	private final List<StorageView<FluidVariant>> blankView;

	/**
	 * Create a new instance.
	 *
	 * @param context The current context.
	 * @param fullItem The new item after a successful fill operation.
	 * @param insertableFluid The fluid that can be inserted. Fluid variant components are ignored.
	 * @param insertableAmount The amount of fluid that can be inserted.
	 */
	public EmptyItemFluidStorage(ContainerItemContext context, Item fullItem, Fluid insertableFluid, long insertableAmount) {
		this(context, emptyVariant -> ItemVariant.of(fullItem, emptyVariant.getComponents()), insertableFluid, insertableAmount);
	}

	/**
	 * Create a new instance, with a custom mapping function.
	 * The mapping function allows customizing how the components of the full item depends on the components of the empty item.
	 * The default behavior with the other constructor is to just copy the full components.
	 *
	 * @param context The current context.
	 * @param emptyToFullMapping A function mapping the empty item variant, to the variant that should be used for the full item.
	 * @param insertableFluid The fluid that can be inserted. Fluid variant components are ignored on insertion.
	 * @param insertableAmount The amount of fluid that can be inserted.
	 * @see #EmptyItemFluidStorage(ContainerItemContext, Item, Fluid, long)
	 */
	public EmptyItemFluidStorage(ContainerItemContext context, Function<ItemVariant, ItemVariant> emptyToFullMapping, Fluid insertableFluid, long insertableAmount) {
		StoragePreconditions.notNegative(insertableAmount);

		this.context = context;
		this.emptyItem = context.getItemVariant().getItem();
		this.emptyToFullMapping = emptyToFullMapping;
		this.insertableFluid = insertableFluid;
		this.insertableAmount = insertableAmount;
		this.blankView = List.of(new BlankVariantView<>(FluidVariant.blank(), insertableAmount));
	}

	@Override
	public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);

		// Can't insert if the item is not emptyItem anymore.
		if (!context.getItemVariant().isOf(emptyItem)) return 0;

		// Make sure that the fluid and amount match.
		if (resource.isOf(insertableFluid) && maxAmount >= insertableAmount) {
			// If that's ok, just convert one of the empty item into the full item, with the mapping function.
			ItemVariant newVariant = emptyToFullMapping.apply(context.getItemVariant());

			if (context.exchange(newVariant, 1, transaction) == 1) {
				// Conversion ok!
				return insertableAmount;
			}
		}

		return 0;
	}

	@Override
	public Iterator<StorageView<FluidVariant>> iterator() {
		return blankView.iterator();
	}

	@Override
	public String toString() {
		return "EmptyItemFluidStorage[context=%s, insertableFluid=%s, insertableAmount=%d]"
				.formatted(context, insertableFluid, insertableAmount);
	}
}
