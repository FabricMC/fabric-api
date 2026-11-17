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

package net.fabricmc.fabric.test.biome.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Copies every biome on the world creation screen, with the test mod's modifications applied, then creates the world
 * and checks each copy against the biome the server has modified.
 */
public class ModifiedCopyClientTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		Map<ResourceKey<Biome>, JsonElement> copies = new HashMap<>();
		List<ResourceKey<Biome>> changed = new ArrayList<>();

		try (TestSingleplayerContext singleplayer = context.worldBuilder().adjustSettings(creator -> {
			// A normal world rather than the flat default, so the test mod's overworld modifications have biomes to select
			Holder<WorldPreset> normal = creator.getSettings().worldgenLoadContext().lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.NORMAL);
			creator.setWorldType(new WorldCreationUiState.WorldTypeEntry(normal));

			// The screen's registries with the dimensions the player has chosen, as BiomeModifications documents
			WorldCreationContext settings = creator.getSettings();
			RegistryAccess registries = settings.worldgenRegistries()
					.replaceFrom(RegistryLayer.DIMENSIONS, settings.selectedDimensions().bake(settings.datapackDimensions()).dimensionsRegistryAccess())
					.compositeAccess();

			for (Holder.Reference<Biome> biome : registries.lookupOrThrow(Registries.BIOME).listElements().toList()) {
				JsonElement copy = encode(registries, BiomeModifications.createModifiedCopy(registries, biome.key()));
				copies.put(biome.key(), copy);

				if (!copy.equals(encode(registries, biome.value()))) {
					changed.add(biome.key());
				}
			}
		}).create()) {
			if (changed.isEmpty()) {
				throw new AssertionError("No copy made on the world creation screen had the test mod's modifications");
			}

			singleplayer.getServer().runOnServer(server -> {
				RegistryAccess registries = server.registryAccess();
				Registry<Biome> biomes = registries.lookupOrThrow(Registries.BIOME);
				List<String> mismatched = new ArrayList<>();

				for (Holder.Reference<Biome> biome : biomes.listElements().toList()) {
					JsonElement copy = copies.get(biome.key());

					if (copy == null || !copy.equals(encode(registries, biome.value()))) {
						mismatched.add(biome.key().identifier().toString());
					}
				}

				if (!mismatched.isEmpty()) {
					throw new AssertionError("Copies made on the world creation screen differ from the server's biomes: " + mismatched);
				}
			});
		}
	}

	private static JsonElement encode(RegistryAccess registries, Biome biome) {
		return Biome.DIRECT_CODEC.encodeStart(registries.createSerializationContext(JsonOps.INSTANCE), biome).getOrThrow();
	}
}
