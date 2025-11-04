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

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

public final class LootGameTest {
	static int inlineLootTablesSeen = 0;

	@GameTest
	public void testReplace(GameTestHelper context) {
		// Black wool should drop an iron ingot
		LootTableDrops drops = LootTableDrops.block(context, Blocks.BLACK_WOOL).drop();
		drops.assertEquals(new ItemStack(Items.IRON_INGOT));
		context.succeed();
	}

	@GameTest
	public void testAddingPools(GameTestHelper context) {
		// White wool should drop a white wool and a gold ingot
		LootTableDrops drops = LootTableDrops.block(context, Blocks.WHITE_WOOL).drop();
		drops.assertContains(new ItemStack(Items.WHITE_WOOL));
		ItemStack goldIngot = new ItemStack(Items.GOLD_INGOT);
		goldIngot.set(DataComponents.CUSTOM_NAME, Component.literal("Gold from White Wool"));
		drops.assertContains(goldIngot);
		context.succeed();
	}

	@GameTest
	public void testModifyingPools(GameTestHelper context) {
		// Yellow wool should drop either yellow wool or emeralds.
		// Let's generate the drops with specific seeds to check.
		LootTableDrops emeraldDrops = LootTableDrops.block(context, Blocks.YELLOW_WOOL).seed(1).drop();
		emeraldDrops.assertEquals(new ItemStack(Items.EMERALD));
		LootTableDrops woolDrops = LootTableDrops.block(context, Blocks.YELLOW_WOOL).seed(490234).drop();
		woolDrops.assertEquals(new ItemStack(Items.YELLOW_WOOL));
		context.succeed();
	}

	@GameTest
	public void testRegistryAccess(GameTestHelper context) {
		// Salmons should drop an enchanted fishing rod.
		ItemStack expected = new ItemStack(Items.FISHING_ROD);
		Holder<Enchantment> lure = context.getLevel()
				.registryAccess()
				.getOrThrow(Enchantments.LURE);
		EnchantmentHelper.updateEnchantments(expected, builder -> builder.set(lure, 1));

		LootTableDrops drops = LootTableDrops.entity(context, EntityType.SALMON).drop();
		drops.assertContains(expected);
		context.succeed();
	}

	@GameTest
	public void testModifyDropsSmelting(GameTestHelper context) {
		// Mining smeltable blocks using a diamond pickaxe should smelt the drops,
		// so copper ore should drop a copper ingot.
		ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
		LootTableDrops drops = LootTableDrops.block(context, Blocks.COPPER_ORE)
				.set(LootContextParams.TOOL, tool)
				.drop();
		drops.assertEquals(new ItemStack(Items.COPPER_INGOT));
		context.succeed();
	}

	@GameTest
	public void testModifyDropsDoubling(GameTestHelper context) {
		// Red banners should drop two red banners
		LootTableDrops drops = LootTableDrops.block(context, Blocks.RED_BANNER).drop();
		drops.assertTotalCount(2);
		context.succeed();
	}

	@GameTest
	public void testInlineTableModifyDrops(GameTestHelper context) {
		int seenAtStart = inlineLootTablesSeen;
		MinecraftServer server = context.getLevel().getServer();
		server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "loot spawn 0 0 0 loot {\"pools\":[{\"entries\":[], \"rolls\":1.0}]}");
		int seenAtEnd = inlineLootTablesSeen;

		context.assertTrue(seenAtStart < seenAtEnd, Component.literal("inline loot table should've been processed by MODIFY_DROPS"));
		context.succeed();
	}
}
