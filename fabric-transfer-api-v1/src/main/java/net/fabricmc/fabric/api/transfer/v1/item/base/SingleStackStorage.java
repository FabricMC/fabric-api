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

package net.fabricmc.fabric.api.transfer.v1.item.base;

import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

/**
 * An item variant storage backed by an {@link ItemStack}.
 * Implementors should at least override {@link #getStack} and {@link #setStack},
 * and probably {@link #onFinalCommit} as well for {@code markDirty()} and similar calls.
 *
 * <p>{@link #canInsert} and {@link #canExtract} can be used for more precise control over which items may be inserted or extracted.
 * If one of these two functions is overridden to always return false, implementors may also wish to override
 * {@link #supportsInsertion} and/or {@link #supportsExtraction}.
 * {@link #getCapacity(ItemVariant)} can be overridden to change the maximum capacity depending on the item variant.
 */
public abstract class SingleStackStorage extends SnapshotParticipant<ItemStack> implements SingleSlotStorage<ItemVariant> {
	/**
	 * Return the stack of this storage. It will be modified directly sometimes to avoid needless copies.
	 * However, any mutation of the stack will directly be followed by a call to {@link #setStack}.
	 * This means that either returning the backing stack directly or a copy is safe.
	 *
	 * @return The current stack.
	 */
	protected abstract ItemStack getStack();

	/**
	 * Set the stack of this storage.
	 */
	protected abstract void setStack(ItemStack stack);

	/**
	 * Return {@code true} if the passed non-blank item variant can be inserted, {@code false} otherwise.
	 */
	protected boolean canInsert(ItemVariant itemVariant) {
		return true;
	}

	/**
	 * Return {@code true} if the passed non-blank item variant can be extracted, {@code false} otherwise.
	 */
	protected boolean canExtract(ItemVariant itemVariant) {
		return true;
	}

	/**
	 * Return the maximum capacity of this storage for the passed item variant.
	 * If the passed item variant is blank, an estimate should be returned.
	 *
	 * <p>If the capacity should be limited by the max count of the item, this function must take it into account.
	 * For example, a storage with a maximum count of 4, or less for items that have a smaller max count,
	 * should override this to return {@code Math.min(itemVariant.getItem().getMaxCount(), 4);}.
	 *
	 * @return The maximum capacity of this storage for the passed item variant.
	 */
	protected int getCapacity(ItemVariant itemVariant) {
		return itemVariant.getItem().getDefaultMaxStackSize();
	}

	@Override
	public boolean isResourceBlank() {
		return getStack().isEmpty();
	}

	@Override
	public ItemVariant getResource() {
		return ItemVariant.of(getStack());
	}

	@Override
	public long getAmount() {
		return getStack().getCount();
	}

	@Override
	public long getCapacity() {
		return getCapacity(getResource());
	}

	@Override
	public long insert(ItemVariant insertedVariant, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(insertedVariant, maxAmount);

		ItemStack currentStack = getStack();

		if ((insertedVariant.matches(currentStack) || currentStack.isEmpty()) && canInsert(insertedVariant)) {
			int insertedAmount = (int) Math.min(maxAmount, getCapacity(insertedVariant) - currentStack.getCount());

			if (insertedAmount > 0) {
				updateSnapshots(transaction);
				currentStack = getStack();

				if (currentStack.isEmpty()) {
					currentStack = insertedVariant.toStack(insertedAmount);
				} else {
					currentStack.grow(insertedAmount);
				}

				setStack(currentStack);

				return insertedAmount;
			}
		}

		return 0;
	}

	@Override
	public long extract(ItemVariant variant, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(variant, maxAmount);

		ItemStack currentStack = getStack();

		if (variant.matches(currentStack) && canExtract(variant)) {
			int extracted = (int) Math.min(currentStack.getCount(), maxAmount);

			if (extracted > 0) {
				this.updateSnapshots(transaction);
				currentStack = getStack();
				currentStack.shrink(extracted);
				setStack(currentStack);

				return extracted;
			}
		}

		return 0;
	}

	@Override
	protected ItemStack createSnapshot() {
		ItemStack original = getStack();
		setStack(original.copy());
		return original;
	}

	@Override
	protected void readSnapshot(ItemStack snapshot) {
		setStack(snapshot);
	}

	@Override
	public String toString() {
		return "SingleStackStorage[" + getStack() + "]";
	}
}
