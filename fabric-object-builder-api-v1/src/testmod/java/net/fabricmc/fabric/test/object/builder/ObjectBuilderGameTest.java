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

package net.fabricmc.fabric.test.object.builder;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

public class ObjectBuilderGameTest {
	@GameTest
	public void testBlockUse(GameTestHelper helper) {
		List<Block> blocks = List.of(BlockEntityTypeBuilderTest.INITIAL_BETRAYAL_BLOCK, BlockEntityTypeBuilderTest.ADDED_BETRAYAL_BLOCK, BlockEntityTypeBuilderTest.FIRST_MULTI_BETRAYAL_BLOCK, BlockEntityTypeBuilderTest.SECOND_MULTI_BETRAYAL_BLOCK);
		BlockPos.MutableBlockPos pos = BlockPos.ZERO.above().mutable();

		for (Block block : blocks) {
			helper.setBlock(pos, block);
			helper.useBlock(pos);
			pos.above();
		}

		helper.succeed();
	}
}
