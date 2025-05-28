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

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

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

	private static Block registerBlock(String name, Function<AbstractBlock.Settings, Block> blockFactory) {
		Identifier id = Identifier.of(MOD_ID, name);
		RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, id);
		RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);
		Block block = blockFactory.apply(AbstractBlock.Settings.create().nonOpaque().registryKey(blockKey));
		Registry.register(Registries.BLOCK, blockKey, block);
		Registry.register(Registries.ITEM, itemKey, new BlockItem(block, new Item.Settings().registryKey(itemKey)));
		return block;
	}

	private static final class NonLadderBlock extends Block {
		NonLadderBlock(Settings settings) {
			super(settings);
		}

		@Override
		protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
			return VoxelShapes.empty();
		}
	}
}
