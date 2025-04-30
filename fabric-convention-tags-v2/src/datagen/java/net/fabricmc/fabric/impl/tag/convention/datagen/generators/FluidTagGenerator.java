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

import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.FluidTags;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalFluidTags;

public final class FluidTagGenerator extends FabricTagProvider.FluidTagProvider {
	public FluidTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	protected void configure(RegistryWrapper.WrapperLookup registries) {
		valueLookupBuilder(ConventionalFluidTags.WATER)
				.addOptionalTag(FluidTags.WATER);
		valueLookupBuilder(ConventionalFluidTags.LAVA)
				.addOptionalTag(FluidTags.LAVA);
		valueLookupBuilder(ConventionalFluidTags.MILK);
		valueLookupBuilder(ConventionalFluidTags.HONEY);
		valueLookupBuilder(ConventionalFluidTags.GASEOUS);
		valueLookupBuilder(ConventionalFluidTags.EXPERIENCE);
		valueLookupBuilder(ConventionalFluidTags.POTION);
		valueLookupBuilder(ConventionalFluidTags.SUSPICIOUS_STEW);
		valueLookupBuilder(ConventionalFluidTags.MUSHROOM_STEW);
		valueLookupBuilder(ConventionalFluidTags.RABBIT_STEW);
		valueLookupBuilder(ConventionalFluidTags.BEETROOT_SOUP);
		valueLookupBuilder(ConventionalFluidTags.HIDDEN_FROM_RECIPE_VIEWERS);
	}
}
