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

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.Blocks;
import net.minecraft.block.HangingSignBlock;
import net.minecraft.block.SignBlock;
import net.minecraft.block.WallHangingSignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.WoodType;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.HangingSignItem;
import net.minecraft.item.Item;
import net.minecraft.item.SignItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.type.WoodTypeBuilder;

public class TealSignTest implements ModInitializer {
	public static final Identifier TEAL_TYPE_ID = ObjectBuilderTestConstants.id("teal");
	public static final BlockSetType TEAL_BLOCK_SET_TYPE = BlockSetTypeBuilder.copyOf(BlockSetType.OAK).build(TEAL_TYPE_ID);
	public static final WoodType TEAL_WOOD_TYPE = WoodTypeBuilder.copyOf(WoodType.OAK).build(TEAL_TYPE_ID, TEAL_BLOCK_SET_TYPE);
	public static final RegistryKey<Block> TEAL_SIGN_KEY = ObjectBuilderTestConstants.block("teal_sign");
	public static final SignBlock TEAL_SIGN = new SignBlock(TEAL_WOOD_TYPE, AbstractBlock.Settings.copy(Blocks.OAK_SIGN).registryKey(TEAL_SIGN_KEY));
	public static final RegistryKey<Block> TEAL_WALL_SIGN_KEY = ObjectBuilderTestConstants.block("teal_wall_sign");
	public static final WallSignBlock TEAL_WALL_SIGN = new WallSignBlock(TEAL_WOOD_TYPE, AbstractBlock.Settings.copy(Blocks.OAK_SIGN).registryKey(TEAL_WALL_SIGN_KEY));
	public static final RegistryKey<Block> TEAL_HANGING_SIGN_KEY = ObjectBuilderTestConstants.block("teal_hanging_sign");
	public static final HangingSignBlock TEAL_HANGING_SIGN = new HangingSignBlock(TEAL_WOOD_TYPE, AbstractBlock.Settings.copy(Blocks.OAK_HANGING_SIGN).registryKey(TEAL_HANGING_SIGN_KEY));
	public static final RegistryKey<Block> TEAL_WALL_HANGING_SIGN_KEY = ObjectBuilderTestConstants.block("teal_wall_hanging_sign");
	public static final WallHangingSignBlock TEAL_WALL_HANGING_SIGN = new WallHangingSignBlock(TEAL_WOOD_TYPE, AbstractBlock.Settings.copy(Blocks.OAK_HANGING_SIGN).registryKey(TEAL_WALL_HANGING_SIGN_KEY));
	public static final SignItem TEAL_SIGN_ITEM = new SignItem(TEAL_SIGN, TEAL_WALL_SIGN, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, TEAL_SIGN_KEY.getValue())));
	public static final HangingSignItem TEAL_HANGING_SIGN_ITEM = new HangingSignItem(TEAL_HANGING_SIGN, TEAL_WALL_HANGING_SIGN, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, TEAL_HANGING_SIGN_KEY.getValue())));

	@Override
	public void onInitialize() {
		WoodType.register(TEAL_WOOD_TYPE);

		Registry.register(Registries.BLOCK, TEAL_SIGN_KEY, TEAL_SIGN);
		Registry.register(Registries.BLOCK, TEAL_WALL_SIGN_KEY, TEAL_WALL_SIGN);
		Registry.register(Registries.BLOCK, TEAL_HANGING_SIGN_KEY, TEAL_HANGING_SIGN);
		Registry.register(Registries.BLOCK, TEAL_WALL_HANGING_SIGN_KEY, TEAL_WALL_HANGING_SIGN);

		Registry.register(Registries.ITEM, TEAL_SIGN_KEY.getValue(), TEAL_SIGN_ITEM);
		Registry.register(Registries.ITEM, TEAL_HANGING_SIGN_KEY.getValue(), TEAL_HANGING_SIGN_ITEM);

		BlockEntityType.SIGN.addSupportedBlock(TEAL_SIGN);
		BlockEntityType.SIGN.addSupportedBlock(TEAL_WALL_SIGN);
		BlockEntityType.HANGING_SIGN.addSupportedBlock(TEAL_HANGING_SIGN);
		BlockEntityType.HANGING_SIGN.addSupportedBlock(TEAL_WALL_HANGING_SIGN);
	}
}
