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

package net.fabricmc.fabric.test.creativemodetab;

import com.google.common.base.Supplier;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;

public class CreativeModeTabTest implements ModInitializer {
	private static final String MOD_ID = "fabric-creative-mode-tab-v1-testmod";
	private static final ResourceKey<Item> ITEM_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "item_test_group"));
	private static final Item TEST_ITEM = new Item(new Item.Properties().setId(ITEM_KEY));

	private static final ResourceKey<CreativeModeTab> ITEM_GROUP = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MOD_ID, "test_group"));

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath("fabric-creative-mode-tab-v1-testmod", "item_test_group"), TEST_ITEM);

		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ITEM_GROUP, FabricCreativeModeTab.builder()
				.title(Component.literal("Test Creative Mode Tab"))
				.icon(() -> new ItemStack(Items.DIAMOND))
				.displayItems((context, entries) -> {
					entries.acceptAll(BuiltInRegistries.ITEM.stream()
							.map(ItemStack::new)
							.filter(input -> !input.isEmpty())
							.toList());
				})
				.build());

		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS).register((content) -> {
			content.accept(TEST_ITEM);

			content.insertBefore(Blocks.OAK_FENCE, Items.DIAMOND, Items.DIAMOND_BLOCK);
			content.insertAfter(Blocks.OAK_DOOR, Items.EMERALD, Items.EMERALD_BLOCK);

			// Test adding when the existing entry does not exist.
			content.insertBefore(Blocks.BEDROCK, Items.GOLD_INGOT, Items.GOLD_BLOCK);
			content.insertAfter(Blocks.BEDROCK, Items.IRON_INGOT, Items.IRON_BLOCK);
		});

		// Add a differently damaged pickaxe to all groups
		CreativeModeTabEvents.MODIFY_OUTPUT_ALL.register((group, content) -> {
			if (group.getIconItem() == ItemStack.EMPTY) { // Leave the empty groups empty
				return;
			}

			ItemStack minDmgPickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
			minDmgPickaxe.setDamageValue(1);
			content.prepend(minDmgPickaxe);

			ItemStack maxDmgPickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
			maxDmgPickaxe.setDamageValue(maxDmgPickaxe.getMaxDamage() - 1);
			content.accept(maxDmgPickaxe);
		});

		// Regression test for #3566
		for (int j = 0; j < 20; j++) {
			Registry.register(
					BuiltInRegistries.CREATIVE_MODE_TAB,
					Identifier.fromNamespaceAndPath(MOD_ID, "empty_tab_" + j),
					FabricCreativeModeTab.builder()
							.title(Component.literal("Empty Creative Mode Tab: " + j))
							.build()
			);
		}

		for (int i = 0; i < 100; i++) {
			final int index = i;

			Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MOD_ID, "test_group_" + i), FabricCreativeModeTab.builder()
					.title(Component.literal("Test Creative Mode Tab: " + i))
					.icon((Supplier<ItemStack>) () -> new ItemStack(BuiltInRegistries.BLOCK.byId(index)))
					.displayItems((context, output) -> {
						var itemStack = new ItemStack(BuiltInRegistries.ITEM.byId(index));

						if (!itemStack.isEmpty()) {
							output.accept(itemStack);
						}
					})
					.build());
		}

		try {
			// Test to make sure that creative mode tabs must have a display name.
			FabricCreativeModeTab.builder().build();
			throw new AssertionError();
		} catch (IllegalStateException ignored) {
			// Ignored
		}
	}
}
