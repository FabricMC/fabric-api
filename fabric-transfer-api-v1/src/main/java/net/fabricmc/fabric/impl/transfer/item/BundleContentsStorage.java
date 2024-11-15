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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.math.Fraction;

import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.mixin.transfer.BundleContentsComponentAccessor;

public class BundleContentsStorage implements Storage<ItemVariant> {
	private final ContainerItemContext ctx;
	private final List<BundleSlotWrapper> slotCache = new ArrayList<>();
	private List<StorageView<ItemVariant>> slots = List.of();
	private final Item originalItem;

	public BundleContentsStorage(ContainerItemContext ctx) {
		this.ctx = ctx;
		this.originalItem = ctx.getItemVariant().getItem();
	}

	private boolean updateStack(ComponentChanges changes, TransactionContext transaction) {
		ItemVariant newVariant = ctx.getItemVariant().withComponentChanges(changes);
		return ctx.exchange(newVariant, 1, transaction) > 0;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);

		if (!isStillValid()) return 0;

		if (maxAmount > Integer.MAX_VALUE) maxAmount = Integer.MAX_VALUE;

		ItemStack stack = resource.toStack((int) maxAmount);

		if (stack.isEmpty() || !stack.getItem().canBeNested()) return 0;

		var builder = new BundleContentsComponent.Builder(bundleContents());

		int inserted = builder.add(stack);

		if (inserted == 0) return 0;

		ComponentChanges changes = ComponentChanges.builder()
				.add(DataComponentTypes.BUNDLE_CONTENTS, builder.build())
				.build();

		if (!updateStack(changes, transaction)) return 0;

		return inserted;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notNegative(maxAmount);

		if (!isStillValid()) return 0;

		updateSlotsIfNeeded();

		long amount = 0;

		for (StorageView<ItemVariant> slot : slots) {
			amount += slot.extract(resource, maxAmount - amount, transaction);
			if (amount == maxAmount) break;
		}

		return amount;
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator() {
		updateSlotsIfNeeded();

		return slots.iterator();
	}

	private boolean isStillValid() {
		return ctx.getItemVariant().getItem() == originalItem;
	}

	private void updateSlotsIfNeeded() {
		int bundleSize = bundleContents().size();

		if (slots.size() != bundleSize) {
			while (bundleSize > slotCache.size()) {
				slotCache.add(new BundleSlotWrapper(slotCache.size()));
			}

			slots = Collections.unmodifiableList(slotCache.subList(0, bundleSize));
		}
	}

	BundleContentsComponent bundleContents() {
		return ctx.getItemVariant().getComponentMap().getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
	}

	private class BundleSlotWrapper implements StorageView<ItemVariant> {
		private final int index;

		private BundleSlotWrapper(int index) {
			this.index = index;
		}

		private ItemStack getStack() {
			if (bundleContents().size() <= index) return ItemStack.EMPTY;

			return ((List<ItemStack>) bundleContents().iterate()).get(index);
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			StoragePreconditions.notNegative(maxAmount);

			if (!BundleContentsStorage.this.isStillValid()) return 0;
			if (bundleContents().size() <= index) return 0;
			if (!resource.matches(getStack())) return 0;

			var stacksCopy = new ArrayList<>((Collection<ItemStack>) bundleContents().iterateCopy());

			int extracted = (int) Math.min(stacksCopy.get(index).getCount(), maxAmount);

			stacksCopy.get(index).decrement(extracted);
			if (stacksCopy.get(index).isEmpty()) stacksCopy.remove(index);

			ComponentChanges changes = ComponentChanges.builder()
					.add(DataComponentTypes.BUNDLE_CONTENTS, new BundleContentsComponent(stacksCopy))
					.build();

			if (!updateStack(changes, transaction)) return 0;

			return extracted;
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
			Fraction remainingSpace = Fraction.ONE.subtract(bundleContents().getOccupancy());
			int extraAllowed = Math.max(
					remainingSpace.divideBy(BundleContentsComponentAccessor.getOccupancy(getStack())).intValue(),
					0
			);
			return getAmount() + extraAllowed;
		}
	}
}
