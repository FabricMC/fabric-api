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

package net.fabricmc.fabric.api.tag.convention.v1;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;

import net.fabricmc.fabric.impl.tag.convention.TagRegistration;

/**
 * @deprecated Please use {@link net.fabricmc.fabric.api.tag.convention.v2.ConventionalEnchantmentTags}
 */
@Deprecated
public final class ConventionalEnchantmentTags {
	private ConventionalEnchantmentTags() {
	}

	/**
	 * A tag containing enchantments that increase the amount or
	 * quality of drops from blocks, such as {@link net.minecraft.world.item.enchantment.Enchantments#FORTUNE}.
	 */
	public static final TagKey<Enchantment> INCREASES_BLOCK_DROPS = register("fortune");
	/**
	 * A tag containing enchantments that increase the amount or
	 * quality of drops from entities, such as {@link net.minecraft.world.item.enchantment.Enchantments#LOOTING}.
	 */
	public static final TagKey<Enchantment> INCREASES_ENTITY_DROPS = register("looting");
	/**
	 * For enchantments that increase the damage dealt by an item.
	 */
	public static final TagKey<Enchantment> WEAPON_DAMAGE_ENHANCEMENT = register("weapon_damage_enhancement");
	/**
	 * For enchantments that increase movement speed or otherwise benefit the entity wearing armor enchanted with it.
	 */
	public static final TagKey<Enchantment> ENTITY_MOVEMENT_ENHANCEMENT = register("entity_movement_enhancement");
	/**
	 * For enchantments that decrease damage taken or otherwise benefit, in regard to damage, the entity wearing armor enchanted with it.
	 */
	public static final TagKey<Enchantment> ENTITY_DEFENSE_ENHANCEMENT = register("entity_defense_enhancement");

	private static TagKey<Enchantment> register(String tagID) {
		return TagRegistration.ENCHANTMENT_TAG_REGISTRATION.registerC(tagID);
	}
}
