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

package net.fabricmc.fabric.api.client.itemgroup;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.item.group.ItemGroupExtensions;

public final class FabricItemGroupBuilder {
	private Identifier identifier;
	private Supplier<ItemStack> stackSupplier = () -> ItemStack.EMPTY;
	private BiConsumer<List<ItemStack>, ItemGroup> stacksForDisplay;

	private FabricItemGroupBuilder(Identifier identifier) {
		this.identifier = identifier;
	}

	/**
	 * Create a new Item Group Builder.
	 *
	 * @param identifier the id will become the name of the ItemGroup and will be used for the translation key
	 * @return a FabricItemGroupBuilder
	 */
	public static FabricItemGroupBuilder create(Identifier identifier) {
		return new FabricItemGroupBuilder(identifier);
	}

	/**
	 * This is used to add an icon to to the item group.
	 *
	 * @param stackSupplier the supplier should return the item stack that you wish to show on the tab
	 * @return a reference to the FabricItemGroupBuilder
	 */
	public FabricItemGroupBuilder icon(Supplier<ItemStack> stackSupplier) {
		this.stackSupplier = stackSupplier;
		return this;
	}

	/**
	 * This allows for a custom list of items to be displayed in a tab, this enabled tabs to be created with a custom set of items.
	 *
	 * @param appender Add ItemStack's to this list to show in the ItemGroup
	 * @return a reference to the FabricItemGroupBuilder
	 * @deprecated use {@link FabricItemGroupBuilder#appendItems(Consumer)}
	 */
	@Deprecated
	public FabricItemGroupBuilder stacksForDisplay(Consumer<List<ItemStack>> appender) {
		return appendItems(appender);
	}

	/**
	 * Set the item stacks of this item group, by having the consumer add them to the passed list.
	 * This bypasses {@link Item#appendStacks}. If you want to append stacks from your items, consider using {@linkplain #appendItems(BiConsumer) the other overload}.
	 *
	 * @param stacksForDisplay Add ItemStack's to this list to show in the ItemGroup
	 * @return a reference to the FabricItemGroupBuilder
	 */
	public FabricItemGroupBuilder appendItems(Consumer<List<ItemStack>> stacksForDisplay) {
		return appendItems((itemStacks, itemGroup) -> stacksForDisplay.accept(itemStacks));
	}

	/**
	 * Set the item stacks of this item group, by having the consumer add them to the passed list.
	 * Compared to the other overload, this one also passes the new ItemGroup.
	 * This allows you to call {@link Item#appendStacks} yourself if you want.
	 *
	 * @param stacksForDisplay Add ItemStack's to this list to show in the ItemGroup, and check if the item is in the ItemGroup
	 * @return a reference to the FabricItemGroupBuilder
	 */
	public FabricItemGroupBuilder appendItems(BiConsumer<List<ItemStack>, ItemGroup> stacksForDisplay) {
		this.stacksForDisplay = stacksForDisplay;
		return this;
	}

	/**
	 * This is a single method that makes creating an ItemGroup with an icon one call.
	 *
	 * @param identifier    the id will become the name of the ItemGroup and will be used for the translation key
	 * @param stackSupplier the supplier should return the item stack that you wish to show on the tab
	 * @return An instance of the built ItemGroup
	 */
	public static ItemGroup build(Identifier identifier, Supplier<ItemStack> stackSupplier) {
		return new FabricItemGroupBuilder(identifier).icon(stackSupplier).build();
	}

	/**
	 * Create an instance of the ItemGroup.
	 *
	 * @return An instance of the built ItemGroup
	 */
	public ItemGroup build() {
		((ItemGroupExtensions) ItemGroup.BUILDING_BLOCKS).fabric_expandArray();
		String groupKey = identifier.getNamespace();
		if (!identifier.getPath().isEmpty()) {
			groupKey = String.format("%s.%s", identifier.getNamespace(), identifier.getPath())
		}
		return new ItemGroup(ItemGroup.GROUPS.length - 1, groupKey) {
			@Override
			public ItemStack createIcon() {
				return stackSupplier.get();
			}

			@Override
			public void appendStacks(DefaultedList<ItemStack> stacks) {
				if (stacksForDisplay != null) {
					stacksForDisplay.accept(stacks, this);
					return;
				}

				super.appendStacks(stacks);
			}
		};
	}
}
