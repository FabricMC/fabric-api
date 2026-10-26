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

package net.fabricmc.fabric.test.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

public class BlockGameTest {
	private void setupFluidFlowBox(GameTestHelper helper, BlockPos center, LiquidBlock fluidBlock, Block baseBlock) {
		helper.setBlock(center, Blocks.GRASS_BLOCK);

		for (Direction d : Direction.Plane.HORIZONTAL) {
			helper.setBlock(center.relative(d), baseBlock);
			helper.setBlock(center.above().relative(d), Blocks.AIR);
		}

		helper.setBlock(center.above(), fluidBlock);
	}

	private void assertFluidFlowInteraction(GameTestHelper helper, int ticksToDelay, BlockPos center, Block toCheck) {
		helper.runAfterDelay(ticksToDelay, () -> {
			for (Direction d : Direction.Plane.HORIZONTAL) {
				helper.assertBlockPresent(toCheck, center.above().relative(d));
			}

			helper.succeed();
		});
	}

	@GameTest(maxTicks = 40)
	public void flowingTestFluidCreatesIceAboveBlueIce(GameTestHelper helper) {
		BlockPos center = BlockPos.ZERO.above();
		setupFluidFlowBox(helper, center, BlockTest.TEST_FLUID_BLOCK, Blocks.BLUE_ICE);
		assertFluidFlowInteraction(helper, 20, center, Blocks.ICE);
	}

	@GameTest(maxTicks = 60)
	public void flowingTestFluidCreatesIceAfterFlowAboveBlueIce(GameTestHelper helper) {
		BlockPos center = BlockPos.ZERO.above();
		setupFluidFlowBox(helper, center, BlockTest.TEST_FLUID_BLOCK, Blocks.GRASS_BLOCK);

		helper.runAfterDelay(20, () -> {
			for (Direction d : Direction.Plane.HORIZONTAL) {
				helper.setBlock(center.relative(d), Blocks.BLUE_ICE);
			}
		});

		assertFluidFlowInteraction(helper, 40, center, Blocks.ICE);
	}

	@GameTest(maxTicks = 40)
	public void flowingTestFluidDoesNotFlowAboveMagma(GameTestHelper helper) {
		// Checks returning false with no set block doesn't try to flow again
		BlockPos center = BlockPos.ZERO.above();
		setupFluidFlowBox(helper, center, BlockTest.TEST_FLUID_BLOCK, Blocks.MAGMA_BLOCK);
		assertFluidFlowInteraction(helper, 20, center, Blocks.AIR);
	}

	@GameTest(maxTicks = 60)
	public void flowingTestFluidDoesNotEvaporateOnMagmaBlockPlace(GameTestHelper helper) {
		// Specifically checks LiquidBlock#neighborChanged shouldSpreadLiquid call

		BlockPos center = BlockPos.ZERO.above();
		setupFluidFlowBox(helper, center, BlockTest.TEST_FLUID_BLOCK, Blocks.GRASS_BLOCK);

		helper.runAfterDelay(10, () -> {
			for (Direction d : Direction.Plane.HORIZONTAL) {
				helper.setBlock(center.relative(d), Blocks.MAGMA_BLOCK);
			}
		});

		helper.runAfterDelay(20, () -> {
			for (Direction d : Direction.Plane.HORIZONTAL) {
				helper.setBlock(center.above().relative(d, 2), Blocks.AIR);
			}
		});

		// Since our handler doesn't set the block to air or anything, the fluid still exists above the magma but will not flow.
		// We are specifically checking that flow is prevented into the new air blocks.
		helper.runAfterDelay(39, () -> {
			for (Direction d : Direction.Plane.HORIZONTAL) {
				helper.assertBlockPresent(Blocks.AIR, center.above().relative(d, 2));
			}
		});

		assertFluidFlowInteraction(helper, 40, center, BlockTest.TEST_FLUID_BLOCK);
	}

	@GameTest(maxTicks = 60)
	public void flowingTestFluidDoesNotEvaporateAboveMagmaOnLiquidBlockPlace(GameTestHelper helper) {
		// Specifically checks LiquidBlock#onPlace shouldSpreadLiquid call

		BlockPos center = BlockPos.ZERO.above();

		helper.setBlock(center, Blocks.MAGMA_BLOCK);

		for (Direction d : Direction.Plane.HORIZONTAL) {
			helper.setBlock(center.above().relative(d), Blocks.AIR);
		}

		helper.setBlock(center.above(), BlockTest.TEST_FLUID_BLOCK);

		// Since our handler doesn't set the block to air or anything, the fluid still exists above the magma but will not flow.
		// We are specifically checking that flow is prevented into the existing air blocks.
		helper.runAfterDelay(10, () -> {
			helper.assertBlockPresent(BlockTest.TEST_FLUID_BLOCK, center.above());

			for (Direction d : Direction.Plane.HORIZONTAL) {
				helper.assertBlockPresent(Blocks.AIR, center.above().relative(d, 1));
			}

			helper.succeed();
		});
	}

	@GameTest(maxTicks = 60)
	public void flowingTestFluidReplacesWaterWithBlackStone(GameTestHelper helper) {
		// Specifically checks that inject into LavaFluid#spreadTo works, as TestFluid extends LavaFluid
		BlockPos center = BlockPos.ZERO.above();
		setupFluidFlowBox(helper, center, ((LiquidBlock) BlockTest.TEST_FLUID_BLOCK), Blocks.WATER);
		assertFluidFlowInteraction(helper, 40, center.below(), Blocks.BLACKSTONE);
	}

	// Double check vanilla behavior
	@GameTest(maxTicks = 100)
	public void flowingLavaReplacesWaterWithStone(GameTestHelper helper) {
		BlockPos center = BlockPos.ZERO.above();
		setupFluidFlowBox(helper, center, ((LiquidBlock) Blocks.LAVA), Blocks.WATER);
		assertFluidFlowInteraction(helper, 80, center.below(), Blocks.STONE);
	}

	@GameTest(maxTicks = 40)
	public void flowingWaterReplacesLavaSourceWithObsidian(GameTestHelper helper) {
		BlockPos center = BlockPos.ZERO.above();
		setupFluidFlowBox(helper, center, ((LiquidBlock) Blocks.WATER), Blocks.LAVA);
		assertFluidFlowInteraction(helper, 20, center.below(), Blocks.OBSIDIAN);
	}

	@GameTest(maxTicks = 120)
	public void flowingWaterReplacesFlowingLavaWithCobblestone(GameTestHelper helper) {
		BlockPos center = BlockPos.ZERO.above();
		setupFluidFlowBox(helper, center, ((LiquidBlock) Blocks.WATER), Blocks.AIR);

		helper.setBlock(center, Blocks.GRASS_BLOCK);

		for (Direction d : Direction.Plane.HORIZONTAL) {
			helper.setBlock(center.above().relative(d), Blocks.AIR);
			helper.setBlock(center.above().relative(d, 2), Blocks.AIR);
			helper.setBlock(center.above().relative(d, 3), Blocks.LAVA);
		}

		helper.runAfterDelay(100, () -> {
			for (Direction d : Direction.Plane.HORIZONTAL) {
				helper.assertBlockPresent(Blocks.COBBLESTONE, center.above().relative(d, 2));
			}

			helper.succeed();
		});
	}

	@GameTest(maxTicks = 120)
	public void flowingLavaAboveSoulSoilAndNextToBlueIceCreatesBasalt(GameTestHelper helper) {
		BlockPos center = BlockPos.ZERO.above();
		setupFluidFlowBox(helper, center, ((LiquidBlock) Blocks.LAVA), Blocks.SOUL_SOIL);

		for (Direction d : Direction.Plane.HORIZONTAL) {
			helper.setBlock(center.above().relative(d, 2), Blocks.BLUE_ICE);
		}

		assertFluidFlowInteraction(helper, 100, center, Blocks.BASALT);
	}
}
