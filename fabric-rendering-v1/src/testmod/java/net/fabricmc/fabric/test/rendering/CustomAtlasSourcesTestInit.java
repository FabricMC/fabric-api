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
import net.minecraft.world.item.Item;

import net.fabricmc.api.ModInitializer;

public class CustomAtlasSourcesTestInit implements ModInitializer {
	public static final ResourceKey<Item> DOUBLE_IRON_INGOT_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fabric-rendering-v1-testmod", "double_iron_ingot"));
	public static final Item DOUBLE_IRON_INGOT = new Item(new Item.Properties().setId(DOUBLE_IRON_INGOT_KEY));

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.ITEM, DOUBLE_IRON_INGOT_KEY, DOUBLE_IRON_INGOT);
	}
}
