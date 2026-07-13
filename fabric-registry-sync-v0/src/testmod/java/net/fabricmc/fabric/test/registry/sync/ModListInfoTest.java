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

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.world.level.storage.LevelResource;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

/**
 * Verifies that {@code fabricModList.json} is written to the world directory
 * after a save and that it contains valid, non-empty data.
 */
public class ModListInfoTest implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("ModListInfoTest");
	private static final String FILE_NAME = "fabricModList.json";
	private boolean hasRun = false;

	@Override
	public void onInitialize() {
		// Run the validation after the first save completes.
		ServerLifecycleEvents.AFTER_SAVE.register((server, flush, force) -> {
			if (this.hasRun) {
				return;
			}

			Path filePath = server.getWorldPath(LevelResource.ROOT).resolve("fabric").resolve(FILE_NAME);

			if (!Files.exists(filePath)) {
				throw new AssertionError("[ModListInfoTest] " + FILE_NAME + " was not created at: " + filePath);
			}

			try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
				JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

				if (!root.has("modCount")) {
					throw new AssertionError("[ModListInfoTest] fabricModList.json is missing 'modCount' field");
				}

				if (!root.has("mods")) {
					throw new AssertionError("[ModListInfoTest] fabricModList.json is missing 'mods' field");
				}

				JsonArray mods = root.getAsJsonArray("mods");
				int modCount = root.get("modCount").getAsInt();

				if (mods.size() == 0) {
					throw new AssertionError("[ModListInfoTest] fabricModList.json 'mods' array is empty");
				}

				if (mods.size() != modCount) {
					throw new AssertionError("[ModListInfoTest] fabricModList.json 'modCount' (" + modCount + ") does not match actual mod array size (" + mods.size() + ")");
				}

				// Verify each entry has the required fields.
				for (int i = 0; i < mods.size(); i++) {
					JsonObject mod = mods.get(i).getAsJsonObject();

					if (!mod.has("id") || mod.get("id").getAsString().isBlank()) {
						throw new AssertionError("[ModListInfoTest] Mod entry at index " + i + " is missing a valid 'id'");
					}

					if (!mod.has("version") || mod.get("version").getAsString().isBlank()) {
						throw new AssertionError("[ModListInfoTest] Mod entry at index " + i + " is missing a valid 'version'");
					}
				}

				LOGGER.info("[ModListInfoTest] PASSED. {} entries written to {}", modCount, FILE_NAME);
				this.hasRun = true;
			} catch (Exception e) {
				throw new AssertionError("[ModListInfoTest] Failed to read or parse " + FILE_NAME, e);
			}
		});
	}
}
