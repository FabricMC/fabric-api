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

package net.fabricmc.fabric.mixin.creativetab;

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

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.creativetab.CreativeModeTabEventsImpl;
import net.fabricmc.fabric.impl.creativetab.FabricCreativeModeTabImpl;

@Mixin(CreativeModeTab.class)
abstract class CreativeModeTabMixin implements FabricCreativeModeTabImpl {
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
		final ResourceKey<CreativeModeTab> resourceKey = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(self).orElseThrow(() -> new IllegalStateException("Unregistered creative mode tab : " + self));

		// Do not modify special creative mode tabs (except Game Master Blocks) at all.
		// Special creative mode tabs include Saved Hotbars, Search, and Survival Inventory.
		// Note, search gets modified as part of the parent creative mode tab.
		if (self.isAlignedRight() && resourceKey != CreativeModeTabs.OP_BLOCKS) return;

		// Sanity check for the injection point. It should be after these fields are set.
		Objects.requireNonNull(displayItems, "displayStacks");
		Objects.requireNonNull(displayItemsSearchTab, "searchTabStacks");

		// Convert the entries to lists
		var mutableDisplayStacks = new LinkedList<>(displayItems);
		var mutableSearchTabStacks = new LinkedList<>(displayItemsSearchTab);
		var entries = new FabricCreativeModeTabOutput(context, mutableDisplayStacks, mutableSearchTabStacks);

		// Now trigger the events
		if (resourceKey != CreativeModeTabs.OP_BLOCKS || context.hasPermissions()) {
			final Event<CreativeModeTabEvents.ModifyOutput> modifyEntriesEvent = CreativeModeTabEventsImpl.getModifyOutputEvent(resourceKey);

			if (modifyEntriesEvent != null) {
				modifyEntriesEvent.invoker().modifyOutput(entries);
			}

			CreativeModeTabEvents.MODIFY_OUTPUT_ALL.invoker().modifyOutput(self, entries);
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
			throw new IllegalStateException("Creative mode tab has no page");
		}

		return page;
	}

	@Override
	public void fabric_setPage(int page) {
		this.page = page;
	}
}
