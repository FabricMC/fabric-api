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

package net.fabricmc.fabric.api.item.v1;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;

import net.fabricmc.fabric.api.util.TriState;

/**
 * Fabric-provided extensions for {@link ItemStack}.
 * This interface is automatically implemented on all item stacks via Mixin and interface injection.
 */
public interface FabricItemStack {
	/**
	 * Return a leftover item for use in recipes.
	 *
	 * <p>See {@link FabricItem#getRecipeRemainder(ItemStack)} for a more in depth description.
	 *
	 * <p>Stack-aware version of {@link Item#getRecipeRemainder()}.
	 *
	 * @return the leftover item
	 */
	default ItemStack getRecipeRemainder() {
		return ((ItemStack) this).getItem().getRecipeRemainder((ItemStack) this);
	}

	/**
	 * Determines whether this {@link ItemStack} can be enchanted with the given {@link Enchantment}.
	 *
	 * <p>When checking whether an enchantment can be applied to an {@link ItemStack}, use this method instead of
	 * {@link Enchantment#isAcceptableItem(ItemStack)} or {@link Enchantment#isPrimaryItem(ItemStack)}, with the appropriate
	 * {@link EnchantingContext}.</p>
	 *
	 * @param enchantment the enchantment to check
	 * @param context the context in which the enchantment is being checked
	 * @return whether the enchantment is allowed to apply to the stack
	 * @see FabricItem#canBeEnchantedWith(ItemStack, RegistryEntry, EnchantingContext)
	 */
	default boolean canBeEnchantedWith(RegistryEntry<Enchantment> enchantment, EnchantingContext context) {
		TriState result = EnchantmentEvents.ALLOW_ENCHANTING.invoker().allowEnchanting(
				enchantment,
				(ItemStack) this,
				context
		);
		return result.orElseGet(() -> ((ItemStack) this).getItem().canBeEnchantedWith((ItemStack) this, enchantment, context));
	}

	/**
	 * Gets the namespace of the mod or datapack that created this item.
	 *
	 * <p>This can be used if, for example, a library mod registers a generic item that other mods can create new
	 * variants for, allowing those mods to take credit for those variants if a player wishes to know what mod they
	 * come from.</p>
	 *
	 * <p>Should be used instead of querying the item ID namespace to determine what mod an item is from when displaying
	 * to the player.</p>
	 *
	 * <p>Defaults to the namespace of the item's own registry entry, except in the cases of potions or enchanted books,
	 * in which it uses the namespace of the potion contents or single enchantment applied.</p>
	 *
	 * <p>Note that while it is recommended that this reflect a namespace and/or mod ID, it can technically be any
	 * arbitrary string.</p>
	 *
	 * @return the namespace of the mod that created the item
	 */
	default String getCreatorNamespace() {
		return ((ItemStack) this).getItem().getCreatorNamespace((ItemStack) this);
	}
}
