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

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItem;

public final class ArmorRenderingTestsInit implements ModInitializer {
	public static final Item CUSTOM_HELMET = new Item(((FabricItem.Settings) new Item.Settings()).equipmentSlot((entity, stack) -> EquipmentSlot.HEAD));

	@Override
	public void onInitialize() {
		Registry.register(Registries.ITEM, Identifier.of("fabric-rendering-v1-testmod", "custom_helmet"), CUSTOM_HELMET);
	}
}
