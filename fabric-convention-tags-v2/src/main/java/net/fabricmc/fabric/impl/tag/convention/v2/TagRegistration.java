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

package net.fabricmc.fabric.impl.tag.convention.v2;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluid;

import net.fabricmc.fabric.api.tag.convention.v2.TagUtil;

public record TagRegistration<T>(ResourceKey<Registry<T>> registryKey) {
	public static final TagRegistration<Item> ITEM_TAG = new TagRegistration<>(Registries.ITEM);
	public static final TagRegistration<Block> BLOCK_TAG = new TagRegistration<>(Registries.BLOCK);
	public static final TagRegistration<Biome> BIOME_TAG = new TagRegistration<>(Registries.BIOME);
	public static final TagRegistration<Structure> STRUCTURE_TAG = new TagRegistration<>(Registries.STRUCTURE);
	public static final TagRegistration<Fluid> FLUID_TAG = new TagRegistration<>(Registries.FLUID);
	public static final TagRegistration<EntityType<?>> ENTITY_TYPE_TAG = new TagRegistration<>(Registries.ENTITY_TYPE);
	public static final TagRegistration<Enchantment> ENCHANTMENT_TAG = new TagRegistration<>(Registries.ENCHANTMENT);

	public TagKey<T> registerFabric(String tagId) {
		return TagKey.create(registryKey, Identifier.fromNamespaceAndPath(TagUtil.FABRIC_TAG_NAMESPACE, tagId));
	}

	public TagKey<T> registerC(String tagId) {
		return TagKey.create(registryKey, Identifier.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, tagId));
	}
}
