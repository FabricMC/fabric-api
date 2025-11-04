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

package net.fabricmc.fabric.api.itemgroup.v1;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.impl.itemgroup.ItemGroupEventsImpl;

/**
 * Holds events related to {@link CreativeModeTabs}.
 */
public final class ItemGroupEvents {
	private ItemGroupEvents() {
	}

	/**
	 * This event allows the entries of any item group to be modified.
	 * <p/>
	 * Use {@link #modifyEntriesEvent(ResourceKey)} to get the event for a specific item group.
	 * <p/>
	 * This event is invoked after those two more specific events.
	 */
	public static final Event<ModifyEntriesAll> MODIFY_ENTRIES_ALL = EventFactory.createArrayBacked(ModifyEntriesAll.class, callbacks -> (group, entries) -> {
		for (ModifyEntriesAll callback : callbacks) {
			callback.modifyEntries(group, entries);
		}
	});

	/**
	 * Returns the modify entries event for a specific item group. This uses the group ID and
	 * is suitable for modifying a modded item group that might not exist.
	 * @param registryKey the {@link ResourceKey} of the item group to modify
	 * @return the event
	 */
	public static Event<ModifyEntries> modifyEntriesEvent(ResourceKey<CreativeModeTab> registryKey) {
		return ItemGroupEventsImpl.getOrCreateModifyEntriesEvent(registryKey);
	}

	@FunctionalInterface
	public interface ModifyEntries {
		/**
		 * Modifies the item group entries.
		 * @param entries the entries
		 * @see FabricItemGroupEntries
		 */
		void modifyEntries(FabricItemGroupEntries entries);
	}

	@FunctionalInterface
	public interface ModifyEntriesAll {
		/**
		 * Modifies the item group entries.
		 * @param group the item group that is being modified
		 * @param entries the entries
		 * @see FabricItemGroupEntries
		 */
		void modifyEntries(CreativeModeTab group, FabricItemGroupEntries entries);
	}
}
