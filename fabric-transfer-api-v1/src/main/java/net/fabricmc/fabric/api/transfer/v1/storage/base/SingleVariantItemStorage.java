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

package net.fabricmc.fabric.api.transfer.v1.storage.base;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/**
 * Base implementation of a fixed-capacity "continuous" storage for item-provided storage APIs.
 * The item may not change, so the data has to be stored in the components of the stacks.
 * This can be used for example to implement portable fluid tanks, fluid-containing jetpacks, and so on...
 * Continuous here means that they can store any integer amount between 0 and the capacity, unlike buckets or bottles.
 *
 * <p>To expose the storage API for an item, you need to register a provider for your item, and pass it an instance of this class:
 * <ul>
 *    <li>You must override {@link #getBlankResource()}, for example {@code return FluidVariant.blank();} for fluids.</li>
 *    <li>You must override {@link #getResource(ItemVariant)} and {@link #getAmount(ItemVariant)}.
 *    Generally you will read the resource and the amount from the components of the item variant.</li>
 *    <li>You must override {@link #getCapacity(TransferVariant)} to set the capacity of your storage.</li>
 *    <li>You must override {@link #getUpdatedVariant}. It is used to change the resource and the amount of the item variant.
 *    Generally you will copy the components, modify it, and then create a new variant from that.
 *    Copying the components instead of recreating it from scratch is important to keep custom names or enchantments.</li>
 *    <li>You may also override {@link #canInsert} and {@link #canExtract} if you want to restrict insertion and/or extraction.</li>
 * </ul>
 *
 * @param <T> The type of the stored transfer variant.
 */
public abstract class SingleVariantItemStorage<T extends TransferVariant<?>> implements SingleSlotStorage<T> {
	/**
	 * Reference to the context.
	 */
	private final ContainerItemContext context;
	/**
	 * Starting item. The storage is not valid for other items.
	 */
	private final Item item;

	public SingleVariantItemStorage(ContainerItemContext context) {
		this.context = context;
		this.item = context.getItemVariant().getItem();
	}

	/**
	 * Return the blank resource.
	 */
	protected abstract T getBlankResource();

	/**
	 * Return the current resource by reading the components of the passed variant.
	 */
	protected abstract T getResource(ItemVariant currentVariant);

	/**
	 * Return the current amount by reading the components of the passed variant.
	 */
	protected abstract long getAmount(ItemVariant currentVariant);

	/**
	 * Return the capacity of this storage for the passed resource.
	 * An estimate should be returned if the passed resource is blank.
	 */
	protected abstract long getCapacity(T variant);

	/**
	 * Return an updated variant with new resource and amount.
	 * Implementors should generally convert the passed {@code currentVariant} to a stack,
	 * then edit the components of the stack so it contains the correct resource and amount.
	 *
	 * <p>When the new amount is 0, it is recommended that the components corresponding to the resource and amount
	 * be removed, so that newly-crafted containers can stack with emptied containers.
	 * If a custom item is used, this means {@linkplain ItemStack#set setting} it to the default as specified in {@link Item.Properties#component};
	 * if a vanilla item is used, this means {@linkplain ItemStack#remove removing} the said component.
	 *
	 * @param currentVariant Variant to which the modification should be applied.
	 * @param newResource Resource that should be contained in the returned variant.
	 * @param newAmount Amount that should be contained in the returned variant.
	 * @return A modified variant containing the new resource and amount.
	 */
	protected abstract ItemVariant getUpdatedVariant(ItemVariant currentVariant, T newResource, long newAmount);

	/**
	 * Return {@code true} if the passed non-blank variant can be inserted, {@code false} otherwise.
	 */
	protected boolean canInsert(T resource) {
		return true;
	}

	/**
	 * Return {@code true} if the passed non-blank variant can be extracted, {@code false} otherwise.
	 */
	protected boolean canExtract(T resource) {
		return true;
	}

	private boolean tryUpdateStorage(T newResource, long newAmount, TransactionContext tx) {
		return context.exchange(getUpdatedVariant(context.getItemVariant(), newResource, newAmount), 1, tx) == 1;
	}

	@Override
	public boolean supportsInsertion() {
		return context.getItemVariant().isOf(item);
	}

	@Override
	public long insert(T insertedResource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(insertedResource, maxAmount);

		// Check insertion.
		if (!canInsert(insertedResource)) return 0;
		// Check item.
		if (!context.getItemVariant().isOf(item)) return 0;

		long amount = getAmount(context.getItemVariant());
		T resource = getResource(context.getItemVariant());

		long inserted = 0;

		if (resource.isBlank() || amount == 0) {
			// Insertion into empty storage.
			inserted = Math.min(getCapacity(insertedResource), maxAmount);
		} else if (resource.equals(insertedResource)) {
			// Insertion into storage with an existing resource.
			inserted = Math.min(getCapacity(insertedResource) - amount, maxAmount);
		}

		if (inserted > 0) {
			if (tryUpdateStorage(insertedResource, amount + inserted, transaction)) {
				return inserted;
			}
		}

		return 0;
	}

	@Override
	public boolean supportsExtraction() {
		return context.getItemVariant().isOf(item);
	}

	@Override
	public long extract(T extractedResource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(extractedResource, maxAmount);

		// Check extraction.
		if (!canExtract(extractedResource)) return 0;

		// Check item.
		if (!context.getItemVariant().isOf(item)) return 0;

		long amount = getAmount(context.getItemVariant());
		T resource = getResource(context.getItemVariant());

		long extracted = 0;

		if (resource.equals(extractedResource)) {
			// Make sure the resource matches
			extracted = Math.min(maxAmount, amount);
		}

		if (extracted > 0) {
			if (tryUpdateStorage(resource, amount - extracted, transaction)) {
				return extracted;
			}
		}

		return 0;
	}

	@Override
	public boolean isResourceBlank() {
		return getResource().isBlank();
	}

	@Override
	public T getResource() {
		if (context.getItemVariant().isOf(item)) {
			return getResource(context.getItemVariant());
		} else {
			return getBlankResource();
		}
	}

	@Override
	public long getAmount() {
		if (context.getItemVariant().isOf(item)) {
			return getAmount(context.getItemVariant());
		} else {
			return 0;
		}
	}

	@Override
	public long getCapacity() {
		if (context.getItemVariant().isOf(item)) {
			return getCapacity(getResource());
		} else {
			return 0;
		}
	}

	@Override
	public String toString() {
		return "SingleVariantItemStorage[" + context + "/" + item + "]";
	}
}
