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

package net.fabricmc.fabric.impl.tag.convention.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.impl.tag.convention.datagen.generators.BiomeTagsGenerator;
import net.fabricmc.fabric.impl.tag.convention.datagen.generators.BlockTagsGenerator;
import net.fabricmc.fabric.impl.tag.convention.datagen.generators.EnchantmentTagsGenerator;
import net.fabricmc.fabric.impl.tag.convention.datagen.generators.EnglishTagLangGenerator;
import net.fabricmc.fabric.impl.tag.convention.datagen.generators.EntityTypeTagsGenerator;
import net.fabricmc.fabric.impl.tag.convention.datagen.generators.FluidTagsGenerator;
import net.fabricmc.fabric.impl.tag.convention.datagen.generators.ItemTagsGenerator;
import net.fabricmc.fabric.impl.tag.convention.datagen.generators.PotionTagsGenerator;
import net.fabricmc.fabric.impl.tag.convention.datagen.generators.StructureTagsGenerator;

public class DatagenEntrypoint implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		final FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		BlockTagsGenerator blockTags = pack.addProvider(BlockTagsGenerator::new);
		pack.addProvider((output, registriesFuture) -> new ItemTagsGenerator(output, registriesFuture, blockTags));
		pack.addProvider(FluidTagsGenerator::new);
		pack.addProvider(EnchantmentTagsGenerator::new);
		pack.addProvider(PotionTagsGenerator::new);
		pack.addProvider(BiomeTagsGenerator::new);
		pack.addProvider(StructureTagsGenerator::new);
		pack.addProvider(EntityTypeTagsGenerator::new);
		pack.addProvider(EnglishTagLangGenerator::new);
	}

	@Override
	public String getEffectiveModId() {
		return "fabric-convention-tags-v2";
	}
}
