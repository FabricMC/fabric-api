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

package net.fabricmc.fabric.api.creativetab.v1;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * This class allows the output of {@linkplain CreativeModeTab creative mode tabs} to be modified by the events in {@link CreativeModeTabEvents}.
 */
public class FabricCreativeModeTabOutput implements CreativeModeTab.Output {
	private final CreativeModeTab.ItemDisplayParameters context;
	private final List<ItemStack> displayStacks;
	private final List<ItemStack> searchTabStacks;

	@ApiStatus.Internal
	public FabricCreativeModeTabOutput(CreativeModeTab.ItemDisplayParameters context, List<ItemStack> displayStacks, List<ItemStack> searchTabStacks) {
		this.context = context;
		this.displayStacks = displayStacks;
		this.searchTabStacks = searchTabStacks;
	}

	public CreativeModeTab.ItemDisplayParameters getContext() {
		return context;
	}

	/**
	 * @return the currently enabled feature set
	 */
	public FeatureFlagSet getEnabledFeatures() {
		return context.enabledFeatures();
	}

	/**
	 * @return whether to show items restricted to operators, such as command blocks
	 */
	public boolean shouldShowOpRestrictedItems() {
		return context.hasPermissions();
	}

	/**
	 * @return the stacks that will be shown in the tab in the creative mode inventory
	 * @apiNote This list can be modified.
	 */
	public List<ItemStack> getDisplayStacks() {
		return displayStacks;
	}

	/**
	 * @return the stacks that will be searched by the creative mode inventory search
	 * @apiNote This list can be modified.
	 */
	public List<ItemStack> getSearchTabStacks() {
		return searchTabStacks;
	}

	/**
	 * Adds a stack to the end of the creative mode tab. Duplicate stacks will be removed.
	 *
	 * @param visibility Determines whether the stack will be shown in the tab itself, returned
	 *                   for searches, or both.
	 */
	@Override
	public void accept(ItemStack stack, CreativeModeTab.TabVisibility visibility) {
		if (isEnabled(stack)) {
			checkStack(stack);

			switch (visibility) {
			case PARENT_AND_SEARCH_TABS -> {
				this.displayStacks.add(stack);
				this.searchTabStacks.add(stack);
			}
			case PARENT_TAB_ONLY -> this.displayStacks.add(stack);
			case SEARCH_TAB_ONLY -> this.searchTabStacks.add(stack);
			}
		}
	}

	/**
	 * See {@link #prepend(ItemStack, CreativeModeTab.TabVisibility)}. Will use {@link CreativeModeTab.TabVisibility#PARENT_AND_SEARCH_TABS}
	 * for visibility.
	 */
	public void prepend(ItemStack stack) {
		prepend(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	}

	/**
	 * Adds a stack to the beginning of the creative mode tab. Duplicate stacks will be removed.
	 *
	 * @param visibility Determines whether the stack will be shown in the tab itself, returned
	 *                   for searches, or both.
	 */
	public void prepend(ItemStack stack, CreativeModeTab.TabVisibility visibility) {
		if (isEnabled(stack)) {
			checkStack(stack);

			switch (visibility) {
			case PARENT_AND_SEARCH_TABS -> {
				this.displayStacks.add(0, stack);
				this.searchTabStacks.add(0, stack);
			}
			case PARENT_TAB_ONLY -> this.displayStacks.add(0, stack);
			case SEARCH_TAB_ONLY -> this.searchTabStacks.add(0, stack);
			}
		}
	}

	/**
	 * See {@link #prepend(ItemStack)}. Automatically creates an {@link ItemStack} from the given item.
	 */
	public void prepend(ItemLike item) {
		prepend(item, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	}

	/**
	 * See {@link #prepend(ItemStack, net.minecraft.world.item.CreativeModeTab.TabVisibility)}.
	 * Automatically creates an {@link ItemStack} from the given item.
	 */
	public void prepend(ItemLike item, CreativeModeTab.TabVisibility visibility) {
		prepend(new ItemStack(item), visibility);
	}

	/**
	 * See {@link #insertAfter(ItemLike, Collection)}.
	 */
	public void insertAfter(ItemLike afterLast, ItemStack... newStack) {
		insertAfter(afterLast, Arrays.asList(newStack));
	}

	/**
	 * See {@link #insertAfter(ItemStack, Collection)}.
	 */
	public void insertAfter(ItemStack afterLast, ItemStack... newStack) {
		insertAfter(afterLast, Arrays.asList(newStack));
	}

	/**
	 * See {@link #insertAfter(ItemLike, Collection)}.
	 */
	public void insertAfter(ItemLike afterLast, ItemLike... newItem) {
		insertAfter(afterLast, Arrays.stream(newItem).map(ItemStack::new).toList());
	}

	/**
	 * See {@link #insertAfter(ItemStack, Collection)}.
	 */
	public void insertAfter(ItemStack afterLast, ItemLike... newItem) {
		insertAfter(afterLast, Arrays.stream(newItem).map(ItemStack::new).toList());
	}

	/**
	 * See {@link #insertAfter(ItemLike, Collection, net.minecraft.world.item.CreativeModeTab.TabVisibility)}.
	 */
	public void insertAfter(ItemLike afterLast, Collection<ItemStack> newStacks) {
		insertAfter(afterLast, newStacks, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	}

	/**
	 * See {@link #insertAfter(ItemStack, Collection, net.minecraft.world.item.CreativeModeTab.TabVisibility)}.
	 */
	public void insertAfter(ItemStack afterLast, Collection<ItemStack> newStacks) {
		insertAfter(afterLast, newStacks, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	}

	/**
	 * Adds stacks after an existing item in the tab, or at the end, if the item isn't in the tab.
	 *
	 * @param afterLast  Add {@code newStacks} after the last entry of this item in the tab.
	 * @param newStacks  The stacks to add. Only {@linkplain #isEnabled(ItemStack) enabled} stacks will be added.
	 * @param visibility Determines whether the stack will be shown in the tab itself, returned
	 *                   for searches, or both.
	 */
	public void insertAfter(ItemLike afterLast, Collection<ItemStack> newStacks, CreativeModeTab.TabVisibility visibility) {
		newStacks = getEnabledStacks(newStacks);

		if (newStacks.isEmpty()) {
			return;
		}

		switch (visibility) {
		case PARENT_AND_SEARCH_TABS -> {
			insertAfter(afterLast, newStacks, displayStacks);
			insertAfter(afterLast, newStacks, searchTabStacks);
		}
		case PARENT_TAB_ONLY -> insertAfter(afterLast, newStacks, displayStacks);
		case SEARCH_TAB_ONLY -> insertAfter(afterLast, newStacks, searchTabStacks);
		}
	}

	/**
	 * Adds stacks after an existing stack in the tab, or at the end, if the stack isn't in the tab.
	 *
	 * @param afterLast  Add {@code newStacks} after the last creative mode tab output matching this stack (compared using {@link ItemStack#isSameItemSameComponents}).
	 * @param newStacks  The stacks to add. Only {@linkplain #isEnabled(ItemStack) enabled} stacks will be added.
	 * @param visibility Determines whether the stack will be shown in the tab itself, returned
	 *                   for searches, or both.
	 */
	public void insertAfter(ItemStack afterLast, Collection<ItemStack> newStacks, CreativeModeTab.TabVisibility visibility) {
		newStacks = getEnabledStacks(newStacks);

		if (newStacks.isEmpty()) {
			return;
		}

		switch (visibility) {
		case PARENT_AND_SEARCH_TABS -> {
			insertAfter(afterLast, newStacks, displayStacks);
			insertAfter(afterLast, newStacks, searchTabStacks);
		}
		case PARENT_TAB_ONLY -> insertAfter(afterLast, newStacks, displayStacks);
		case SEARCH_TAB_ONLY -> insertAfter(afterLast, newStacks, searchTabStacks);
		}
	}

	/**
	 * Adds stacks after the last creative mode tab output matching a predicate, or at the end, if no outputs match.
	 *
	 * @param afterLast  Add {@code newStacks} after the last creative mode tab output matching this predicate.
	 * @param newStacks  The stacks to add. Only {@linkplain #isEnabled(ItemStack) enabled} stacks will be added.
	 * @param visibility Determines whether the stack will be shown in the tab itself, returned
	 *                   for searches, or both.
	 */
	public void insertAfter(Predicate<ItemStack> afterLast, Collection<ItemStack> newStacks, CreativeModeTab.TabVisibility visibility) {
		newStacks = getEnabledStacks(newStacks);

		if (newStacks.isEmpty()) {
			return;
		}

		switch (visibility) {
		case PARENT_AND_SEARCH_TABS -> {
			insertAfter(afterLast, newStacks, displayStacks);
			insertAfter(afterLast, newStacks, searchTabStacks);
		}
		case PARENT_TAB_ONLY -> insertAfter(afterLast, newStacks, displayStacks);
		case SEARCH_TAB_ONLY -> insertAfter(afterLast, newStacks, searchTabStacks);
		}
	}

	/**
	 * See {@link #insertBefore(ItemLike, Collection)}.
	 */
	public void insertBefore(ItemLike beforeFirst, ItemStack... newStack) {
		insertBefore(beforeFirst, Arrays.asList(newStack));
	}

	/**
	 * See {@link #insertBefore(ItemStack, Collection)}.
	 */
	public void insertBefore(ItemStack beforeFirst, ItemStack... newStack) {
		insertBefore(beforeFirst, Arrays.asList(newStack));
	}

	/**
	 * See {@link #insertBefore(ItemLike, Collection)}.
	 */
	public void insertBefore(ItemLike beforeFirst, ItemLike... newItem) {
		insertBefore(beforeFirst, Arrays.stream(newItem).map(ItemStack::new).toList());
	}

	/**
	 * See {@link #insertBefore(ItemStack, Collection)}.
	 */
	public void insertBefore(ItemStack beforeFirst, ItemLike... newItem) {
		insertBefore(beforeFirst, Arrays.stream(newItem).map(ItemStack::new).toList());
	}

	/**
	 * See {@link #insertBefore(ItemLike, Collection, net.minecraft.world.item.CreativeModeTab.TabVisibility)}.
	 */
	public void insertBefore(ItemLike beforeFirst, Collection<ItemStack> newStacks) {
		insertBefore(beforeFirst, newStacks, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	}

	/**
	 * See {@link #insertBefore(ItemStack, Collection, net.minecraft.world.item.CreativeModeTab.TabVisibility)}.
	 */
	public void insertBefore(ItemStack beforeFirst, Collection<ItemStack> newStacks) {
		insertBefore(beforeFirst, newStacks, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
	}

	/**
	 * Adds stacks before an existing item in the tab, or at the end, if the item isn't in the tab.
	 *
	 * @param beforeFirst Add {@code newStacks} before the first entry of this item in the tab.
	 * @param newStacks   The stacks to add. Only {@linkplain #isEnabled(ItemStack) enabled} stacks will be added.
	 * @param visibility  Determines whether the stack will be shown in the tab itself, returned
	 *                    for searches, or both.
	 */
	public void insertBefore(ItemLike beforeFirst, Collection<ItemStack> newStacks, CreativeModeTab.TabVisibility visibility) {
		newStacks = getEnabledStacks(newStacks);

		if (newStacks.isEmpty()) {
			return;
		}

		switch (visibility) {
		case PARENT_AND_SEARCH_TABS -> {
			insertBefore(beforeFirst, newStacks, displayStacks);
			insertBefore(beforeFirst, newStacks, searchTabStacks);
		}
		case PARENT_TAB_ONLY -> insertBefore(beforeFirst, newStacks, displayStacks);
		case SEARCH_TAB_ONLY -> insertBefore(beforeFirst, newStacks, searchTabStacks);
		}
	}

	/**
	 * Adds stacks before an existing stack to the creative mode tab, or at the end, if the stack isn't in the creative mode tab.
	 *
	 * @param beforeFirst Add {@code newStacks} before the first creative mode tab output matching this stack (compared using {@link ItemStack#isSameItemSameComponents}).
	 * @param newStacks   The stacks to add. Only {@linkplain #isEnabled(ItemStack) enabled} stacks will be added.
	 * @param visibility  Determines whether the stack will be shown in the tab itself, returned
	 *                    for searches, or both.
	 */
	public void insertBefore(ItemStack beforeFirst, Collection<ItemStack> newStacks, CreativeModeTab.TabVisibility visibility) {
		newStacks = getEnabledStacks(newStacks);

		if (newStacks.isEmpty()) {
			return;
		}

		switch (visibility) {
		case PARENT_AND_SEARCH_TABS -> {
			insertBefore(beforeFirst, newStacks, displayStacks);
			insertBefore(beforeFirst, newStacks, searchTabStacks);
		}
		case PARENT_TAB_ONLY -> insertBefore(beforeFirst, newStacks, displayStacks);
		case SEARCH_TAB_ONLY -> insertBefore(beforeFirst, newStacks, searchTabStacks);
		}
	}

	/**
	 * Adds stacks before the first tab output matching a predicate, or at the end, if no output match.
	 *
	 * @param beforeFirst Add {@code newStacks} before the first tab output matching this predicate.
	 * @param newStacks   The stacks to add. Only {@linkplain #isEnabled(ItemStack) enabled} stacks will be added.
	 * @param visibility  Determines whether the stack will be shown in the tab itself, returned
	 *                    for searches, or both.
	 */
	public void insertBefore(Predicate<ItemStack> beforeFirst, Collection<ItemStack> newStacks, CreativeModeTab.TabVisibility visibility) {
		newStacks = getEnabledStacks(newStacks);

		if (newStacks.isEmpty()) {
			return;
		}

		switch (visibility) {
		case PARENT_AND_SEARCH_TABS -> {
			insertBefore(beforeFirst, newStacks, displayStacks);
			insertBefore(beforeFirst, newStacks, searchTabStacks);
		}
		case PARENT_TAB_ONLY -> insertBefore(beforeFirst, newStacks, displayStacks);
		case SEARCH_TAB_ONLY -> insertBefore(beforeFirst, newStacks, searchTabStacks);
		}
	}

	/**
	 * @return True if the item of a given stack is enabled in the current {@link FeatureFlagSet}.
	 * @see Item#isEnabled
	 */
	private boolean isEnabled(ItemStack stack) {
		return stack.getItem().isEnabled(getEnabledFeatures());
	}

	private Collection<ItemStack> getEnabledStacks(Collection<ItemStack> newStacks) {
		// If not all stacks are enabled, filter the list, otherwise use it as-is
		if (newStacks.stream().allMatch(this::isEnabled)) {
			return newStacks;
		}

		return newStacks.stream().filter(this::isEnabled).toList();
	}

	/**
	 * Adds the {@link ItemStack} before the first match, if no matches the {@link ItemStack} is appended to the end of the {@link CreativeModeTab}.
	 */
	private static void insertBefore(Predicate<ItemStack> predicate, Collection<ItemStack> newStacks, List<ItemStack> addTo) {
		checkStacks(newStacks);

		for (int i = 0; i < addTo.size(); i++) {
			if (predicate.test(addTo.get(i))) {
				addTo.subList(i, i).addAll(newStacks);
				return;
			}
		}

		// Anchor not found, add to end
		addTo.addAll(newStacks);
	}

	private static void insertAfter(Predicate<ItemStack> predicate, Collection<ItemStack> newStacks, List<ItemStack> addTo) {
		checkStacks(newStacks);

		// Iterate in reverse to add after the last match
		for (int i = addTo.size() - 1; i >= 0; i--) {
			if (predicate.test(addTo.get(i))) {
				addTo.subList(i + 1, i + 1).addAll(newStacks);
				return;
			}
		}

		// Anchor not found, add to end
		addTo.addAll(newStacks);
	}

	private static void insertBefore(ItemStack anchor, Collection<ItemStack> newStacks, List<ItemStack> addTo) {
		checkStacks(newStacks);

		for (int i = 0; i < addTo.size(); i++) {
			if (ItemStack.isSameItemSameComponents(anchor, addTo.get(i))) {
				addTo.subList(i, i).addAll(newStacks);
				return;
			}
		}

		// Anchor not found, add to end
		addTo.addAll(newStacks);
	}

	private static void insertAfter(ItemStack anchor, Collection<ItemStack> newStacks, List<ItemStack> addTo) {
		checkStacks(newStacks);

		// Iterate in reverse to add after the last match
		for (int i = addTo.size() - 1; i >= 0; i--) {
			if (ItemStack.isSameItemSameComponents(anchor, addTo.get(i))) {
				addTo.subList(i + 1, i + 1).addAll(newStacks);
				return;
			}
		}

		// Anchor not found, add to end
		addTo.addAll(newStacks);
	}

	private static void insertBefore(ItemLike anchor, Collection<ItemStack> newStacks, List<ItemStack> addTo) {
		checkStacks(newStacks);

		Item anchorItem = anchor.asItem();

		for (int i = 0; i < addTo.size(); i++) {
			if (addTo.get(i).is(anchorItem)) {
				addTo.subList(i, i).addAll(newStacks);
				return;
			}
		}

		// Anchor not found, add to end
		addTo.addAll(newStacks);
	}

	private static void insertAfter(ItemLike anchor, Collection<ItemStack> newStacks, List<ItemStack> addTo) {
		checkStacks(newStacks);

		Item anchorItem = anchor.asItem();

		// Iterate in reverse to add after the last match
		for (int i = addTo.size() - 1; i >= 0; i--) {
			if (addTo.get(i).is(anchorItem)) {
				addTo.subList(i + 1, i + 1).addAll(newStacks);
				return;
			}
		}

		// Anchor not found, add to end
		addTo.addAll(newStacks);
	}

	private static void checkStacks(Collection<ItemStack> stacks) {
		for (ItemStack stack : stacks) {
			checkStack(stack);
		}
	}

	private static void checkStack(ItemStack stack) {
		if (stack.isEmpty()) {
			throw new IllegalArgumentException("Cannot add empty stack");
		}

		if (stack.getCount() != 1) {
			throw new IllegalArgumentException("Stack size must be exactly 1 for stack: " + stack);
		}
	}
}
