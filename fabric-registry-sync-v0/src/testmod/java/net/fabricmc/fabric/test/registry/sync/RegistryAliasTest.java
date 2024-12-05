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

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;

public class RegistryAliasTest implements ModInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final boolean USE_OLD_IDS = Boolean.parseBoolean(System.getProperty("fabric.registry.sync.test.alias.use_old_ids", "true"));
	public static final Identifier OLD_TEST_INGOT = id("test_ingot_old");
	public static final Identifier TEST_INGOT = id("test_ingot");
	public static final Identifier OLD_TEST_BLOCK = id("test_block_old");
	public static final Identifier TEST_BLOCK = id("test_block");

	@Override
	public void onInitialize() {
		if (USE_OLD_IDS) {
			LOGGER.info("Registering old IDs");
			register(OLD_TEST_BLOCK, OLD_TEST_INGOT);
		} else {
			LOGGER.info("Registering new IDs");
			register(TEST_BLOCK, TEST_INGOT);
			LOGGER.info("Adding aliases");
			Registries.BLOCK.addAlias(OLD_TEST_BLOCK, TEST_BLOCK);
			Registries.ITEM.addAlias(OLD_TEST_BLOCK, TEST_BLOCK);
			Registries.ITEM.addAlias(OLD_TEST_INGOT, TEST_INGOT);
		}

		Registries.ITEM.addAlias(Identifier.of("old_stone"), Identifier.of("stone"));
	}

	private static void register(Identifier blockId, Identifier itemId) {
		Block block = new Block(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, blockId)));
		Registry.register(Registries.BLOCK, blockId, block);
		BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, blockId)));
		Registry.register(Registries.ITEM, blockId, blockItem);
		Item item = new Item(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, itemId)));
		Registry.register(Registries.ITEM, itemId, item);
	}

	private static Identifier id(String path) {
		return Identifier.of("registry_sync_alias_test", path);
	}
}
