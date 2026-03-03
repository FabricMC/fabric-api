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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemContainerContents;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedSlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.mixin.transfer.ItemContainerContentsAccessor;

public class ItemContainerContentsStorage extends CombinedSlottedStorage<ItemVariant, SingleSlotStorage<ItemVariant>> {
	final ContainerItemContext ctx;
	private final Item originalItem;

	public ItemContainerContentsStorage(ContainerItemContext ctx, int slots) {
		super(Collections.emptyList());
		this.ctx = ctx;
		this.originalItem = ctx.getItemVariant().getItem();

		List<ContainerSlotWrapper> backingList = new ArrayList<>(slots);

		for (int i = 0; i < slots; i++) {
			backingList.add(new ContainerSlotWrapper(i));
		}

		parts = Collections.unmodifiableList(backingList);
	}

	ItemContainerContents container() {
		return ctx.getItemVariant().getComponents().getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
	}

	ItemContainerContentsAccessor containerAccessor() {
		return (ItemContainerContentsAccessor) (Object) container();
	}

	private boolean isStillValid() {
		return ctx.getItemVariant().getItem() == originalItem;
	}

	private class ContainerSlotWrapper implements SingleSlotStorage<ItemVariant> {
		final int slot;

		ContainerSlotWrapper(int slot) {
			this.slot = slot;
		}

		private ItemStack getStack() {
			List<Optional<ItemStackTemplate>> stacks = ItemContainerContentsStorage.this.containerAccessor().fabric_getItems();

			if (stacks.size() <= slot) return ItemStack.EMPTY;

			return stacks.get(slot).map(ItemStackTemplate::create).orElse(ItemStack.EMPTY);
		}

		protected boolean setStack(ItemStack stack, TransactionContext transaction) {
			List<ItemStack> stacks = ItemContainerContentsStorage.this.container().allItemsCopyStream().collect(Collectors.toList());

			while (stacks.size() <= slot) stacks.add(ItemStack.EMPTY);

			stacks.set(slot, stack);

			ContainerItemContext ctx = ItemContainerContentsStorage.this.ctx;

			ItemVariant newVariant = ctx.getItemVariant().withComponents(DataComponentPatch.builder()
							.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(stacks))
							.build());

			return ctx.exchange(newVariant, 1, transaction) == 1;
		}

		@Override
		public long insert(ItemVariant insertedVariant, long maxAmount, TransactionContext transaction) {
			StoragePreconditions.notBlankNotNegative(insertedVariant, maxAmount);

			if (!ItemContainerContentsStorage.this.isStillValid()) return 0;

			ItemStack currentStack = getStack();

			if ((insertedVariant.matches(currentStack) || currentStack.isEmpty()) && insertedVariant.getItem().canFitInsideContainerItems()) {
				int insertedAmount = (int) Math.min(maxAmount, getCapacity() - currentStack.getCount());

				if (insertedAmount > 0) {
					currentStack = getStack().copy();

					if (currentStack.isEmpty()) {
						currentStack = insertedVariant.toStack(insertedAmount);
					} else {
						currentStack.grow(insertedAmount);
					}

					if (!setStack(currentStack, transaction)) return 0;

					return insertedAmount;
				}
			}

			return 0;
		}

		@Override
		public long extract(ItemVariant variant, long maxAmount, TransactionContext transaction) {
			StoragePreconditions.notBlankNotNegative(variant, maxAmount);

			if (!ItemContainerContentsStorage.this.isStillValid()) return 0;

			ItemStack currentStack = getStack();

			if (variant.matches(currentStack)) {
				int extracted = (int) Math.min(currentStack.getCount(), maxAmount);

				if (extracted > 0) {
					currentStack = getStack().copy();
					currentStack.shrink(extracted);

					if (!setStack(currentStack, transaction)) return 0;

					return extracted;
				}
			}

			return 0;
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
			return getStack().getMaxStackSize();
		}

		@Override
		public String toString() {
			return "ContainerSlotWrapper[%s#%d]".formatted(ItemContainerContentsStorage.this.ctx.getItemVariant(), slot);
		}
	}
}
