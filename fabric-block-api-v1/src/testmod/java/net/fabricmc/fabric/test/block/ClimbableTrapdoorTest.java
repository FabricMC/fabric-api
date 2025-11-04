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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.fabricmc.api.ModInitializer;

// Registers two blocks that can be used to test the fabric:can_climb_trapdoor_above tag.
// - custom_ladder: a custom LadderBlock. You should be able to climb an open trapdoor above this block
//   when they're placed on the same side of the wall.
// - custom_non_ladder: a custom block that is *not* a LadderBlock. You should always be able to climb a trapdoor above
//   this block.
public final class ClimbableTrapdoorTest implements ModInitializer {
	private static final String MOD_ID = "fabric-block-api-v1-testmod";

	public static Block customLadderBlock;
	public static Block customNonLadderBlock;

	@Override
	public void onInitialize() {
		customLadderBlock = registerBlock("custom_ladder", settings -> new LadderBlock(settings) { });
		customNonLadderBlock = registerBlock("custom_non_ladder", NonLadderBlock::new);
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
		protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
			return Shapes.empty();
		}
	}
}
