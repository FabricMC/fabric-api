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

package net.fabricmc.fabric.test.biome;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;

public class TestBiomeTagProvider extends FabricTagProvider<Biome> {
	public TestBiomeTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, Registries.BIOME, registriesFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		builder(TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(FabricBiomeTest.MOD_ID, "biome_tag_test")))
				.add(TestBiomes.CUSTOM_PLAINS)
				.add(TestBiomes.TEST_END_HIGHLANDS);
		builder(TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(FabricBiomeTest.MOD_ID, "tag_selector_test")))
				.add(Biomes.BEACH)
				.add(Biomes.DESERT)
				.add(Biomes.SAVANNA)
				.add(Biomes.BADLANDS);
	}
}
