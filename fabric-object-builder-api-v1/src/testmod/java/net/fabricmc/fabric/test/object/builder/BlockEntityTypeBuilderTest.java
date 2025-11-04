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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

public class BlockEntityTypeBuilderTest implements ModInitializer {
	private static final ResourceKey<Block> INITIAL_BETRAYAL_BLOCK_ID = ObjectBuilderTestConstants.block("initial_betrayal_block");
	static final Block INITIAL_BETRAYAL_BLOCK = createBetrayalBlock(INITIAL_BETRAYAL_BLOCK_ID, MapColor.COLOR_BLUE);

	private static final ResourceKey<Block> ADDED_BETRAYAL_BLOCK_ID = ObjectBuilderTestConstants.block("added_betrayal_block");
	static final Block ADDED_BETRAYAL_BLOCK = createBetrayalBlock(ADDED_BETRAYAL_BLOCK_ID, MapColor.COLOR_GREEN);

	private static final ResourceKey<Block> FIRST_MULTI_BETRAYAL_BLOCK_ID = ObjectBuilderTestConstants.block("first_multi_betrayal_block");
	static final Block FIRST_MULTI_BETRAYAL_BLOCK = createBetrayalBlock(FIRST_MULTI_BETRAYAL_BLOCK_ID, MapColor.COLOR_RED);

	private static final ResourceKey<Block> SECOND_MULTI_BETRAYAL_BLOCK_ID = ObjectBuilderTestConstants.block("second_multi_betrayal_block");
	static final Block SECOND_MULTI_BETRAYAL_BLOCK = createBetrayalBlock(SECOND_MULTI_BETRAYAL_BLOCK_ID, MapColor.COLOR_YELLOW);

	private static final ResourceKey<Block> BLOCK_ENTITY_TYPE_ID = ObjectBuilderTestConstants.block("betrayal_block");
	public static final BlockEntityType<?> BLOCK_ENTITY_TYPE = FabricBlockEntityTypeBuilder.create(BetrayalBlockEntity::new, INITIAL_BETRAYAL_BLOCK, ADDED_BETRAYAL_BLOCK, FIRST_MULTI_BETRAYAL_BLOCK, SECOND_MULTI_BETRAYAL_BLOCK).build();

	@Override
	public void onInitialize() {
		register(INITIAL_BETRAYAL_BLOCK_ID, INITIAL_BETRAYAL_BLOCK);
		register(ADDED_BETRAYAL_BLOCK_ID, ADDED_BETRAYAL_BLOCK);
		register(FIRST_MULTI_BETRAYAL_BLOCK_ID, FIRST_MULTI_BETRAYAL_BLOCK);
		register(SECOND_MULTI_BETRAYAL_BLOCK_ID, SECOND_MULTI_BETRAYAL_BLOCK);

		Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, BLOCK_ENTITY_TYPE_ID.identifier(), BLOCK_ENTITY_TYPE);
	}

	private static Block createBetrayalBlock(ResourceKey<Block> key, MapColor color) {
		return new BetrayalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).mapColor(color).setId(key));
	}

	private static void register(ResourceKey<Block> id, Block block) {
		Registry.register(BuiltInRegistries.BLOCK, id, block);

		Item item = new BlockItem(block, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id.identifier())));
		Registry.register(BuiltInRegistries.ITEM, id.identifier(), item);
	}

	private static class BetrayalBlock extends Block implements EntityBlock {
		private BetrayalBlock(BlockBehaviour.Properties settings) {
			super(settings);
		}

		@Override
		public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
			if (!world.isClientSide()) {
				BlockEntity blockEntity = world.getBlockEntity(pos);

				if (blockEntity == null) {
					throw new AssertionError("Missing block entity for betrayal block at " + pos);
				} else if (!BLOCK_ENTITY_TYPE.equals(blockEntity.getType())) {
					Identifier id = BlockEntityType.getKey(blockEntity.getType());
					throw new AssertionError("Incorrect block entity for betrayal block at " + pos + ": " + id);
				}

				Component posText = Component.translatable("chat.coordinates", pos.getX(), pos.getY(), pos.getZ());
				Component message = Component.translatableEscape("text.fabric-object-builder-api-v1-testmod.block_entity_type_success", posText, BLOCK_ENTITY_TYPE_ID);

				player.displayClientMessage(message, false);
			}

			return InteractionResult.SUCCESS;
		}

		@Override
		public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
			return new BetrayalBlockEntity(pos, state);
		}
	}

	private static class BetrayalBlockEntity extends BlockEntity {
		private BetrayalBlockEntity(BlockPos pos, BlockState state) {
			super(BLOCK_ENTITY_TYPE, pos, state);
		}
	}
}
