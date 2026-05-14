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
import com.google.gson.JsonParser;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagEntry;
import net.minecraft.registry.tag.TagFile;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.tag.TagManagerLoader;
import net.minecraft.util.Identifier;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class ClientTagsLoader {
	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-client-tags-api-v1");
	/**
	 * Load a given tag from the available mods into a set of {@code Identifier}s.
	 * Parsing based on {@link net.minecraft.registry.tag.TagGroupLoader#loadTags(net.minecraft.resource.ResourceManager)}
	 */
	public static LoadedTag loadTag(TagKey<?> tagKey) {
		var values = new HashSet<TagEntry>();
		var remove = new HashSet<TagEntry>();
		HashSet<Path> tagFiles = getTagFiles(tagKey.registry(), tagKey.id());

		for (Path tagPath : tagFiles) {
			try (BufferedReader tagReader = Files.newBufferedReader(tagPath)) {
				JsonElement jsonElement = JsonParser.parseReader(tagReader);
				TagFile maybeTagFile = TagFile.CODEC.parse(new Dynamic<>(JsonOps.INSTANCE, jsonElement))
						.result().orElse(null);

				if (maybeTagFile != null) {
					if (maybeTagFile.replace()) {
						values.clear();
						remove.clear();
					}

					values.addAll(maybeTagFile.entries());
					remove.addAll(maybeTagFile.remove());
				}
			} catch (IOException e) {
				LOGGER.error("Error loading tag: {}", tagKey, e);
			}
		}

		HashSet<Identifier> completeIds = new HashSet<>();
		HashSet<Identifier> removeIds = new HashSet<>();
		HashSet<Identifier> immediateChildIds = new HashSet<>();
		HashSet<TagKey<?>> immediateChildTags = new HashSet<>();

		for (TagEntry tagEntry : values) {
			tagEntry.resolve(new TagEntry.ValueGetter<Identifier>() {
				@Override
				public Identifier direct(Identifier id) {
					immediateChildIds.add(id);
					return id;
				}

				@Nullable
				@Override
				public Collection<Identifier> tag(Identifier id) {
					TagKey<?> tag = TagKey.of(tagKey.registry(), id);
					immediateChildTags.add(tag);
					return ClientTagsImpl.getOrCreatePartiallySyncedTag(tag).completeIds;
				}
			}, id -> {
				removeIds.remove(id);
				completeIds.add(id);
			});
		}

		for (TagEntry removeEntry : remove) {
			removeEntry.resolve(new TagEntry.ValueGetter<Identifier>() {
				@Override
				public @Nullable Identifier direct(Identifier id) {
					return id;
				}

				@Nullable
				@Override
				public Collection<Identifier> tag(Identifier id) {
					TagKey<?> tag = TagKey.of(tagKey.registry(), id);
					return ClientTagsImpl.getOrCreatePartiallySyncedTag(tag).removeIds;
				}
			}, id -> {
				completeIds.remove(id);
				removeIds.add(id);
			});
		}

		// Ensure that the tag does not refer to itself
		immediateChildTags.remove(tagKey);

		return new LoadedTag(
				Collections.unmodifiableSet(completeIds),
				Collections.unmodifiableSet(removeIds),
				Collections.unmodifiableSet(immediateChildTags),
				Collections.unmodifiableSet(immediateChildIds)
		);
	}

	public record LoadedTag(Set<Identifier> completeIds,
							Set<Identifier> removeIds,
							Set<TagKey<?>> immediateChildTags,
							Set<Identifier> immediateChildIds) {
	}

	/**
	 * @param registryKey the RegistryKey of the TagKey
	 * @param identifier  the Identifier of the tag
	 * @return the paths to all tag json files within the available mods
	 */
	private static HashSet<Path> getTagFiles(RegistryKey<? extends Registry<?>> registryKey, Identifier identifier) {
		return getTagFiles(TagManagerLoader.getPath(registryKey), identifier);
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
