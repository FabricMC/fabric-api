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

import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

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
	 * When the NBT of an item stack in the main hand or off hand changes, vanilla runs an "update animation".
	 * This function is called on the client side when the NBT or count of the stack has changed, but not the item,
	 * and returning false cancels this animation.
	 *
	 * @param player   the current player; this may be safely cast to {@link net.minecraft.client.network.ClientPlayerEntity} in client-only code
	 * @param hand     the hand; this function applies both to the main hand and the off hand
	 * @param oldStack the previous stack, of this item
	 * @param newStack the new stack, also of this item
	 * @return true to run the vanilla animation, false to cancel it.
	 */
	default boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
		return true;
	}

	/**
	 * When the NBT of the selected stack changes, block breaking progress is reset.
	 * This function is called when the NBT of the selected stack has changed,
	 * and returning true allows the block breaking progress to continue.
	 *
	 * @param player   the player breaking the block
	 * @param oldStack the previous stack, of this item
	 * @param newStack the new stack, also of this item
	 * @return true to allow continuing block breaking, false to reset the progress.
	 */
	default boolean allowContinuingBlockBreaking(PlayerEntity player, ItemStack oldStack, ItemStack newStack) {
		return false;
	}

	/**
	 * Return the attribute modifiers to apply when this stack is worn in a living entity equipment slot.
	 * Stack-aware version of {@link Item#getAttributeModifiers(EquipmentSlot)}.
	 *
	 * <p>Note that attribute modifiers are only updated when the stack changes, i.e. when {@code ItemStack.areEqual(old, new)} is false.
	 *
	 * @param stack the current stack
	 * @param slot  the equipment slot this stack is in
	 * @return the attribute modifiers
	 */
	default Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
		return ((Item) this).getAttributeModifiers(slot);
	}

	/**
	 * Determines if mining with this item allows drops to be harvested from the specified block state.
	 * Stack-aware version of {@link Item#isSuitableFor(BlockState)}.
	 *
	 * @param stack the current stack
	 * @param state the block state of the targeted block
	 * @return true if drops can be harvested
	 */
	default boolean isSuitableFor(ItemStack stack, BlockState state) {
		return ((Item) this).isSuitableFor(state);
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
	 * <p>This is a stack-aware version of {@link Item#getRecipeRemainder()}.
	 *
	 * <p>Note that simple item remainders can also be set via {@link Item.Settings#recipeRemainder(Item)}.
	 *
	 * <p>If you want to get a remainder for a stack,
	 * is recommended to use the stack version of this method: {@link FabricItemStack#getRecipeRemainder()}.
	 *
	 * @param stack the consumed {@link ItemStack}
	 * @return the leftover item stack
	 */
	default ItemStack getRecipeRemainder(ItemStack stack) {
		return ((Item) this).hasRecipeRemainder() ? ((Item) this).getRecipeRemainder().getDefaultStack() : ItemStack.EMPTY;
	}

	/**
	 * This is a stack-aware version of {@link Item#getFoodComponent()}.
	 * Note that simple food component can also be set via {@link Item.Settings#food(FoodComponent)}.
	 * If you want to get a food component for a stack, is <strong>recommended</strong> to use the stack version of this method: {@link FabricItemStack#getFoodComponent()}.
	 *
	 * @return this item's {@link FoodComponent}, or {@code null} if none was set
	 */
	default @Nullable FoodComponent getFoodComponent(ItemStack stack) {
		return ((Item) this).getFoodComponent();
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
	default boolean canBeEnchantedWith(ItemStack stack, Enchantment enchantment, EnchantingContext context) {
		return context != EnchantingContext.RANDOM_ENCHANTMENT
				? enchantment.isAcceptableItem(stack)
				: enchantment.target.isAcceptableItem(stack.getItem());
	}

	/**
	 * Returns a (stack-aware) map of <em>intrinsic enchantments</em> for this item.
	 * These enchantments have their usual gameplay effects, but do not produce glint or otherwise show on the item,
	 * and cannot be removed with a grindstone. For example, a mod that adds an electric multi-tool might want to give
	 * it a Silk Touch-like effect, without relying on the vanilla system.
	 *
	 * <p>By default, having an intrinsic enchantment does not prevent the item from being enchanted with the same one
	 * by normal means. In such a case, only the highest level of the enchantment will be retained. To prevent the item
	 * from receiving certain enchantments, use {@link #canBeEnchantedWith(ItemStack, Enchantment, EnchantingContext)}.</p>
	 *
	 * @param stack the current stack
	 * @return an immutable map from each intrinsic enchantment to its level
	 * @see #canBeEnchantedWith(ItemStack, Enchantment, EnchantingContext)
	 */
	default Object2IntMap<Enchantment> getIntrinsicEnchantments(ItemStack stack) {
		return Object2IntMaps.emptyMap();
	}
}
