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

import static net.minecraft.world.item.CreativeModeTabs.BUILDING_BLOCKS;
import static net.minecraft.world.item.CreativeModeTabs.COLORED_BLOCKS;
import static net.minecraft.world.item.CreativeModeTabs.COMBAT;
import static net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS;
import static net.minecraft.world.item.CreativeModeTabs.FUNCTIONAL_BLOCKS;
import static net.minecraft.world.item.CreativeModeTabs.HOTBAR;
import static net.minecraft.world.item.CreativeModeTabs.INGREDIENTS;
import static net.minecraft.world.item.CreativeModeTabs.INVENTORY;
import static net.minecraft.world.item.CreativeModeTabs.NATURAL_BLOCKS;
import static net.minecraft.world.item.CreativeModeTabs.OP_BLOCKS;
import static net.minecraft.world.item.CreativeModeTabs.REDSTONE_BLOCKS;
import static net.minecraft.world.item.CreativeModeTabs.SEARCH;
import static net.minecraft.world.item.CreativeModeTabs.SPAWN_EGGS;
import static net.minecraft.world.item.CreativeModeTabs.TOOLS_AND_UTILITIES;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

import net.fabricmc.fabric.impl.itemgroup.FabricItemGroupImpl;

@Mixin(CreativeModeTabs.class)
public class CreativeModeTabsMixin {
	@Unique
	private static final int TABS_PER_PAGE = FabricItemGroupImpl.TABS_PER_PAGE;

	@Inject(method = "validate", at = @At("HEAD"), cancellable = true)
	private static void deferDuplicateCheck(CallbackInfo ci) {
		/*
		 * Defer the duplication checks to when fabric performs them (see mixin below).
		 * It is preserved just in case, but fabric's pagination logic should prevent any from happening anyway.
		 */
		ci.cancel();
	}

	@Inject(method = "buildAllTabContents", at = @At("TAIL"))
	private static void paginateGroups(CallbackInfo ci) {
		final List<ResourceKey<CreativeModeTab>> vanillaGroups = List.of(BUILDING_BLOCKS, COLORED_BLOCKS, NATURAL_BLOCKS, FUNCTIONAL_BLOCKS, REDSTONE_BLOCKS, HOTBAR, SEARCH, TOOLS_AND_UTILITIES, COMBAT, FOOD_AND_DRINKS, INGREDIENTS, SPAWN_EGGS, OP_BLOCKS, INVENTORY);

		int count = 0;

		Comparator<Holder.Reference<CreativeModeTab>> entryComparator = (e1, e2) -> {
			// Non-displayable groups should come last for proper pagination
			int displayCompare = Boolean.compare(e1.value().shouldDisplay(), e2.value().shouldDisplay());

			if (displayCompare != 0) {
				return -displayCompare;
			} else {
				// Ensure a deterministic order
				return compareNamespaceFirst(e1.key().identifier(), e2.key().identifier());
			}
		};
		final List<Holder.Reference<CreativeModeTab>> sortedItemGroups = BuiltInRegistries.CREATIVE_MODE_TAB.listElements()
				.sorted(entryComparator)
				.toList();

		for (Holder.Reference<CreativeModeTab> reference : sortedItemGroups) {
			final CreativeModeTab itemGroup = reference.value();
			final FabricItemGroupImpl fabricItemGroup = (FabricItemGroupImpl) itemGroup;

			if (vanillaGroups.contains(reference.key())) {
				// Vanilla group goes on the first page.
				fabricItemGroup.fabric_setPage(0);
				continue;
			}

			final CreativeModeTabAccessor itemGroupAccessor = (CreativeModeTabAccessor) itemGroup;
			fabricItemGroup.fabric_setPage((count / TABS_PER_PAGE) + 1);
			int pageIndex = count % TABS_PER_PAGE;
			CreativeModeTab.Row row = pageIndex < (TABS_PER_PAGE / 2) ? CreativeModeTab.Row.TOP : CreativeModeTab.Row.BOTTOM;
			itemGroupAccessor.setRow(row);
			itemGroupAccessor.setColumn(row == CreativeModeTab.Row.TOP ? pageIndex % TABS_PER_PAGE : (pageIndex - TABS_PER_PAGE / 2) % (TABS_PER_PAGE));

			count++;
		}

		// Overlapping group detection logic, with support for pages.
		record ItemGroupPosition(CreativeModeTab.Row row, int column, int page) { }
		var map = new HashMap<ItemGroupPosition, String>();

		for (ResourceKey<CreativeModeTab> registryKey : BuiltInRegistries.CREATIVE_MODE_TAB.registryKeySet()) {
			final CreativeModeTab itemGroup = BuiltInRegistries.CREATIVE_MODE_TAB.getValueOrThrow(registryKey);
			final FabricItemGroupImpl fabricItemGroup = (FabricItemGroupImpl) itemGroup;
			final String displayName = itemGroup.getDisplayName().getString();
			final var position = new ItemGroupPosition(itemGroup.row(), itemGroup.column(), fabricItemGroup.fabric_getPage());
			final String existingName = map.put(position, displayName);

			if (existingName != null) {
				throw new IllegalArgumentException("Duplicate position: (%s) for item groups %s vs %s".formatted(position, displayName, existingName));
			}
		}
	}

	// Identifier#compareTo checks the path first, but we want to check the namespace first so that groups added by the
	// same mod appear next to each other.
	@Unique
	private static int compareNamespaceFirst(Identifier a, Identifier b) {
		int c = a.getNamespace().compareTo(b.getNamespace());

		if (c != 0) {
			return c;
		}

		return a.getPath().compareTo(b.getPath());
	}
}
