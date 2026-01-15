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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.mojang.serialization.DataResult;
import org.apache.commons.lang3.math.Fraction;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.BundleContents;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.mixin.transfer.BundleContentsAccessor;

public class BundleContentsStorage implements Storage<ItemVariant> {
	private final ContainerItemContext ctx;
	private final List<BundleSlotWrapper> slotCache = new ArrayList<>();
	private List<StorageView<ItemVariant>> slots = List.of();
	private final Item originalItem;

	public BundleContentsStorage(ContainerItemContext ctx) {
		this.ctx = ctx;
		this.originalItem = ctx.getItemVariant().getItem();
	}

	private boolean updateStack(DataComponentPatch patch, TransactionContext transaction) {
		ItemVariant newVariant = ctx.getItemVariant().withComponents(patch);
		return ctx.exchange(newVariant, 1, transaction) > 0;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);

		if (!isStillValid()) return 0;

		if (maxAmount > Integer.MAX_VALUE) maxAmount = Integer.MAX_VALUE;

		ItemStack stack = resource.toStack((int) maxAmount);

		if (!BundleContents.canItemBeInBundle(stack)) return 0;

		var builder = new BundleContents.Mutable(bundleContents());

		int inserted = builder.tryInsert(stack);

		if (inserted == 0) return 0;

		DataComponentPatch changes = DataComponentPatch.builder()
				.set(DataComponents.BUNDLE_CONTENTS, builder.toImmutable())
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

	BundleContents bundleContents() {
		return ctx.getItemVariant().getComponents().getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
	}

	private class BundleSlotWrapper implements StorageView<ItemVariant> {
		private final int index;

		private BundleSlotWrapper(int index) {
			this.index = index;
		}

		private ItemStack getStack() {
			if (bundleContents().size() <= index) return ItemStack.EMPTY;

			return bundleContents().items().get(index).create();
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			StoragePreconditions.notNegative(maxAmount);

			if (!BundleContentsStorage.this.isStillValid()) return 0;
			if (bundleContents().size() <= index) return 0;
			if (!resource.matches(getStack())) return 0;

			var stacksCopy = new ArrayList<>(bundleContents().items());
			ItemStackTemplate toSrink = stacksCopy.get(index);
			int extracted = (int) Math.min(toSrink.count(), maxAmount);

			if (toSrink.count() - extracted <= 1) {
				stacksCopy.remove(index);
			} else {
				stacksCopy.set(index, new ItemStackTemplate(toSrink.item(), toSrink.count() - extracted, toSrink.components()));
			}

			DataComponentPatch changes = DataComponentPatch.builder()
					.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(stacksCopy))
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
			Fraction remainingSpace = Fraction.ONE.subtract(getWeight(bundleContents().weight()));
			int extraAllowed = Math.max(
					remainingSpace.divideBy(getWeight(BundleContentsAccessor.getWeight(getStack()))).intValue(),
					0
			);
			return getAmount() + extraAllowed;
		}

		private static Fraction getWeight(DataResult<Fraction> weight) {
			return switch (weight) {
			case DataResult.Success<Fraction> success -> success.value();
			case DataResult.Error<Fraction> ignored -> Fraction.ONE;
			};
		}
	}
}
