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

package net.fabricmc.fabric.test.screenhandler;

import net.fabricmc.fabric.test.inventory.InventoryEventsTest;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.test.screenhandler.block.BoxBlock;
import net.fabricmc.fabric.test.screenhandler.block.BoxBlockEntity;
import net.fabricmc.fabric.test.screenhandler.item.BagItem;
import net.fabricmc.fabric.test.screenhandler.item.PositionedBagItem;
import net.fabricmc.fabric.test.screenhandler.screen.BagScreenHandler;
import net.fabricmc.fabric.test.screenhandler.screen.BoxScreenHandler;
import net.fabricmc.fabric.test.screenhandler.screen.PositionedBagScreenHandler;

public class ScreenHandlerTest implements ModInitializer {
	public static final String ID = "fabric-screen-handler-api-v1-testmod";

	public static final RegistryKey<Item> BAG_KEY = RegistryKey.of(RegistryKeys.ITEM, id("bag"));
	public static final Item BAG = new BagItem(new Item.Settings().maxCount(1).registryKey(BAG_KEY));
	public static final RegistryKey<Item> POSITIONED_BAG_KEY = RegistryKey.of(RegistryKeys.ITEM, id("positioned_bag"));
	public static final Item POSITIONED_BAG = new PositionedBagItem(new Item.Settings().maxCount(1).registryKey(POSITIONED_BAG_KEY));
	public static final RegistryKey<Block> BOX_KEY = RegistryKey.of(RegistryKeys.BLOCK, id("box"));
	public static final Block BOX = new BoxBlock(AbstractBlock.Settings.copy(Blocks.OAK_WOOD).registryKey(BOX_KEY));
	public static final Item BOX_ITEM = new BlockItem(BOX, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, BOX_KEY.getValue())));
	public static final BlockEntityType<BoxBlockEntity> BOX_ENTITY = FabricBlockEntityTypeBuilder.create(BoxBlockEntity::new, BOX).build();
	public static final ScreenHandlerType<BagScreenHandler> BAG_SCREEN_HANDLER = new ScreenHandlerType<>(BagScreenHandler::new, FeatureFlags.VANILLA_FEATURES);
	public static final ScreenHandlerType<PositionedBagScreenHandler> POSITIONED_BAG_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(PositionedBagScreenHandler::new, PositionedBagScreenHandler.BagData.PACKET_CODEC);
	public static final ScreenHandlerType<BoxScreenHandler> BOX_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(BoxScreenHandler::new, BlockPos.PACKET_CODEC.cast());

	public static Identifier id(String path) {
		return Identifier.of(ID, path);
	}

	@Override
	public void onInitialize() {
		Registry.register(Registries.ITEM, BAG_KEY, BAG);
		Registry.register(Registries.ITEM, POSITIONED_BAG_KEY, POSITIONED_BAG);
		Registry.register(Registries.BLOCK, BOX_KEY, BOX);
		Registry.register(Registries.ITEM, BOX_KEY.getValue(), BOX_ITEM);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, id("box"), BOX_ENTITY);
		Registry.register(Registries.SCREEN_HANDLER, id("bag"), BAG_SCREEN_HANDLER);
		Registry.register(Registries.SCREEN_HANDLER, id("positioned_bag"), POSITIONED_BAG_SCREEN_HANDLER);
		Registry.register(Registries.SCREEN_HANDLER, id("box"), BOX_SCREEN_HANDLER);

		new InventoryEventsTest().onInitialize();

	}
}
