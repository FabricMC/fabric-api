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

package net.fabricmc.fabric.test.datagen.client;

import static net.fabricmc.fabric.test.datagen.DataGeneratorTestContent.MOD_ID;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

import net.fabricmc.fabric.api.client.datagen.v1.builder.SoundTypeBuilder;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricSoundsProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.JsonKeySortOrderCallback;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.fabricmc.fabric.test.datagen.DataGeneratorTestContent;

public class DataGeneratorClientTestEntrypoint implements DataGeneratorEntrypoint {
	@Override
	public void addJsonKeySortOrders(JsonKeySortOrderCallback callback) {
		callback.add("type", 100); // Force 'type' at the end
	}

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
		final FabricDataGenerator.Pack pack = dataGenerator.createBuiltinResourcePack(Identifier.fromNamespaceAndPath(MOD_ID, "example_builtin"));
		pack.addProvider(TestAtlasSourceProvider::new);
		pack.addProvider(TestModelProvider::new);
		pack.addProvider(TestSoundsProvider::new);
	}

	private static class TestAtlasSourceProvider extends FabricCodecDataProvider<List<SpriteSource>> {
		private TestAtlasSourceProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> holderFuture) {
			super(packOutput, holderFuture, PackOutput.Target.RESOURCE_PACK, "atlases", SpriteSources.FILE_CODEC);
		}

		@Override
		protected void configure(BiConsumer<Identifier, List<SpriteSource>> spriteProvider, HolderLookup.Provider registryLookup) {
			spriteProvider.accept(Identifier.fromNamespaceAndPath(MOD_ID, "atlas_source_test"), List.of(new DirectoryLister("example", "example/")));
		}

		@Override
		public String getName() {
			return "Atlas Sources";
		}
	}

	private static class TestModelProvider extends FabricModelProvider {
		private TestModelProvider(FabricPackOutput output) {
			super(output);
		}

		@Override
		public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {
			blockModelGenerators.createTrivialCube(DataGeneratorTestContent.SIMPLE_BLOCK);
			blockModelGenerators.createTrivialCube(DataGeneratorTestContent.BLOCK_WITHOUT_ITEM);
			blockModelGenerators.createTrivialCube(DataGeneratorTestContent.BLOCK_WITHOUT_LOOT_TABLE);
			blockModelGenerators.createTrivialCube(DataGeneratorTestContent.BLOCK_WITH_VANILLA_LOOT_TABLE);
			blockModelGenerators.createTrivialCube(DataGeneratorTestContent.BLOCK_THAT_DROPS_NOTHING);
		}

		@Override
		public void generateItemModels(ItemModelGenerators itemModelGenerators) {
			//itemModelGenerator.register(item, Models.SLAB);
		}
	}

	private static class TestSoundsProvider extends FabricSoundsProvider {
		private TestSoundsProvider(PackOutput output, CompletableFuture<Provider> registryLookupFuture) {
			super(output, registryLookupFuture);
		}

		@Override
		public String getName() {
			return "Test Sound Events";
		}

		@Override
		protected void configure(HolderLookup.Provider registryLookup, SoundExporter exporter) {
			exporter.add(DataGeneratorTestContent.TEST_SOUND, SoundTypeBuilder.of(DataGeneratorTestContent.TEST_SOUND)
					.sound(SoundTypeBuilder.RegistrationBuilder.ofFile(Identifier.withDefaultNamespace("mob/parrot/idle"))
						.volume(0.7F), 1)
					.sound(SoundTypeBuilder.RegistrationBuilder.ofFile(Identifier.withDefaultNamespace("mob/parrot/idle2")))
					.sound(SoundTypeBuilder.RegistrationBuilder.ofEvent(SoundEvents.ANVIL_HIT))
					.sound(SoundTypeBuilder.RegistrationBuilder.ofEvent(SoundEvents.ARMOR_EQUIP_GENERIC))
					.sound(SoundTypeBuilder.RegistrationBuilder.ofFile(Identifier.withDefaultNamespace("mob/parrot/idle"))
						.volume(0.3F).pitch(0.5F).stream(true).preload(true).attenuationDistance(8)
					).replace(true)
			);
		}
	}
}
