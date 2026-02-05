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

package net.fabricmc.fabric.test.rendering;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.fabricmc.api.ModInitializer;

public class CustomColorResolverTestInit implements ModInitializer {
	public static final ResourceKey<Block> KEY = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("fabric-rendering-v1-testmod", "custom_color_block"));
	public static final Block CUSTOM_COLOR_BLOCK = new Block(BlockBehaviour.Properties.of().setId(KEY));
	public static final Item CUSTOM_COLOR_BLOCK_ITEM = new BlockItem(CUSTOM_COLOR_BLOCK, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, KEY.identifier())));

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.BLOCK, KEY, CUSTOM_COLOR_BLOCK);
		Registry.register(BuiltInRegistries.ITEM, KEY.identifier(), CUSTOM_COLOR_BLOCK_ITEM);
	}
}
