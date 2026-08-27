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

package net.fabricmc.fabric.test.item.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

public class CustomBlockTransformsGameTest {
	@GameTest
	public void testAxe(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		ItemStack axe = Items.DIAMOND_AXE.getDefaultInstance();
		player.setItemInHand(InteractionHand.MAIN_HAND, axe);

		BlockPos axeTestPos = new BlockPos(0, 1, 0);
		helper.setBlock(axeTestPos, Blocks.WOOL_STAIRS.white());
		helper.useBlock(axeTestPos, player);
		helper.assertBlockPresent(Blocks.WOOL_SLAB.white(), axeTestPos);
		helper.succeed();
	}

	@GameTest
	public void testHoe(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		ItemStack hoe = Items.DIAMOND_HOE.getDefaultInstance();
		player.setItemInHand(InteractionHand.MAIN_HAND, hoe);

		BlockPos hoeTest1Pos = new BlockPos(0, 1, 0);
		helper.setBlock(hoeTest1Pos, Blocks.BAMBOO_MOSAIC);
		helper.useBlock(hoeTest1Pos, player);
		helper.assertBlockState(hoeTest1Pos, Blocks.BAMBOO_MOSAIC_SLAB.defaultBlockState().setValue(
				BlockStateProperties.SLAB_TYPE, SlabType.TOP
		));

		BlockPos hoeTest2Pos = new BlockPos(1, 2, 0);
		BlockPos hoeTest2BedrockPos = hoeTest2Pos.below();
		helper.setBlock(hoeTest2Pos, Blocks.COBBLESTONE);
		helper.setBlock(hoeTest2BedrockPos, Blocks.BEDROCK);
		helper.useBlock(hoeTest2Pos, player);
		helper.assertBlockPresent(Blocks.PACKED_ICE, hoeTest2Pos);

		BlockPos hoeTest3Pos = hoeTest2Pos.above();
		helper.setBlock(hoeTest3Pos, Blocks.COBBLESTONE);
		helper.useBlock(hoeTest3Pos, player);
		helper.assertBlockNotPresent(Blocks.PACKED_ICE, hoeTest3Pos);

		helper.succeed();
	}

	@GameTest
	public void testShovel(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		ItemStack shovel = Items.DIAMOND_SHOVEL.getDefaultInstance();
		player.setItemInHand(InteractionHand.MAIN_HAND, shovel);

		BlockPos shovelTest1Pos = new BlockPos(0, 1, 0);
		helper.setBlock(shovelTest1Pos, Blocks.ACACIA_STAIRS);
		helper.useBlock(shovelTest1Pos, player);
		helper.assertBlockPresent(Blocks.PALE_OAK_STAIRS, shovelTest1Pos);

		BlockPos shovelTest2Pos = new BlockPos(1, 1, 0);
		helper.setBlock(shovelTest2Pos, Blocks.BIRCH_STAIRS);
		helper.useBlock(shovelTest2Pos, player);
		helper.assertBlockPresent(Blocks.PALE_OAK_STAIRS, shovelTest2Pos);

		BlockPos shovelTest3Pos = new BlockPos(2, 1, 0);
		helper.setBlock(shovelTest3Pos, Blocks.DRIED_KELP_BLOCK);
		helper.useBlock(shovelTest3Pos, player);
		helper.assertBlockPresent(Blocks.DEAD_BRAIN_CORAL_BLOCK, shovelTest3Pos);

		helper.succeed();
	}

	@GameTest
	public void testPickaxe(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		ItemStack pickaxe = Items.DIAMOND_PICKAXE.getDefaultInstance();
		player.setItemInHand(InteractionHand.MAIN_HAND, pickaxe);

		BlockPos pickaxeTest1Pos = new BlockPos(0, 1, 0);
		helper.setBlock(pickaxeTest1Pos, Blocks.STONE_BRICKS);
		helper.useBlock(pickaxeTest1Pos, player);
		helper.assertBlockPresent(Blocks.CRACKED_STONE_BRICKS, pickaxeTest1Pos);

		BlockPos pickaxeTest2Pos = new BlockPos(1, 1, 0);
		helper.setBlock(pickaxeTest2Pos, Blocks.NETHER_BRICKS);
		helper.useBlock(pickaxeTest2Pos, player);
		helper.assertBlockPresent(Blocks.CRACKED_NETHER_BRICKS, pickaxeTest2Pos);

		helper.succeed();
	}
}
