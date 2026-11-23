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

package net.fabricmc.fabric.impl.registry.sync;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

/**
 * Saves the list of currently active mods and their versions into the world's
 * save directory as {@code fabric/mod-list.json} after the world is saved.
 *
 * <p>The file is written to {@code <world_dir>/fabric/mod-list.json}. It is
 * ignored by vanilla Minecraft and can be used to reconstruct the modpack
 * that was used when a world was last played.
 */
public final class ModListSaver {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModListSaver.class);
	private static final String FILE_NAME = "mod-list.json";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	@Nullable
	private static volatile String cachedJson = null;

	private ModListSaver() {
	}

	/**
	 * Collects the active mod list and writes it atomically to the world directory.
	 *
	 * @param server the server whose world directory to write into
	 */
	public static void save(MinecraftServer server) {
		Objects.requireNonNull(server, "server");

		Path outputPath = server.getWorldPath(LevelResource.ROOT).resolve("fabric").resolve(FILE_NAME);

		try {
			Files.createDirectories(outputPath.getParent());
		} catch (IOException e) {
			LOGGER.error("Failed to create directory for {}", FILE_NAME, e);
			return;
		}

		String json = getOrBuildJson();
		writeToDisk(json, outputPath);
	}

	/**
	 * Returns the cached JSON representation of the mod list, building it on first call.
	 *
	 * <p>The result is cached because mod metadata never changes during a server session.
	 */
	private static String getOrBuildJson() {
		String json = cachedJson;

		if (json == null) {
			cachedJson = json = buildJson();
		}

		return json;
	}

	/**
	 * Serializes all loaded mods into a JSON object.
	 *
	 * <p>Only top-level mods appear in the root {@code mods} array; embedded mods are
	 * nested recursively under their parent's {@code children} array. The total count
	 * of all mods (including embedded ones) is stored in {@code mod_count}.
	 */
	private static String buildJson() {
		// Collect only top-level mods (those not embedded inside another mod).
		List<ModContainer> topLevelMods = new ArrayList<>();

		for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
			if (container.getContainingMod().isEmpty()) {
				topLevelMods.add(container);
			}
		}

		topLevelMods.sort(Comparator.comparing(mod -> mod.getMetadata().getId()));

		JsonArray modArray = new JsonArray();

		for (ModContainer mod : topLevelMods) {
			// toJson recurses into each mod's contained mods to build the full tree.
			modArray.add(toJson(mod));
		}

		JsonObject root = new JsonObject();
		root.addProperty("mod_count", FabricLoader.getInstance().getAllMods().size());
		root.add("mods", modArray);

		return GSON.toJson(root);
	}

	/**
	 * Recursively serializes a mod and all its embedded children into a JSON object.
	 *
	 * <p>Children (mods embedded via Jar-in-Jar) are sorted by mod id and nested
	 * under a {@code children} array, allowing the full mod tree to be represented.
	 */
	private static JsonObject toJson(ModContainer mod) {
		JsonObject entry = new JsonObject();
		entry.addProperty("id", mod.getMetadata().getId());
		entry.addProperty("name", mod.getMetadata().getName());
		entry.addProperty("version", mod.getMetadata().getVersion().getFriendlyString());
		entry.addProperty("environment", mod.getMetadata().getEnvironment().name().toLowerCase(Locale.ROOT));

		if (!mod.getContainedMods().isEmpty()) {
			JsonArray children = new JsonArray();

			List<ModContainer> childMods = new ArrayList<>(mod.getContainedMods());
			childMods.sort(Comparator.comparing(child -> child.getMetadata().getId()));

			for (ModContainer child : childMods) {
				children.add(toJson(child));
			}

			entry.add("children", children);
		}

		return entry;
	}

	/**
	 * Writes {@code json} to {@code outputPath} atomically via a temporary file,
	 * so that a crash mid-write never leaves a partially written file.
	 */
	private static void writeToDisk(String json, Path outputPath) {
		Path tempPath = outputPath.resolveSibling(FILE_NAME + ".tmp");

		try (Writer writer = Files.newBufferedWriter(tempPath, StandardCharsets.UTF_8)) {
			writer.write(json);
		} catch (IOException e) {
			LOGGER.error("Failed to write {} to {}", FILE_NAME, tempPath, e);
			return;
		}

		try {
			Files.move(tempPath, outputPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			LOGGER.error("Failed to move {} to {}", tempPath, outputPath, e);
		}
	}
}
