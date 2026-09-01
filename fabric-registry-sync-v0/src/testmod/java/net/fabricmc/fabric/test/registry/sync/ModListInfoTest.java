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

package net.fabricmc.fabric.test.registry.sync;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.world.level.storage.LevelResource;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

/**
 * Verifies that {@code mod-list.json} is written to the world directory
 * after a save and that it contains valid, non-empty data.
 */
public class ModListInfoTest implements ModInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String FILE_NAME = "mod-list.json";
	private boolean hasRun;

	@Override
	public void onInitialize() {
		// Run the validation after the first save completes.
		ServerLifecycleEvents.AFTER_SAVE.register((server, flush, force) -> {
			if (this.hasRun) {
				return;
			}

			Path filePath = server.getWorldPath(LevelResource.ROOT).resolve("fabric").resolve(FILE_NAME);

			if (!Files.exists(filePath)) {
				throw new AssertionError(FILE_NAME + " was not created at: " + filePath);
			}

			try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
				JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

				if (!root.has("mod_count")) {
					throw new AssertionError(FILE_NAME + " is missing 'mod_count' field");
				}

				if (!root.has("mods")) {
					throw new AssertionError(FILE_NAME + " is missing 'mods' field");
				}

				JsonArray mods = root.getAsJsonArray("mods");
				int modCount = root.get("mod_count").getAsInt();

				Set<String> jsonIds = new HashSet<>();
				collectModIds(mods, jsonIds);

				Set<String> loadedIds = new HashSet<>();

				for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
					loadedIds.add(container.getMetadata().getId());
				}

				if (jsonIds.size() != loadedIds.size()) {
					throw new AssertionError("JSON mod count (" + jsonIds.size() + ") does not match loaded mod count (" + loadedIds.size() + ")");
				}

				for (String id : loadedIds) {
					if (!jsonIds.contains(id)) {
						throw new AssertionError("Mod loaded but not listed in " + FILE_NAME + ": " + id);
					}
				}

				LOGGER.info("PASSED. {} entries written to {}", modCount, FILE_NAME);
				this.hasRun = true;
			} catch (IOException e) {
				throw new AssertionError("Failed to read or parse " + FILE_NAME, e);
			}
		});
	}

	private static void collectModIds(JsonArray mods, Set<String> ids) {
		for (int i = 0; i < mods.size(); i++) {
			JsonObject mod = mods.get(i).getAsJsonObject();

			if (!mod.has("id") || mod.get("id").getAsString().isBlank()) {
				throw new AssertionError("Mod entry is missing a valid 'id'");
			}

			if (!mod.has("version") || mod.get("version").getAsString().isBlank()) {
				throw new AssertionError("Mod entry is missing a valid 'version'");
			}

			if (!mod.has("environment") || mod.get("environment").getAsString().isBlank()) {
				throw new AssertionError("Mod entry is missing a valid 'environment'");
			}

			ids.add(mod.get("id").getAsString());

			if (mod.has("children")) {
				collectModIds(mod.getAsJsonArray("children"), ids);
			}
		}
	}
}
