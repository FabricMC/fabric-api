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

package net.fabricmc.fabric.test.loot;

import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

public final class LootGameTest {
	static int inlineLootTablesSeen = 0;

	@GameTest
	public void testReplace(TestContext context) {
		// Black wool should drop an iron ingot
		LootTableDrops drops = LootTableDrops.block(context, Blocks.BLACK_WOOL).drop();
		drops.assertEquals(new ItemStack(Items.IRON_INGOT));
		context.complete();
	}

	@GameTest
	public void testAddingPools(TestContext context) {
		// White wool should drop a white wool and a gold ingot
		LootTableDrops drops = LootTableDrops.block(context, Blocks.WHITE_WOOL).drop();
		drops.assertContains(new ItemStack(Items.WHITE_WOOL));
		ItemStack goldIngot = new ItemStack(Items.GOLD_INGOT);
		goldIngot.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Gold from White Wool"));
		drops.assertContains(goldIngot);
		context.complete();
	}

	@GameTest
	public void testModifyingPools(TestContext context) {
		// Yellow wool should drop either yellow wool or emeralds.
		// Let's generate the drops with specific seeds to check.
		LootTableDrops emeraldDrops = LootTableDrops.block(context, Blocks.YELLOW_WOOL).seed(1).drop();
		emeraldDrops.assertEquals(new ItemStack(Items.EMERALD));
		LootTableDrops woolDrops = LootTableDrops.block(context, Blocks.YELLOW_WOOL).seed(490234).drop();
		woolDrops.assertEquals(new ItemStack(Items.YELLOW_WOOL));
		context.complete();
	}

	@GameTest
	public void testRegistryAccess(TestContext context) {
		// Salmons should drop an enchanted fishing rod.
		ItemStack expected = new ItemStack(Items.FISHING_ROD);
		RegistryEntry<Enchantment> lure = context.getWorld()
				.getRegistryManager()
				.getEntryOrThrow(Enchantments.LURE);
		EnchantmentHelper.apply(expected, builder -> builder.set(lure, 1));

		LootTableDrops drops = LootTableDrops.entity(context, EntityType.SALMON).drop();
		drops.assertContains(expected);
		context.complete();
	}

	@GameTest
	public void testModifyDropsSmelting(TestContext context) {
		// Mining smeltable blocks using a diamond pickaxe should smelt the drops,
		// so copper ore should drop a copper ingot.
		ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
		LootTableDrops drops = LootTableDrops.block(context, Blocks.COPPER_ORE)
				.set(LootContextParameters.TOOL, tool)
				.drop();
		drops.assertEquals(new ItemStack(Items.COPPER_INGOT));
		context.complete();
	}

	@GameTest
	public void testModifyDropsDoubling(TestContext context) {
		// Red banners should drop two red banners
		LootTableDrops drops = LootTableDrops.block(context, Blocks.RED_BANNER).drop();
		drops.assertTotalCount(2);
		context.complete();
	}

	@GameTest
	public void testInlineTableModifyDrops(TestContext context) {
		int seenAtStart = inlineLootTablesSeen;
		MinecraftServer server = context.getWorld().getServer();
		server.getCommandManager().executeWithPrefix(server.getCommandSource(), "loot spawn 0 0 0 loot {\"pools\":[{\"entries\":[], \"rolls\":1.0}]}");
		int seenAtEnd = inlineLootTablesSeen;

		context.assertTrue(seenAtStart < seenAtEnd, Text.literal("inline loot table should've been processed by MODIFY_DROPS"));
		context.complete();
	}
}
