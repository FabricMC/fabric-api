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

package net.fabricmc.fabric.test.item;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;

public class CustomModelIdTest implements ModInitializer {
	public static final RegistryKey<Item> NOT_A_DIAMOND_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fabric-item-api-v1-testmod", "not_a_diamond"));
	public static final Item NOT_A_DIAMOND = new Item(new Item.Settings().registryKey(NOT_A_DIAMOND_KEY).modelId(Identifier.ofVanilla("diamond")));

	@Override
	public void onInitialize() {
		Registry.register(Registries.ITEM, NOT_A_DIAMOND_KEY, NOT_A_DIAMOND);
	}
}
