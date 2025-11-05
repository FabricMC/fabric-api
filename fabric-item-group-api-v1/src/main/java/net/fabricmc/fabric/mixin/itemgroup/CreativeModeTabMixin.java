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

package net.fabricmc.fabric.mixin.itemgroup;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.impl.itemgroup.FabricItemGroupImpl;
import net.fabricmc.fabric.impl.itemgroup.ItemGroupEventsImpl;

@Mixin(CreativeModeTab.class)
abstract class CreativeModeTabMixin implements FabricItemGroupImpl {
	@Shadow
	private Collection<ItemStack> displayItems;

	@Shadow
	private Set<ItemStack> displayItemsSearchTab;

	@Unique
	private int page = -1;

	@SuppressWarnings("ConstantConditions")
	@Inject(method = "buildContents", at = @At("TAIL"))
	public void getStacks(CreativeModeTab.ItemDisplayParameters context, CallbackInfo ci) {
		final CreativeModeTab self = (CreativeModeTab) (Object) this;
		final ResourceKey<CreativeModeTab> registryKey = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(self).orElseThrow(() -> new IllegalStateException("Unregistered item group : " + self));

		// Do not modify special item groups (except Operator Blocks) at all.
		// Special item groups include Saved Hotbars, Search, and Survival Inventory.
		// Note, search gets modified as part of the parent item group.
		if (self.isAlignedRight() && registryKey != CreativeModeTabs.OP_BLOCKS) return;

		// Sanity check for the injection point. It should be after these fields are set.
		Objects.requireNonNull(displayItems, "displayStacks");
		Objects.requireNonNull(displayItemsSearchTab, "searchTabStacks");

		// Convert the entries to lists
		var mutableDisplayStacks = new LinkedList<>(displayItems);
		var mutableSearchTabStacks = new LinkedList<>(displayItemsSearchTab);
		var entries = new FabricItemGroupEntries(context, mutableDisplayStacks, mutableSearchTabStacks);

		// Now trigger the events
		if (registryKey != CreativeModeTabs.OP_BLOCKS || context.hasPermissions()) {
			final Event<ItemGroupEvents.ModifyEntries> modifyEntriesEvent = ItemGroupEventsImpl.getModifyEntriesEvent(registryKey);

			if (modifyEntriesEvent != null) {
				modifyEntriesEvent.invoker().modifyEntries(entries);
			}

			ItemGroupEvents.MODIFY_ENTRIES_ALL.invoker().modifyEntries(self, entries);
		}

		// Convert the stacks back to sets after the events had a chance to modify them
		displayItems.clear();
		displayItems.addAll(mutableDisplayStacks);

		displayItemsSearchTab.clear();
		displayItemsSearchTab.addAll(mutableSearchTabStacks);
	}

	@Override
	public int fabric_getPage() {
		if (page < 0) {
			throw new IllegalStateException("Item group has no page");
		}

		return page;
	}

	@Override
	public void fabric_setPage(int page) {
		this.page = page;
	}
}
