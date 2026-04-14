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
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityTypeIds;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEntityTypeTags;

public final class EntityTypeTagsGenerator extends FabricTagsProvider.EntityTypeTagsProvider {
	public EntityTypeTagsGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		builder(ConventionalEntityTypeTags.BOSSES)
				.add(EntityTypeIds.ENDER_DRAGON)
				.add(EntityTypeIds.WITHER);
		builder(ConventionalEntityTypeTags.MINECARTS)
				.add(EntityTypeIds.MINECART)
				.add(EntityTypeIds.TNT_MINECART)
				.add(EntityTypeIds.CHEST_MINECART)
				.add(EntityTypeIds.FURNACE_MINECART)
				.add(EntityTypeIds.COMMAND_BLOCK_MINECART)
				.add(EntityTypeIds.HOPPER_MINECART)
				.add(EntityTypeIds.SPAWNER_MINECART);
		builder(ConventionalEntityTypeTags.BOATS)
				.addOptionalTag(EntityTypeTags.BOAT)
				.add(EntityTypeIds.OAK_CHEST_BOAT)
				.add(EntityTypeIds.SPRUCE_CHEST_BOAT)
				.add(EntityTypeIds.BIRCH_CHEST_BOAT)
				.add(EntityTypeIds.JUNGLE_CHEST_BOAT)
				.add(EntityTypeIds.ACACIA_CHEST_BOAT)
				.add(EntityTypeIds.CHERRY_CHEST_BOAT)
				.add(EntityTypeIds.PALE_OAK_CHEST_BOAT)
				.add(EntityTypeIds.DARK_OAK_CHEST_BOAT)
				.add(EntityTypeIds.MANGROVE_CHEST_BOAT)
				.add(EntityTypeIds.BAMBOO_CHEST_RAFT);
		builder(ConventionalEntityTypeTags.ITEM_FRAMES)
				.add(EntityTypeIds.ITEM_FRAME)
				.add(EntityTypeIds.GLOW_ITEM_FRAME);
		builder(ConventionalEntityTypeTags.CAPTURING_NOT_SUPPORTED);
		builder(ConventionalEntityTypeTags.TELEPORTING_NOT_SUPPORTED);
	}
}
