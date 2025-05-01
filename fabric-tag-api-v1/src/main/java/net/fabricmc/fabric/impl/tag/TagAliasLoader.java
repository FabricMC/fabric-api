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

import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.profiler.Profiler;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;

public final class TagAliasLoader extends SinglePreparationResourceReloader<Map<RegistryKey<? extends Registry<?>>, List<TagAliasLoader.Data>>> implements IdentifiableResourceReloadListener {
	public static final Identifier ID = Identifier.of("fabric-tag-api-v1", "tag_alias_groups");

	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-tag-api-v1");
	private final RegistryWrapper.WrapperLookup registries;

	public TagAliasLoader(RegistryWrapper.WrapperLookup registries) {
		this.registries = registries;
	}

	@Override
	public Identifier getFabricId() {
		return ID;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Map<RegistryKey<? extends Registry<?>>, List<TagAliasLoader.Data>> prepare(ResourceManager manager, Profiler profiler) {
		Map<RegistryKey<? extends Registry<?>>, List<TagAliasLoader.Data>> dataByRegistry = new HashMap<>();
		Iterator<RegistryKey<? extends Registry<?>>> registryIterator = registries.streamAllRegistryKeys().iterator();

		while (registryIterator.hasNext()) {
			RegistryKey<? extends Registry<?>> registryKey = registryIterator.next();
			ResourceFinder resourceFinder = ResourceFinder.json(getDirectory(registryKey));

			for (Map.Entry<Identifier, Resource> entry : resourceFinder.findResources(manager).entrySet()) {
				Identifier resourcePath = entry.getKey();
				Identifier groupId = resourceFinder.toResourceId(resourcePath);

				try (Reader reader = entry.getValue().getReader()) {
					JsonElement json = StrictJsonParser.parse(reader);
					Codec<TagAliasGroup<Object>> codec = TagAliasGroup.codec((RegistryKey<? extends Registry<Object>>) registryKey);

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

	private static String getDirectory(RegistryKey<? extends Registry<?>> registryKey) {
		String directory = "fabric/tag_alias/";
		Identifier registryId = registryKey.getValue();

		if (!Identifier.DEFAULT_NAMESPACE.equals(registryId.getNamespace())) {
			directory += registryId.getNamespace() + '/';
		}

		return directory + registryId.getPath();
	}

	@Override
	protected void apply(Map<RegistryKey<? extends Registry<?>>, List<TagAliasLoader.Data>> prepared, ResourceManager manager, Profiler profiler) {
		for (Map.Entry<RegistryKey<? extends Registry<?>>, List<Data>> entry : prepared.entrySet()) {
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

			RegistryWrapper.Impl<?> wrapper = registries.getOrThrow(entry.getKey());

			if (wrapper instanceof TagAliasEnabledRegistryWrapper aliasWrapper) {
				aliasWrapper.fabric_loadTagAliases(groupsByTag);
			} else {
				throw new ClassCastException("[Fabric] Couldn't apply tag aliases to registry wrapper %s (%s) since it doesn't implement TagAliasEnabledRegistryWrapper"
						.formatted(wrapper, entry.getKey().getValue()));
			}
		}
	}

	public static <T> void applyToDynamicRegistries(CombinedDynamicRegistries<T> registries, T phase) {
		Iterator<DynamicRegistryManager.Entry<?>> registryEntries = registries.get(phase).streamAllRegistries().iterator();

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
