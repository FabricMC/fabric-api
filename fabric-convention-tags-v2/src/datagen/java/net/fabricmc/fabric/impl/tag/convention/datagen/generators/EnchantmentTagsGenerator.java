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

package net.fabricmc.fabric.impl.tag.convention.datagen.generators;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEnchantmentTags;

public final class EnchantmentTagsGenerator extends FabricTagsProvider<Enchantment> {
	public EnchantmentTagsGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, Registries.ENCHANTMENT, registriesFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		builder(ConventionalEnchantmentTags.INCREASE_BLOCK_DROPS)
				.add(Enchantments.FORTUNE);
		builder(ConventionalEnchantmentTags.INCREASE_ENTITY_DROPS)
				.add(Enchantments.LOOTING);
		builder(ConventionalEnchantmentTags.WEAPON_DAMAGE_ENHANCEMENTS)
				.add(Enchantments.SHARPNESS)
				.add(Enchantments.SMITE)
				.add(Enchantments.BANE_OF_ARTHROPODS)
				.add(Enchantments.POWER)
				.add(Enchantments.IMPALING);
		builder(ConventionalEnchantmentTags.ENTITY_SPEED_ENHANCEMENTS)
				.add(Enchantments.SOUL_SPEED)
				.add(Enchantments.SWIFT_SNEAK)
				.add(Enchantments.DEPTH_STRIDER);
		builder(ConventionalEnchantmentTags.ENTITY_AUXILIARY_MOVEMENT_ENHANCEMENTS)
				.add(Enchantments.FEATHER_FALLING)
				.add(Enchantments.FROST_WALKER);
		builder(ConventionalEnchantmentTags.ENTITY_DEFENSE_ENHANCEMENTS)
				.add(Enchantments.PROTECTION)
				.add(Enchantments.BLAST_PROTECTION)
				.add(Enchantments.PROJECTILE_PROTECTION)
				.add(Enchantments.FIRE_PROTECTION)
				.add(Enchantments.RESPIRATION)
				.add(Enchantments.FEATHER_FALLING);
		builder(ConventionalEnchantmentTags.HIDDEN_FROM_RECIPE_VIEWERS); // Generate tag so others can see it exists through JSON.
	}
}
