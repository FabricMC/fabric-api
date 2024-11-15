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
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.util.TriState;

/**
 * Events relating to enchantments, allowing for finer control of what enchantments can apply to different items.
 */
public final class EnchantmentEvents {
	private EnchantmentEvents() { }

	/**
	 * An event that allows overriding whether an {@link Enchantment} can be applied to an {@link ItemStack}.
	 *
	 * <p>This should only be used to modify the behavior of <em>external</em> items with regards to <em>external</em> enchantments,
	 * where 'external' means either vanilla or from another mod. For instance, a mod might allow enchanting a pickaxe
	 * with Sharpness (and only Sharpness) under certain specific conditions.</p>
	 *
	 * <p>To modify the behavior of your own modded <em>enchantments</em>, specify a custom tag for {@link Enchantment.Definition#supportedItems()} instead.
	 * To modify the behavior of your own modded <em>items</em>, add to the applicable tags instead, when that suffices.
	 * Note that this event triggers <em>before</em> {@link FabricItem#canBeEnchantedWith(ItemStack, RegistryEntry, EnchantingContext)},
	 * and that method will only be called if no listeners override it.</p>
	 *
	 * <p>Note that allowing an enchantment using this event does not guarantee the item will receive that enchantment,
	 * only that it isn't forbidden from doing so.</p>
	 *
	 * @see AllowEnchanting#allowEnchanting(RegistryEntry, ItemStack, EnchantingContext)
	 * @see Enchantment#isAcceptableItem(ItemStack)
	 * @see FabricItem#canBeEnchantedWith(ItemStack, RegistryEntry, EnchantingContext)
	 */
	public static final Event<AllowEnchanting> ALLOW_ENCHANTING = EventFactory.createArrayBacked(
			AllowEnchanting.class,
			callbacks -> (enchantment, target, context) -> {
				for (AllowEnchanting callback : callbacks) {
					TriState result = callback.allowEnchanting(enchantment, target, context);

					if (result != TriState.DEFAULT) {
						return result;
					}
				}

				return TriState.DEFAULT;
			}
	);

	/**
	 * An event that allows an {@link Enchantment} to be modified without needing to fully override an enchantment.
	 *
	 * <p>This should only be used to modify the behavior of <em>external</em> enchantments, where 'external' means
	 * either vanilla or from another mod. For instance, a mod might add a bleed effect to Sharpness (and only Sharpness).
	 * For your own enchantments, you should simply define them in your mod's data pack. See the
	 * <a href="https://minecraft.wiki/w/Enchantment_definition">Enchantment Definition page</a> on the Minecraft Wiki
	 * for more information.
	 *
	 * <p>Note: If you wish to modify the exclusive set of the enchantment, consider extending the
	 * {@linkplain net.minecraft.registry.tag.EnchantmentTags relevant tag} through your mod's data pack instead.
	 */
	public static final Event<Modify> MODIFY = EventFactory.createArrayBacked(
			Modify.class,
			callbacks -> (key, builder, source) -> {
				for (Modify callback : callbacks) {
					callback.modify(key, builder, source);
				}
			}
	);

	@FunctionalInterface
	public interface AllowEnchanting {
		/**
		 * Checks whether an {@link Enchantment} should be applied to a given {@link ItemStack}.
		 *
		 * @param enchantment the enchantment that may be applied
		 * @param target the target item
		 * @param enchantingContext the enchanting context in which this check is made
		 * @return {@link TriState#TRUE} if the enchantment may be applied, {@link TriState#FALSE} if it
		 * may not, {@link TriState#DEFAULT} to fall back to other callbacks/vanilla behavior
		 * @see EnchantingContext
		 */
		TriState allowEnchanting(
				RegistryEntry<Enchantment> enchantment,
				ItemStack target,
				EnchantingContext enchantingContext
		);
	}

	@FunctionalInterface
	public interface Modify {
		/**
		 * Modifies the effects of an {@link Enchantment}.
		 *
		 * @param key The ID of the enchantment
		 * @param builder The enchantment builder
		 * @param source The source of the enchantment
		 */
		void modify(
				RegistryKey<Enchantment> key,
				Enchantment.Builder builder,
				EnchantmentSource source
		);
	}
}
