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

import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.block.v1.FluidFlowEvents;

// Registers two blocks that can be used to test the fabric:can_climb_trapdoor_above tag.
// - custom_ladder: a custom LadderBlock. You should be able to climb an open trapdoor above this block
//   when they're placed on the same side of the wall.
// - custom_non_ladder: a custom block that is *not* a LadderBlock. You should always be able to climb a trapdoor above
//   this block.
public final class BlockTest implements ModInitializer {
	private static final String MOD_ID = "fabric-block-api-v1-testmod";

	public static Block customLadderBlock;
	public static Block customNonLadderBlock;

	public static final FlowingFluid TEST_FLUID = Registry.register(BuiltInRegistries.FLUID, Identifier.fromNamespaceAndPath(MOD_ID, "test_fluid"), new TestFluid.Source());
	public static final FlowingFluid TEST_FLUID_FLOWING = Registry.register(BuiltInRegistries.FLUID, Identifier.fromNamespaceAndPath(MOD_ID, "test_fluid_flowing"), new TestFluid.Flowing());
	public static final LiquidBlock TEST_FLUID_BLOCK = Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "test_fluid"), new LiquidBlock(TEST_FLUID, BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "test_fluid")))) {
	});

	public static final TagKey<Fluid> TEST_FLUID_KEY = TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath(MOD_ID, "test_fluid"));

	@Override
	public void onInitialize() {
		customLadderBlock = registerBlock("custom_ladder", settings -> new LadderBlock(settings) { });
		customNonLadderBlock = registerBlock("custom_non_ladder", NonLadderBlock::new);

		FluidFlowEvents.ALLOW.register((fluid, level, position) -> {
			// Check we are the test fluid
			if (!fluid.is(TEST_FLUID_KEY)) return true;

			if (level.getBlockState(position.below()).is(Blocks.BLUE_ICE)) { // Check we are above blue ice
				level.setBlock(position, Blocks.ICE.defaultBlockState(), Block.UPDATE_ALL);
				level.playSound(null, position, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 1, 1);
				return false;
			} else if (level.getBlockState(position.below()).is(Blocks.MAGMA_BLOCK)) { // Check we are above magma block
				level.playSound(null, position, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, 1);
				// Not updating the block state here would likely be a bug in a mod, but it allows us to test specific behavior in the game tests.
				return false;
			} else if (level.getFluidState(position).is(FluidTags.WATER)) { // Check we are in water
				level.setBlock(position, Blocks.BLACKSTONE.defaultBlockState(), Block.UPDATE_ALL);
				level.playSound(null, position, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1, 1);
				return false;
			}

			return true;
		});
	}

	private static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> blockFactory) {
		Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, name);
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
		Block block = blockFactory.apply(BlockBehaviour.Properties.of().noOcclusion().setId(blockKey));
		Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
		Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(block, new Item.Properties().setId(itemKey)));
		return block;
	}

	private static final class NonLadderBlock extends Block {
		NonLadderBlock(Properties settings) {
			super(settings);
		}

		@Override
		protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
			return Shapes.empty();
		}
	}
}
