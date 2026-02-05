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

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;

import net.fabricmc.api.ModInitializer;

public class TooltipComponentTestInit implements ModInitializer {
	public static final ResourceKey<Item> CUSTOM_TOOLTIP_ITEM_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fabric-rendering-v1-testmod", "custom_tooltip"));
	public static final Item CUSTOM_TOOLTIP_ITEM = new CustomTooltipItem(new Item.Properties().setId(CUSTOM_TOOLTIP_ITEM_KEY));

	public static final ArmorMaterial TEST_ARMOR_MATERIAL = createTestArmorMaterial();
	public static final ResourceKey<Item> CUSTOM_ARMOR_ITEM_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fabric-rendering-v1-testmod", "test_chest"));
	public static final Item CUSTOM_ARMOR_ITEM = new Item(new Item.Properties().humanoidArmor(TEST_ARMOR_MATERIAL, ArmorType.CHESTPLATE).setId(CUSTOM_ARMOR_ITEM_KEY));

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.ITEM, CUSTOM_TOOLTIP_ITEM_KEY, CUSTOM_TOOLTIP_ITEM);
		Registry.register(BuiltInRegistries.ITEM, CUSTOM_ARMOR_ITEM_KEY, CUSTOM_ARMOR_ITEM);
	}

	private static class CustomTooltipItem extends Item {
		CustomTooltipItem(Properties settings) {
			super(settings);
		}

		@Override
		public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
			return Optional.of(new Data(stack.getItem().getDescriptionId()));
		}
	}

	public record Data(String string) implements TooltipComponent {
	}

	private static ArmorMaterial createTestArmorMaterial() {
		return new ArmorMaterial(
				0,
				Map.of(
						ArmorType.BOOTS, 1,
						ArmorType.LEGGINGS, 2,
						ArmorType.CHESTPLATE, 3,
						ArmorType.HELMET, 1,
						ArmorType.BODY, 3
				),
				1,
				SoundEvents.ARMOR_EQUIP_LEATHER,
				0,
				0.5F,
				ItemTags.REPAIRS_LEATHER_ARMOR,
				EquipmentAssets.IRON
		);
	}
}
