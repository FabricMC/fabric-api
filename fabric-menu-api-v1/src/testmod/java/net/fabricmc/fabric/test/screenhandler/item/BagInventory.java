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

package net.fabricmc.fabric.test.screenhandler.item;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

final class BagInventory implements ImplementedInventory {
	private final ItemStack stack;
	private final NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);

	BagInventory(ItemStack stack) {
		this.stack = stack;
		ItemContainerContents container = stack.get(DataComponents.CONTAINER);

		if (container != null) container.copyInto(items);
	}

	@Override
	public NonNullList<ItemStack> getItems() {
		return items;
	}

	@Override
	public void setChanged() {
		stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
	}
}
