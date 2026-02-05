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

package net.fabricmc.fabric.test.lookup.api;

import java.util.function.Predicate;

import net.minecraft.world.item.ItemStack;

public final class ItemUtils {
	/**
	 * Move at most maxCount items, and return the number of items moved.
	 */
	public static int move(ItemExtractable from, ItemInsertable to, int maxCount) {
		Predicate<ItemStack> insertionFilter = stack -> {
			if (stack.isEmpty()) return false;

			ItemStack insertedStack = to.tryInsert(stack, true);
			return insertedStack.isEmpty() || insertedStack.getCount() < stack.getCount();
		};

		ItemStack extracted = from.tryExtract(maxCount, insertionFilter, true);
		ItemStack leftover = to.tryInsert(extracted, false);
		int moved = extracted.getCount() - leftover.getCount();
		from.tryExtract(moved, insertionFilter, false);
		return moved;
	}

	private ItemUtils() {
	}
}
