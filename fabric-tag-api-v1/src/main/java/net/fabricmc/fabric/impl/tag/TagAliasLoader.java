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

package net.fabricmc.fabric.impl.tag;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StrictJsonParser;

import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.SimpleResourceReloader;

public final class TagAliasLoader extends SimpleResourceReloader<Map<ResourceKey<? extends Registry<?>>, List<TagAliasLoader.Data>>> {
	public static final Identifier ID = Identifier.fromNamespaceAndPath("fabric-tag-api-v1", "tag_alias_groups");

	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-tag-api-v1");

	@SuppressWarnings("unchecked")
	@Override
	protected Map<ResourceKey<? extends Registry<?>>, List<TagAliasLoader.Data>> prepare(SharedState store) {
		Map<ResourceKey<? extends Registry<?>>, List<TagAliasLoader.Data>> dataByRegistry = new HashMap<>();
		HolderLookup.Provider registries = store.get(ResourceLoader.RELOADER_REGISTRY_LOOKUP_KEY);
		Iterator<ResourceKey<? extends Registry<?>>> registryIterator = registries.listRegistryKeys().iterator();

		while (registryIterator.hasNext()) {
			ResourceKey<? extends Registry<?>> registryKey = registryIterator.next();
			FileToIdConverter resourceFinder = FileToIdConverter.json(getDirectory(registryKey));

			for (Map.Entry<Identifier, Resource> entry : resourceFinder.listMatchingResources(store.resourceManager()).entrySet()) {
				Identifier resourcePath = entry.getKey();
				Identifier groupId = resourceFinder.fileToId(resourcePath);

				try (Reader reader = entry.getValue().openAsReader()) {
					JsonElement json = StrictJsonParser.parse(reader);
					Codec<TagAliasGroup<Object>> codec = TagAliasGroup.codec((ResourceKey<? extends Registry<Object>>) registryKey);

					switch (codec.parse(JsonOps.INSTANCE, json)) {
					case DataResult.Success(TagAliasGroup<Object> group, Lifecycle unused) -> {
						var data = new Data(groupId, group);
						dataByRegistry.computeIfAbsent(registryKey, key -> new ArrayList<>()).add(data);
					}
					case DataResult.Error<?> error -> {
						LOGGER.error("[Fabric] Couldn't parse tag alias group file '{}' from '{}': {}", groupId, resourcePath, error.message());
					}
					}
				} catch (IOException | JsonParseException e) {
					LOGGER.error("[Fabric] Couldn't parse tag alias group file '{}' from '{}'", groupId, resourcePath, e);
				}
			}
		}

		return dataByRegistry;
	}

	private static String getDirectory(ResourceKey<? extends Registry<?>> registryKey) {
		String directory = "fabric/tag_alias/";
		Identifier registryId = registryKey.identifier();

		if (!Identifier.DEFAULT_NAMESPACE.equals(registryId.getNamespace())) {
			directory += registryId.getNamespace() + '/';
		}

		return directory + registryId.getPath();
	}

	@Override
	protected void apply(Map<ResourceKey<? extends Registry<?>>, List<TagAliasLoader.Data>> prepared, SharedState store) {
		for (Map.Entry<ResourceKey<? extends Registry<?>>, List<Data>> entry : prepared.entrySet()) {
			Map<TagKey<?>, Set<TagKey<?>>> groupsByTag = new HashMap<>();

			for (Data data : entry.getValue()) {
				Set<TagKey<?>> group = new HashSet<>(data.group.tags());

				for (TagKey<?> tag : data.group.tags()) {
					Set<TagKey<?>> oldGroup = groupsByTag.get(tag);

					// If there's an old group...
					if (oldGroup != null) {
						// ...merge all of its tags into the current group...
						group.addAll(oldGroup);

						// ...and replace the recorded group of each tag in the old group with the new group.
						for (TagKey<?> other : oldGroup) {
							groupsByTag.put(other, group);
						}
					}

					groupsByTag.put(tag, group);
				}
			}

			// Remove any groups of one tag, we don't need to apply them.
			groupsByTag.values().removeIf(tags -> tags.size() == 1);

			HolderLookup.RegistryLookup<?> wrapper = store.get(ResourceLoader.RELOADER_REGISTRY_LOOKUP_KEY).lookupOrThrow(entry.getKey());

			if (wrapper instanceof TagAliasEnabledRegistryWrapper aliasWrapper) {
				aliasWrapper.fabric_loadTagAliases(groupsByTag);
			} else {
				throw new ClassCastException("[Fabric] Couldn't apply tag aliases to registry wrapper %s (%s) since it doesn't implement TagAliasEnabledRegistryWrapper"
						.formatted(wrapper, entry.getKey().identifier()));
			}
		}
	}

	public static <T> void applyToDynamicRegistries(LayeredRegistryAccess<T> registries, T phase) {
		Iterator<RegistryAccess.RegistryEntry<?>> registryEntries = registries.getLayer(phase).registries().iterator();

		while (registryEntries.hasNext()) {
			Registry<?> registry = registryEntries.next().value();

			if (registry instanceof SimpleRegistryExtension extension) {
				extension.fabric_applyPendingTagAliases();
				// This is not needed in the static registry code path as the tag aliases are applied
				// before the tags are refreshed. Dynamic registry loading (including tags) takes place earlier
				// than the rest of a data reload, so we need to refresh the tags manually.
				extension.fabric_refreshTags();
			} else {
				throw new ClassCastException("[Fabric] Couldn't apply pending tag aliases to registry %s (%s) since it doesn't implement SimpleRegistryExtension"
						.formatted(registry, registry.getClass().getName()));
			}
		}
	}

	protected record Data(Identifier groupId, TagAliasGroup<?> group) {
	}
}
