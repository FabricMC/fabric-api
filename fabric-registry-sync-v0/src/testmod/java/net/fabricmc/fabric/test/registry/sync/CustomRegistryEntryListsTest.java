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

package net.fabricmc.fabric.test.registry.sync;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.BlockTags;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.entrylists.DefaultCustomRegistryEntryLists;

public class CustomRegistryEntryListsTest implements ModInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();

	@Override
	public void onInitialize() {
		CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
			LOGGER.info("Starting custom registry list tests...");

			if (client) {
				return;
			}

			RegistryEntryList<Block> anvils = registries.getOrThrow(RegistryKeys.BLOCK).getOrThrow(BlockTags.ANVIL);
			RegistryEntryList<Block> corals = registries.getOrThrow(RegistryKeys.BLOCK).getOrThrow(BlockTags.CORAL_BLOCKS);
			RegistryEntry<Block> anvil = registries.getOrThrow(RegistryKeys.BLOCK).getEntry(Blocks.ANVIL);
			RegistryEntry<Block> chippedAnvil = registries.getOrThrow(RegistryKeys.BLOCK).getEntry(Blocks.CHIPPED_ANVIL);
			RegistryEntry<Block> coral = registries.getOrThrow(RegistryKeys.BLOCK).getEntry(Blocks.BRAIN_CORAL_BLOCK);
			RegistryEntry<Block> log = registries.getOrThrow(RegistryKeys.BLOCK).getEntry(Blocks.OAK_LOG);

			RegistryEntryList<Block> union = DefaultCustomRegistryEntryLists.union(
					anvils,
					corals
			);

			warnIfFalse(union.contains(anvil), "CustomRegistryEntryList.union doesn't contain anvil");
			warnIfFalse(union.contains(coral), "CustomRegistryEntryList.union doesn't contain brain coral");

			RegistryEntryList<Block> intersection = DefaultCustomRegistryEntryLists.intersection(
					anvils,
					corals
			);

			warnIfFalse(!intersection.contains(anvil), "CustomRegistryEntryList.intersection contains anvil");
			warnIfFalse(!intersection.contains(coral), "CustomRegistryEntryList.intersection contains brain coral");

			RegistryEntryList<Block> inverse = DefaultCustomRegistryEntryLists.inverse(
					registries.getOrThrow(RegistryKeys.BLOCK),
					union
			);

			warnIfFalse(!inverse.contains(anvil), "CustomRegistryEntryList.inverse contains anvil");
			warnIfFalse(!inverse.contains(coral), "CustomRegistryEntryList.inverse contains brain coral");
			warnIfFalse(inverse.contains(log), "CustomRegistryEntryList.inverse doesn't contain oak log");

			RegistryEntryList<Block> universal = DefaultCustomRegistryEntryLists.universal(
					registries.getOrThrow(RegistryKeys.BLOCK)
			);

			warnIfFalse(universal.contains(anvil), "CustomRegistryEntryList.universal doesn't contain anvil");
			warnIfFalse(universal.contains(coral), "CustomRegistryEntryList.universal doesn't contain brain coral");
			warnIfFalse(universal.contains(log), "CustomRegistryEntryList.universal doesn't contain oak log");

			RegistryEntryList<Block> subtraction = DefaultCustomRegistryEntryLists.subtraction(
					registries.getOrThrow(RegistryKeys.BLOCK),
					universal,
					RegistryEntryList.of(registries.getOrThrow(RegistryKeys.BLOCK).getEntry(Blocks.ANVIL))
			);

			warnIfFalse(!subtraction.contains(anvil), "CustomRegistryEntryList.subtraction contains anvil");
			warnIfFalse(subtraction.contains(chippedAnvil), "CustomRegistryEntryList.subtraction doesn't contain chipped anvil");

			// a future pr test serialization logic here.

			LOGGER.info("Finished custom registry list tests...");
		});
	}

	private static void warnIfFalse(boolean condition, String message) {
		if (!condition) {
			LOGGER.warn(message);
		}
	}
}
