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

package net.fabricmc.fabric.test.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.test.menu.block.BoxBlock;
import net.fabricmc.fabric.test.menu.block.BoxBlockEntity;
import net.fabricmc.fabric.test.menu.item.BagItem;
import net.fabricmc.fabric.test.menu.item.PositionedBagItem;
import net.fabricmc.fabric.test.menu.menu.BagMenu;
import net.fabricmc.fabric.test.menu.menu.BoxMenu;
import net.fabricmc.fabric.test.menu.menu.PositionedBagMenu;

public class MenuTest implements ModInitializer {
	public static final String ID = "fabric-menu-api-v1-testmod";

	public static final ResourceKey<Item> BAG_KEY = ResourceKey.create(Registries.ITEM, id("bag"));
	public static final Item BAG = new BagItem(new Item.Properties().stacksTo(1).setId(BAG_KEY));
	public static final ResourceKey<Item> POSITIONED_BAG_KEY = ResourceKey.create(Registries.ITEM, id("positioned_bag"));
	public static final Item POSITIONED_BAG = new PositionedBagItem(new Item.Properties().stacksTo(1).setId(POSITIONED_BAG_KEY));
	public static final ResourceKey<Block> BOX_KEY = ResourceKey.create(Registries.BLOCK, id("box"));
	public static final Block BOX = new BoxBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD).setId(BOX_KEY));
	public static final Item BOX_ITEM = new BlockItem(BOX, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, BOX_KEY.identifier())));
	public static final BlockEntityType<BoxBlockEntity> BOX_ENTITY = FabricBlockEntityTypeBuilder.create(BoxBlockEntity::new, BOX).build();
	public static final MenuType<BagMenu> BAG_MENU = new MenuType<>(BagMenu::new, FeatureFlags.VANILLA_SET);
	public static final MenuType<PositionedBagMenu> POSITIONED_BAG_MENU = new ExtendedMenuType<>(PositionedBagMenu::new, PositionedBagMenu.BagData.PACKET_CODEC);
	public static final MenuType<BoxMenu> BOX_SCREEN_MENU = new ExtendedMenuType<>(BoxMenu::new, BlockPos.STREAM_CODEC.cast());

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(ID, path);
	}

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.ITEM, BAG_KEY, BAG);
		Registry.register(BuiltInRegistries.ITEM, POSITIONED_BAG_KEY, POSITIONED_BAG);
		Registry.register(BuiltInRegistries.BLOCK, BOX_KEY, BOX);
		Registry.register(BuiltInRegistries.ITEM, BOX_KEY.identifier(), BOX_ITEM);
		Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("box"), BOX_ENTITY);
		Registry.register(BuiltInRegistries.MENU, id("bag"), BAG_MENU);
		Registry.register(BuiltInRegistries.MENU, id("positioned_bag"), POSITIONED_BAG_MENU);
		Registry.register(BuiltInRegistries.MENU, id("box"), BOX_SCREEN_MENU);
	}
}
