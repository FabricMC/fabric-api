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

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * An enum that describes the contexts in which the game checks whether an enchantment can be applied to an item.
 */
public enum EnchantingContext {
	/**
	 * When checking if an item is <em>acceptable</em> for a given enchantment, i.e if the item should be able to bear
	 * that enchantment. This includes anvils, the {@code enchant_randomly} loot function, and the {@code /enchant} command.
	 *
	 * @see Enchantment#canEnchant(ItemStack)
	 */
	ACCEPTABLE,
	/**
	 * When checking for an enchantment's <em>primary</em> items. This includes enchanting in an enchanting table, random
	 * mob equipment, and the {@code enchant_with_levels} loot function.
	 *
	 * @see Enchantment#isPrimaryItem(ItemStack)
	 */
	PRIMARY
}
