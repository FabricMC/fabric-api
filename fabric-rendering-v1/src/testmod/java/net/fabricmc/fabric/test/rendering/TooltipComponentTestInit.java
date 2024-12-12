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

import java.util.Map;
import java.util.Optional;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;

public class TooltipComponentTestInit implements ModInitializer {
	public static final RegistryKey<Item> CUSTOM_TOOLTIP_ITEM_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fabric-rendering-v1-testmod", "custom_tooltip"));
	public static final Item CUSTOM_TOOLTIP_ITEM = new CustomTooltipItem(new Item.Settings().registryKey(CUSTOM_TOOLTIP_ITEM_KEY));

	public static final ArmorMaterial TEST_ARMOR_MATERIAL = createTestArmorMaterial();
	public static final RegistryKey<Item> CUSTOM_ARMOR_ITEM_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fabric-rendering-v1-testmod", "test_chest"));
	public static final Item CUSTOM_ARMOR_ITEM = new ArmorItem(TEST_ARMOR_MATERIAL, EquipmentType.CHESTPLATE, new Item.Settings().registryKey(CUSTOM_ARMOR_ITEM_KEY));

	@Override
	public void onInitialize() {
		Registry.register(Registries.ITEM, CUSTOM_TOOLTIP_ITEM_KEY, CUSTOM_TOOLTIP_ITEM);
		Registry.register(Registries.ITEM, CUSTOM_ARMOR_ITEM_KEY, CUSTOM_ARMOR_ITEM);
	}

	private static class CustomTooltipItem extends Item {
		CustomTooltipItem(Settings settings) {
			super(settings);
		}

		@Override
		public Optional<TooltipData> getTooltipData(ItemStack stack) {
			return Optional.of(new Data(stack.getItem().getTranslationKey()));
		}
	}

	public record Data(String string) implements TooltipData {
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
