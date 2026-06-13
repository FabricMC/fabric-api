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

package net.fabricmc.fabric.test.holder.component.datagen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.references.ItemIds;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.animal.pig.PigVariants;
import net.minecraft.world.level.biome.Biomes;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.holder.component.v1.provider.DataHolderComponentProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;

public class HolderComponentTestModDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
		FabricDataGenerator.Pack pack = dataGenerator.createPack();
		pack.addProvider(TestDataHolderComponentProvider::new);
	}

	private static class TestDataHolderComponentProvider extends DataHolderComponentProvider {
		private TestDataHolderComponentProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(output, registriesFuture);
		}

		@Override
		protected void generate(HolderLookup.Provider registries) {
			this.builder(ItemIds.PUMPKIN_SEEDS)
					.newPatch()
							// Test basic component setting and removing.
							.set(DataComponents.PIG_VARIANT, registries.getOrThrow(PigVariants.TEMPERATE))
							.remove(DataComponents.ITEM_NAME)
					.newPatch()
							// Test resource condition testing. The test should fail, otherwise this
							// file won't load.
							.condition(ResourceConditions.allModsLoaded("foo"))
							.forceSet(Identifier.fromNamespaceAndPath("foo", "a"), StringTag.valueOf("a"))
							.forceRemove(Identifier.fromNamespaceAndPath("foo", "b"))
					.newPatch()
							// Test required field. foo:c is not a component but required=false will
							// cause only this patch to be skipped.
							.required(false)
							.forceSet(Identifier.fromNamespaceAndPath("foo", "c"), StringTag.valueOf("c"));

			this.builder(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("foo", "bar")))
					.replace(true)
					.withConditions(ResourceConditions.allModsLoaded("foo"));

			// Test every set method, each one of these should produce the same patch in JSON
			this.builder(Biomes.PLAINS)
					.newPatch()
							.set(DataComponents.UNBREAKABLE, Unit.INSTANCE)
					.newPatch()
							.set(new TypedDataComponent<>(DataComponents.UNBREAKABLE, Unit.INSTANCE))
					.newPatch()
							.set(DataComponentMap.builder().set(DataComponents.UNBREAKABLE, Unit.INSTANCE).build())
					.newPatch()
							.forceSet(Identifier.withDefaultNamespace("unbreakable"), new CompoundTag());

			// Test single-patch encoding when one patch without condition
			this.builder(Biomes.SUNFLOWER_PLAINS)
					.newPatch()
							.required(false)
							.set(DataComponents.CUSTOM_NAME, Component.literal("Single-patch encoding"));

			// Test multi-patch encoding when one patch with condition
			this.builder(Biomes.SNOWY_PLAINS)
					.newPatch()
							.required(false)
							.set(DataComponents.CUSTOM_NAME, Component.literal("Multi-patch encoding"))
							.condition(ResourceConditions.alwaysTrue());
		}
	}
}
