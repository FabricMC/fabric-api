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

package net.fabricmc.fabric.impl.tag.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonElement;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StrictJsonParser;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class ClientTagsLoader {
	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-client-tags-api-v1");
	/**
	 * Load a given tag from the available mods into a set of {@code Identifier}s.
	 * Parsing based on {@link net.minecraft.tags.TagLoader#loadTags(net.minecraft.server.packs.resources.ResourceManager)}
	 */
	public static LoadedTag loadTag(TagKey<?> tagKey) {
		var tags = new HashSet<TagEntry>();
		HashSet<Path> tagFiles = getTagFiles(tagKey.registry(), tagKey.location());

		for (Path tagPath : tagFiles) {
			try (BufferedReader tagReader = Files.newBufferedReader(tagPath)) {
				JsonElement jsonElement = StrictJsonParser.parse(tagReader);
				TagFile maybeTagFile = TagFile.CODEC.parse(new Dynamic<>(JsonOps.INSTANCE, jsonElement))
						.result().orElse(null);

				if (maybeTagFile != null) {
					if (maybeTagFile.replace()) {
						tags.clear();
					}

					tags.addAll(maybeTagFile.entries());
				}
			} catch (IOException e) {
				LOGGER.error("Error loading tag: " + tagKey, e);
			}
		}

		HashSet<Identifier> completeIds = new HashSet<>();
		HashSet<Identifier> immediateChildIds = new HashSet<>();
		HashSet<TagKey<?>> immediateChildTags = new HashSet<>();

		for (TagEntry tagEntry : tags) {
			tagEntry.build(new TagEntry.Lookup<>() {
				@Nullable
				@Override
				public Identifier element(Identifier id, boolean required) {
					immediateChildIds.add(id);
					return id;
				}

				@Nullable
				@Override
				public Collection<Identifier> tag(Identifier id) {
					TagKey<?> tag = TagKey.create(tagKey.registry(), id);
					immediateChildTags.add(tag);
					return ClientTagsImpl.getOrCreatePartiallySyncedTag(tag).completeIds;
				}
			}, completeIds::add);
		}

		// Ensure that the tag does not refer to itself
		immediateChildTags.remove(tagKey);

		return new LoadedTag(Collections.unmodifiableSet(completeIds), Collections.unmodifiableSet(immediateChildTags),
				Collections.unmodifiableSet(immediateChildIds));
	}

	public record LoadedTag(Set<Identifier> completeIds, Set<TagKey<?>> immediateChildTags, Set<Identifier> immediateChildIds) {
	}

	/**
	 * @param registryKey the RegistryKey of the TagKey
	 * @param identifier  the Identifier of the tag
	 * @return the paths to all tag json files within the available mods
	 */
	private static HashSet<Path> getTagFiles(ResourceKey<? extends Registry<?>> registryKey, Identifier identifier) {
		return getTagFiles(Registries.tagsDirPath(registryKey), identifier);
	}

	/**
	 * @return the paths to all tag json files within the available mods
	 */
	private static HashSet<Path> getTagFiles(String tagType, Identifier identifier) {
		String tagFile = "data/%s/%s/%s.json".formatted(identifier.getNamespace(), tagType, identifier.getPath());
		return getResourcePaths(tagFile);
	}

	/**
	 * @return all paths from the available mods that match the given internal path
	 */
	private static HashSet<Path> getResourcePaths(String path) {
		HashSet<Path> out = new HashSet<>();

		for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
			mod.findPath(path).ifPresent(out::add);
		}

		return out;
	}
}
