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

package net.fabricmc.fabric.impl.registry.sync.trackers.vanilla;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;

public final class BlockItemTracker implements RegistryEntryAddedCallback<Item> {
	private BlockItemTracker() { }

	public static void register(Registry<Item> registry) {
		BlockItemTracker tracker = new BlockItemTracker();
		RegistryEntryAddedCallback.event(registry).register(tracker);
	}

	@Override
	public void onEntryAdded(int rawId, Identifier id, Item object) {
		if (object instanceof BlockItem) {
			((BlockItem) object).registerBlocks(Item.BY_BLOCK, object);
		}
	}
}
