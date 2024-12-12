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

import java.util.Map;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;

public class ArmorKnockbackResistanceTest implements ModInitializer {
	private static final ArmorMaterial WOOD_ARMOR = createTestArmorMaterial();

	@Override
	public void onInitialize() {
		RegistryKey<Item> registryKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fabric-item-api-v1-testmod", "wooden_boots"));
		Registry.register(Registries.ITEM, registryKey, new ArmorItem(WOOD_ARMOR, EquipmentType.BOOTS, new Item.Settings().registryKey(registryKey)));
	}

	private static ArmorMaterial createTestArmorMaterial() {
		return new ArmorMaterial(
			0,
			Map.of(
				EquipmentType.BOOTS, 1,
				EquipmentType.LEGGINGS, 2,
				EquipmentType.CHESTPLATE, 3,
				EquipmentType.HELMET, 1,
				EquipmentType.BODY, 3
			),
			1,
			SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
			0,
			0.5F,
			ItemTags.REPAIRS_LEATHER_ARMOR,
			EquipmentAssetKeys.IRON
		);
	}
}
