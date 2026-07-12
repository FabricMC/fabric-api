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

package net.fabricmc.fabric.impl.modlist;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

/**
 * Saves the list of currently active mods and their versions into the world's
 * save directory as {@code fabricModList.json} after the world is saved.
 *
 * <p>The file is written to {@code <world_dir>/fabricModList.json}. It is
 * ignored by vanilla Minecraft and can be used to reconstruct the modpack
 * that was used when a world was last played.
 */
public final class ModListSaver {
	private static final Logger LOGGER = LoggerFactory.getLogger("FabricModListInfo");
	private static final String FILE_NAME = "fabricModList.json";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private ModListSaver() {
	}

	/**
	 * Collects the active mod list and writes it to the world directory.
	 *
	 * @param server the server whose world directory to write into
	 */
	public static void save(MinecraftServer server) {
		Objects.requireNonNull(server, "server");

		Path outputPath = server.storageSource.getLevelPath(LevelResource.ROOT).resolve(FILE_NAME);

		JsonObject root = new JsonObject();

		// Collect all top-level mods (not embedded ones), sorted by mod id.
		List<ModContainer> mods = new ArrayList<>();

		for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
			if (container.getContainingMod().isEmpty()) {
				mods.add(container);
			}
		}

		mods.sort(Comparator.comparing(mod -> mod.getMetadata().getId()));

		JsonArray modArray = new JsonArray();

		for (ModContainer mod : mods) {
			JsonObject entry = new JsonObject();
			entry.addProperty("id", mod.getMetadata().getId());
			entry.addProperty("name", mod.getMetadata().getName());
			entry.addProperty("version", mod.getMetadata().getVersion().getFriendlyString());

			// Also list any embedded/child mods (e.g. Fabric API sub-modules).
			if (!mod.getContainedMods().isEmpty()) {
				JsonArray children = new JsonArray();

				List<ModContainer> childMods = new ArrayList<>(mod.getContainedMods());
				childMods.sort(Comparator.comparing(child -> child.getMetadata().getId()));

				for (ModContainer child : childMods) {
					JsonObject childEntry = new JsonObject();
					childEntry.addProperty("id", child.getMetadata().getId());
					childEntry.addProperty("version", child.getMetadata().getVersion().getFriendlyString());
					children.add(childEntry);
				}

				entry.add("children", children);
			}

			modArray.add(entry);
		}

		root.addProperty("modCount", mods.size());
		root.add("mods", modArray);

		try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
			GSON.toJson(root, writer);
		} catch (IOException e) {
			LOGGER.error("Failed to write {} to {}", FILE_NAME, outputPath, e);
		}
	}
}
