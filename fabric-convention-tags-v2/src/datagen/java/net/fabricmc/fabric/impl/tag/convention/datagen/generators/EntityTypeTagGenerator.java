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

import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.EntityTypeTags;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEntityTypeTags;

public final class EntityTypeTagGenerator extends FabricTagProvider.EntityTypeTagProvider {
	public EntityTypeTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	protected void configure(RegistryWrapper.WrapperLookup registries) {
		valueLookupBuilder(ConventionalEntityTypeTags.BOSSES)
				.add(EntityType.ENDER_DRAGON)
				.add(EntityType.WITHER);
		valueLookupBuilder(ConventionalEntityTypeTags.MINECARTS)
				.add(EntityType.MINECART)
				.add(EntityType.TNT_MINECART)
				.add(EntityType.CHEST_MINECART)
				.add(EntityType.FURNACE_MINECART)
				.add(EntityType.COMMAND_BLOCK_MINECART)
				.add(EntityType.HOPPER_MINECART)
				.add(EntityType.SPAWNER_MINECART);
		valueLookupBuilder(ConventionalEntityTypeTags.BOATS)
				.addOptionalTag(EntityTypeTags.BOAT)
				.add(EntityType.OAK_CHEST_BOAT)
				.add(EntityType.SPRUCE_CHEST_BOAT)
				.add(EntityType.BIRCH_CHEST_BOAT)
				.add(EntityType.JUNGLE_CHEST_BOAT)
				.add(EntityType.ACACIA_CHEST_BOAT)
				.add(EntityType.CHERRY_CHEST_BOAT)
				.add(EntityType.PALE_OAK_CHEST_BOAT)
				.add(EntityType.DARK_OAK_CHEST_BOAT)
				.add(EntityType.MANGROVE_CHEST_BOAT)
				.add(EntityType.BAMBOO_CHEST_RAFT);
		valueLookupBuilder(ConventionalEntityTypeTags.CAPTURING_NOT_SUPPORTED);
		valueLookupBuilder(ConventionalEntityTypeTags.TELEPORTING_NOT_SUPPORTED);
	}
}
