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

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

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
			BuiltInRegistries.BLOCK.addAlias(OLD_TEST_BLOCK, TEST_BLOCK);
			BuiltInRegistries.ITEM.addAlias(OLD_TEST_BLOCK, TEST_BLOCK);
			BuiltInRegistries.ITEM.addAlias(OLD_TEST_INGOT, TEST_INGOT);
		}

		BuiltInRegistries.ITEM.addAlias(Identifier.parse("old_stone"), Identifier.parse("stone"));
	}

	private static void register(Identifier blockId, Identifier itemId) {
		Block block = new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, blockId)));
		Registry.register(BuiltInRegistries.BLOCK, blockId, block);
		BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, blockId)));
		Registry.register(BuiltInRegistries.ITEM, blockId, blockItem);
		Item item = new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, itemId)));
		Registry.register(BuiltInRegistries.ITEM, itemId, item);
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("registry_sync_alias_test", path);
	}
}
