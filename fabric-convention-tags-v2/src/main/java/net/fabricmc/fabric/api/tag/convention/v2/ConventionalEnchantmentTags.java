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

package net.fabricmc.fabric.api.tag.convention.v2;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;

import net.fabricmc.fabric.impl.tag.convention.v2.TagRegistration;

public final class ConventionalEnchantmentTags {
	private ConventionalEnchantmentTags() {
	}

	/**
	 * A tag containing enchantments that increase the amount or
	 * quality of drops from blocks, such as {@link net.minecraft.world.item.enchantment.Enchantments#FORTUNE}.
	 */
	public static final TagKey<Enchantment> INCREASE_BLOCK_DROPS = register("increase_block_drops");
	/**
	 * A tag containing enchantments that increase the amount or
	 * quality of drops from entities, such as {@link net.minecraft.world.item.enchantment.Enchantments#LOOTING}.
	 */
	public static final TagKey<Enchantment> INCREASE_ENTITY_DROPS = register("increase_entity_drops");
	/**
	 * For enchantments that increase the damage dealt by an item.
	 */
	public static final TagKey<Enchantment> WEAPON_DAMAGE_ENHANCEMENTS = register("weapon_damage_enhancements");
	/**
	 * For enchantments that increase movement speed for entity wearing armor enchanted with it.
	 */
	public static final TagKey<Enchantment> ENTITY_SPEED_ENHANCEMENTS = register("entity_speed_enhancements");
	/**
	 * For enchantments that applies movement-based benefits unrelated to speed for the entity wearing armor enchanted with it.
	 * Example: Reducing falling speeds ({@link net.minecraft.world.item.enchantment.Enchantments#FEATHER_FALLING}) or allowing walking on water ({@link net.minecraft.world.item.enchantment.Enchantments#FROST_WALKER})
	 */
	public static final TagKey<Enchantment> ENTITY_AUXILIARY_MOVEMENT_ENHANCEMENTS = register("entity_auxiliary_movement_enhancements");
	/**
	 * For enchantments that decrease damage taken or otherwise benefit, in regard to damage, the entity wearing armor enchanted with it.
	 */
	public static final TagKey<Enchantment> ENTITY_DEFENSE_ENHANCEMENTS = register("entity_defense_enhancements");

	private static TagKey<Enchantment> register(String tagId) {
		return TagRegistration.ENCHANTMENT_TAG.registerC(tagId);
	}
}
