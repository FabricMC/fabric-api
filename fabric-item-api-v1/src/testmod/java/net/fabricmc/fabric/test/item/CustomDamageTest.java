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

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.CustomDamageHandler;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.fabricmc.fabric.api.item.v1.EnchantmentEvents;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.fabricmc.fabric.api.registry.FuelRegistryEvents;
import net.fabricmc.fabric.api.util.TriState;

public class CustomDamageTest implements ModInitializer {
	public static final DataComponentType<Integer> WEIRD = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath("fabric-item-api-v1-testmod", "weird"),
																			DataComponentType.<Integer>builder().persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());
	public static final CustomDamageHandler WEIRD_DAMAGE_HANDLER = (stack, amount, entity, slot, breakCallback) -> {
		// If sneaking, apply all damage to vanilla. Otherwise, increment a tag on the stack by one and don't apply any damage
		if (entity.isShiftKeyDown()) {
			return amount;
		} else {
			stack.set(WEIRD, Math.max(0, stack.getOrDefault(WEIRD, 0) + 1));
			return 0;
		}
	};
	// Do this static init *after* the damage handler otherwise it's still null while inside the constructor
	public static final ResourceKey<Item> WEIRD_PICK_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fabric-item-api-v1-testmod", "weird_pickaxe"));
	public static final Item WEIRD_PICK = new WeirdPick(WEIRD_PICK_KEY);

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.ITEM, WEIRD_PICK_KEY, WEIRD_PICK);
		FuelRegistryEvents.BUILD.register((builder, context) -> builder.add(WEIRD_PICK, context.baseSmeltTime()));
		FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> builder.addMix(Potions.WATER, WEIRD_PICK, Potions.AWKWARD));
		EnchantmentEvents.ALLOW_ENCHANTING.register(((enchantment, target, enchantingContext) -> {
			if (target.is(Items.DIAMOND_PICKAXE) && enchantment.is(Enchantments.SHARPNESS) && EnchantmentHelper.hasTag(target, EnchantmentTags.MINING_EXCLUSIVE)) {
				return TriState.TRUE;
			}

			return TriState.DEFAULT;
		}));
	}

	public static class WeirdPick extends Item {
		protected WeirdPick(ResourceKey<Item> registryKey) {
			super(new Item.Properties().pickaxe(ToolMaterial.GOLD, 3f, 5f).customDamage(WEIRD_DAMAGE_HANDLER).setId(registryKey));
		}

		@Override
		public Component getName(ItemStack stack) {
			int v = stack.getOrDefault(WEIRD, 0);
			return super.getName(stack).copy().append(" (Weird Value: " + v + ")");
		}

		@Override
		public ItemStack getRecipeRemainder(ItemStack stack) {
			if (stack.getDamageValue() < stack.getMaxDamage() - 1) {
				ItemStack moreDamaged = stack.copy();
				moreDamaged.setCount(1);
				moreDamaged.setDamageValue(stack.getDamageValue() + 1);
				return moreDamaged;
			}

			return ItemStack.EMPTY;
		}

		@Override
		public boolean canBeEnchantedWith(ItemStack stack, Holder<Enchantment> enchantment, EnchantingContext context) {
			return context == EnchantingContext.ACCEPTABLE && enchantment.is(Enchantments.FIRE_ASPECT)
				|| !enchantment.is(Enchantments.FORTUNE) && super.canBeEnchantedWith(stack, enchantment, context);
		}
	}
}
