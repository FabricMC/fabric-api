package net.fabricmc.fabric.test.item;

import net.fabricmc.api.ModInitializer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;

public class CustomItemTests implements ModInitializer {
	@Override
	public void onInitialize() {
		Identifier fishingRod = Identifier.fromNamespaceAndPath("fabric", "custom_fishing_rod");
		Registry.register(BuiltInRegistries.ITEM, fishingRod, new FishingRodItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, fishingRod))));
	}
}
