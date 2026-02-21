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

package net.fabricmc.fabric.test.lookup.compat;

import java.util.function.Predicate;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import net.fabricmc.fabric.test.lookup.api.ItemExtractable;
import net.fabricmc.fabric.test.lookup.api.ItemInsertable;

final class WrappedInventory implements ItemInsertable, ItemExtractable {
	private final Inventory inv;

	WrappedInventory(Inventory inv) {
		this.inv = inv;
	}

	@Override
	public ItemStack tryExtract(int maxCount, Predicate<ItemStack> filter, boolean simulate) {
		for (int i = 0; i < inv.size(); ++i) {
			ItemStack stack = inv.getStack(i);

			if (!stack.isEmpty() && filter.test(stack)) {
				ItemStack returned;

				if (simulate) {
					returned = stack.copy().split(maxCount);
				} else {
					returned = stack.split(maxCount);
				}

				return returned;
			}
		}

		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack tryInsert(ItemStack input, boolean simulate) {
		input = input.copy();

		for (int i = 0; i < inv.size(); ++i) {
			if (inv.isValid(i, input)) {
				ItemStack stack = inv.getStack(i);

				if (stack.isEmpty() || ItemStack.areItemsAndComponentsEqual(stack, input)) {
					int remainingSpace = Math.min(inv.getMaxCountPerStack(), stack.getMaxCount()) - stack.getCount();
					int inserted = Math.min(remainingSpace, input.getCount());

					if (!simulate) {
						if (stack.isEmpty()) {
							inv.setStack(i, input.copy());
							inv.getStack(i).setBobbingAnimationTime(inserted);
						} else {
							stack.increment(inserted);
						}
					}

					input.decrement(inserted);
				}
			}
		}

		return input;
	}
}
