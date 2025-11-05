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

import java.util.Optional;
import java.util.Set;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TippedArrowItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.enchantment.Enchantment;

import net.fabricmc.fabric.impl.item.FabricItemInternals;

/**
 * General-purpose Fabric-provided extensions for {@link Item} subclasses.
 *
 * <p>Note: This interface is automatically implemented on all items via Mixin and interface injection.
 *
 * <p>Note to maintainers: Functions should only be added to this interface if they are general-purpose enough,
 * to be evaluated on a case-by-case basis. Otherwise, they are better suited for more specialized APIs.
 */
public interface FabricItem {
	/**
	 * When the components of an item stack in the main hand or off hand changes, vanilla runs an "update animation".
	 * This function is called on the client side when the components or count of the stack has changed, but not the item,
	 * and returning false cancels this animation.
	 *
	 * @param player   the current player; this may be safely cast to {@link net.minecraft.client.player.LocalPlayer} in client-only code
	 * @param hand     the hand; this function applies both to the main hand and the off hand
	 * @param oldStack the previous stack, of this item
	 * @param newStack the new stack, also of this item
	 * @return true to run the vanilla animation, false to cancel it.
	 */
	default boolean allowComponentsUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
		return true;
	}

	/**
	 * When the components of the selected stack changes, block breaking progress is reset.
	 * This function is called when the components of the selected stack has changed,
	 * and returning true allows the block breaking progress to continue.
	 *
	 * @param player   the player breaking the block
	 * @param oldStack the previous stack, of this item
	 * @param newStack the new stack, also of this item
	 * @return true to allow continuing block breaking, false to reset the progress.
	 */
	default boolean allowContinuingBlockBreaking(Player player, ItemStack oldStack, ItemStack newStack) {
		return false;
	}

	/**
	 * Returns a leftover item stack after {@code stack} is consumed in a recipe.
	 * (This is also known as "recipe remainder".)
	 * For example, using a lava bucket in a furnace as fuel will leave an empty bucket.
	 *
	 * <p>Here is an example for a recipe remainder that increments the item's damage.
	 *
	 * <pre>{@code
	 *  if (stack.getDamage() < stack.getMaxDamage() - 1) {
	 *  	ItemStack moreDamaged = stack.copy();
	 *  	moreDamaged.setDamage(stack.getDamage() + 1);
	 *  	return moreDamaged;
	 *  }
	 *
	 *  return ItemStack.EMPTY;
	 * }</pre>
	 *
	 *
	 * <p>This is a stack-aware version of {@link Item#getCraftingRemainder()}.
	 *
	 * <p>Note that simple item remainders can also be set via {@link Item.Properties#craftRemainder(Item)}.
	 *
	 * <p>If you want to get a remainder for a stack,
	 * is recommended to use the stack version of this method: {@link FabricItemStack#getRecipeRemainder()}.
	 *
	 * @param stack the consumed {@link ItemStack}
	 * @return the leftover item stack
	 */
	default ItemStack getRecipeRemainder(ItemStack stack) {
		return ((Item) this).getCraftingRemainder();
	}

	/**
	 * Determines if the item is allowed to receive an {@link Enchantment}. This can be used to manually override what
	 * enchantments a modded item is able to receive.
	 *
	 * <p>For example, one might want a modded item to be able to receive Unbreaking, but not Mending, which cannot be
	 * achieved with the vanilla tag system alone. Alternatively, one might want to do the same thing with enchantments
	 * from other mods, which don't have a similar tag system in general.</p>
	 *
	 * <p>Note that this method is only called <em>after</em> the {@link EnchantmentEvents#ALLOW_ENCHANTING} event, and
	 * only if none of the listeners to that event override the result.</p>
	 *
	 * @param stack the current stack
	 * @param enchantment the enchantment to check
	 * @param context the context in which the enchantment is being checked
	 * @return whether the enchantment is allowed to apply to the stack
	 */
	default boolean canBeEnchantedWith(ItemStack stack, Holder<Enchantment> enchantment, EnchantingContext context) {
		return context == EnchantingContext.PRIMARY
				? enchantment.value().isPrimaryItem(stack)
				: enchantment.value().canEnchant(stack);
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
	 * @param stack the current stack
	 * @return the namespace of the mod that created the item
	 */
	default String getCreatorNamespace(ItemStack stack) {
		Holder<?> entry = stack.getItemHolder();

		if ((this instanceof PotionItem || this instanceof TippedArrowItem) && stack.has(DataComponents.POTION_CONTENTS)) {
			Optional<Holder<Potion>> potion = stack.get(DataComponents.POTION_CONTENTS).potion();
			if (potion.isPresent()) entry = potion.get();
		} else if (stack.is(Items.ENCHANTED_BOOK) && stack.has(DataComponents.STORED_ENCHANTMENTS)) {
			Set<Holder<Enchantment>> enchantments = stack.get(DataComponents.STORED_ENCHANTMENTS).keySet();
			if (enchantments.size() == 1) entry = enchantments.iterator().next();
		}

		return entry.unwrapKey().orElseThrow().identifier().getNamespace();
	}

	/**
	 * Fabric-provided extensions for {@link Item.Properties}.
	 * This interface is automatically implemented on all item settings via Mixin and interface injection.
	 */
	interface Settings {
		/**
		 * Sets the equipment slot provider of the item.
		 *
		 * @param equipmentSlotProvider the equipment slot provider
		 * @return this builder
		 */
		default Item.Properties equipmentSlot(EquipmentSlotProvider equipmentSlotProvider) {
			FabricItemInternals.computeExtraData((Item.Properties) this).equipmentSlot(equipmentSlotProvider);
			return (Item.Properties) this;
		}

		/**
		 * Sets the custom damage handler of the item.
		 * Note that this is only called on an ItemStack if {@link ItemStack#isDamageableItem()} returns true.
		 *
		 * @see CustomDamageHandler
		 */
		default Item.Properties customDamage(CustomDamageHandler handler) {
			FabricItemInternals.computeExtraData((Item.Properties) this).customDamage(handler);
			return (Item.Properties) this;
		}

		/**
		 * Sets the model of the item to static Identifier.
		 *
		 * @param modelId the model id item should use
		 * @return this builder
		 */
		default Item.Properties modelId(Identifier modelId) {
			return (Item.Properties) this;
		}
	}
}
