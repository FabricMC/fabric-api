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

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.impl.creativetab.CreativeModeTabEventsImpl;

/**
 * Holds events related to {@link CreativeModeTabs}.
 */
public final class CreativeModeTabEvents {
	private CreativeModeTabEvents() {
	}

	/**
	 * This event allows the output of any creative mode tab to be modified.
	 *
	 * <p>Use {@link #modifyOutputEvent(ResourceKey)} to get the event for a specific creative mode tab.
	 *
	 * <p>This event is invoked after those two more specific events.
	 */
	public static final Event<ModifyOutputAll> MODIFY_OUTPUT_ALL = EventFactory.createArrayBacked(ModifyOutputAll.class, callbacks -> (tab, output) -> {
		for (ModifyOutputAll callback : callbacks) {
			callback.modifyOutput(tab, output);
		}
	});

	/**
	 * Returns the modify output event for a specific creative mode tab. This uses the tab's ID and
	 * is suitable for modifying a modded creative mode tab that might not exist.
	 * @param resourceKey the {@link ResourceKey} of the creative mode tab to modify
	 * @return the event
	 */
	public static Event<ModifyOutput> modifyOutputEvent(ResourceKey<CreativeModeTab> resourceKey) {
		return CreativeModeTabEventsImpl.getOrCreateModifyOutputEvent(resourceKey);
	}

	@FunctionalInterface
	public interface ModifyOutput {
		/**
		 * Modifies the creative mode tab output.
		 * @param output the output
		 * @see FabricCreativeModeTabOutput
		 */
		void modifyOutput(FabricCreativeModeTabOutput output);
	}

	@FunctionalInterface
	public interface ModifyOutputAll {
		/**
		 * Modifies the creative mode tab output.
		 * @param tab the creative mode tab that is being modified
		 * @param output the output
		 * @see FabricCreativeModeTabOutput
		 */
		void modifyOutput(CreativeModeTab tab, FabricCreativeModeTabOutput output);
	}
}
