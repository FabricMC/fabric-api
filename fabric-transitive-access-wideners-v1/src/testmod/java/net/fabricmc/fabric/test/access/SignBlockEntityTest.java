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

package net.fabricmc.fabric.test.access;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

public final class SignBlockEntityTest implements ModInitializer {
	public static final String MOD_ID = "fabric-transitive-access-wideners-v1-testmod";
	public static final ResourceKey<Block> TEST_SIGN_KEY = keyOf("test_sign");
	public static final StandingSignBlock TEST_SIGN = new StandingSignBlock(WoodType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN).setId(TEST_SIGN_KEY)) {
		@Override
		public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
			return new TestSign(pos, state);
		}
	};
	public static final ResourceKey<Block> TEST_WALL_SIGN_KEY = keyOf("test_wall_sign");
	public static final WallSignBlock TEST_WALL_SIGN = new WallSignBlock(WoodType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN).setId(TEST_WALL_SIGN_KEY)) {
		@Override
		public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
			return new TestSign(pos, state);
		}
	};
	public static final SignItem TEST_SIGN_ITEM = new SignItem(TEST_SIGN, TEST_WALL_SIGN, new Item.Properties().setId(itemKey(TEST_SIGN_KEY)));
	public static final BlockEntityType<TestSign> TEST_SIGN_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(TestSign::new, TEST_SIGN, TEST_WALL_SIGN).build();

	private static ResourceKey<Block> keyOf(String id) {
		return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, id));
	}

	private static ResourceKey<Item> itemKey(ResourceKey<Block> blockKey) {
		return ResourceKey.create(Registries.ITEM, blockKey.identifier());
	}

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.BLOCK, TEST_SIGN_KEY, TEST_SIGN);
		Registry.register(BuiltInRegistries.BLOCK, TEST_WALL_SIGN_KEY, TEST_WALL_SIGN);
		Registry.register(BuiltInRegistries.ITEM, TEST_SIGN_KEY.identifier(), TEST_SIGN_ITEM);
		Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "test_sign"), TEST_SIGN_BLOCK_ENTITY);
	}

	public static class TestSign extends SignBlockEntity {
		public TestSign(BlockPos pos, BlockState state) {
			super(TEST_SIGN_BLOCK_ENTITY, pos, state);
		}

		@Override
		public BlockEntityType<?> getType() {
			return TEST_SIGN_BLOCK_ENTITY;
		}
	}
}
