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
import java.util.stream.Collectors;

import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedSlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.mixin.transfer.ContainerComponentAccessor;

public class ContainerComponentStorage extends CombinedSlottedStorage<ItemVariant, SingleSlotStorage<ItemVariant>> {
	final ContainerItemContext ctx;
	private final Item originalItem;

	public ContainerComponentStorage(ContainerItemContext ctx, int slots) {
		super(Collections.emptyList());
		this.ctx = ctx;
		this.originalItem = ctx.getItemVariant().getItem();

		List<ContainerSlotWrapper> backingList = new ArrayList<>(slots);

		for (int i = 0; i < slots; i++) {
			backingList.add(new ContainerSlotWrapper(i));
		}

		parts = Collections.unmodifiableList(backingList);
	}

	ContainerComponent container() {
		return ctx.getItemVariant().getComponentMap().getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
	}

	ContainerComponentAccessor containerAccessor() {
		return (ContainerComponentAccessor) (Object) container();
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
			List<ItemStack> stacks = ContainerComponentStorage.this.containerAccessor().fabric_getStacks();

			if (stacks.size() <= slot) return ItemStack.EMPTY;

			return stacks.get(slot);
		}

		protected boolean setStack(ItemStack stack, TransactionContext transaction) {
			List<ItemStack> stacks = ContainerComponentStorage.this.container().stream().collect(Collectors.toList());

			while (stacks.size() <= slot) stacks.add(ItemStack.EMPTY);

			stacks.set(slot, stack);

			ContainerItemContext ctx = ContainerComponentStorage.this.ctx;

			ItemVariant newVariant = ctx.getItemVariant().withComponentChanges(ComponentChanges.builder()
							.add(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(stacks))
							.build());

			return ctx.exchange(newVariant, 1, transaction) == 1;
		}

		@Override
		public long insert(ItemVariant insertedVariant, long maxAmount, TransactionContext transaction) {
			StoragePreconditions.notBlankNotNegative(insertedVariant, maxAmount);

			if (!ContainerComponentStorage.this.isStillValid()) return 0;

			ItemStack currentStack = getStack();

			if ((insertedVariant.matches(currentStack) || currentStack.isEmpty()) && insertedVariant.getItem().canBeNested()) {
				int insertedAmount = (int) Math.min(maxAmount, getCapacity() - currentStack.getCount());

				if (insertedAmount > 0) {
					currentStack = getStack().copy();

					if (currentStack.isEmpty()) {
						currentStack = insertedVariant.toStack(insertedAmount);
					} else {
						currentStack.increment(insertedAmount);
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

			if (!ContainerComponentStorage.this.isStillValid()) return 0;

			ItemStack currentStack = getStack();

			if (variant.matches(currentStack)) {
				int extracted = (int) Math.min(currentStack.getCount(), maxAmount);

				if (extracted > 0) {
					currentStack = getStack().copy();
					currentStack.decrement(extracted);

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
			return getStack().getItem().getMaxCount();
		}

		@Override
		public String toString() {
			return "ContainerSlotWrapper[%s#%d]".formatted(ContainerComponentStorage.this.ctx.getItemVariant(), slot);
		}
	}
}
