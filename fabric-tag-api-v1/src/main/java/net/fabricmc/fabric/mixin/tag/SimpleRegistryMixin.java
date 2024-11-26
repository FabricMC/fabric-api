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

package net.fabricmc.fabric.mixin.tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;

import net.fabricmc.fabric.impl.tag.SimpleRegistryExtension;
import net.fabricmc.fabric.impl.tag.TagAliasEnabledRegistryWrapper;

/**
 * Adds tag alias support to {@code SimpleRegistry}, the primary registry implementation.
 *
 * <p>Additionally, the {@link TagAliasEnabledRegistryWrapper} implementation is for dynamic registry tag loading.
 */
@Mixin(SimpleRegistry.class)
abstract class SimpleRegistryMixin<T> implements SimpleRegistryExtension, TagAliasEnabledRegistryWrapper {
	@Unique
	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-tag-api-v1");

	@Unique
	private Map<TagKey<?>, Set<TagKey<?>>> pendingTagAliasGroups;

	@Shadow
	@Final
	private RegistryKey<? extends Registry<T>> key;

	@Shadow
	SimpleRegistry.TagLookup<T> tagLookup;

	@Shadow
	protected abstract RegistryEntryList.Named<T> createNamedEntryList(TagKey<T> tag);

	@Shadow
	abstract void refreshTags();

	@Shadow
	public abstract RegistryKey<? extends Registry<T>> getKey();

	@Override
	public void fabric_loadTagAliases(Map<TagKey<?>, Set<TagKey<?>>> aliasGroups) {
		pendingTagAliasGroups = aliasGroups;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void fabric_applyPendingTagAliases() {
		if (pendingTagAliasGroups == null) return;

		Set<Set<TagKey<?>>> uniqueAliasGroups = Sets.newIdentityHashSet();
		uniqueAliasGroups.addAll(pendingTagAliasGroups.values());

		for (Set<TagKey<?>> aliasGroup : uniqueAliasGroups) {
			Set<RegistryEntry<T>> entries = Sets.newIdentityHashSet();

			// Fetch all entries from each tag.
			for (TagKey<?> tag : aliasGroup) {
				RegistryEntryList.Named<T> entryList = tagLookup.getOptional((TagKey<T>) tag).orElse(null);

				if (entryList != null) {
					entries.addAll(entryList.entries);
				} else {
					LOGGER.info("[Fabric] Creating a new empty tag {} for unknown tag used in a tag alias group in {}", tag.id(), tag.registryRef().getValue());
					Map<TagKey<T>, RegistryEntryList.Named<T>> tagMap = ((SimpleRegistryTagLookup2Accessor<T>) tagLookup).fabric_getTagMap();

					if (!(tagMap instanceof HashMap<?, ?>)) {
						// Unfreeze the backing map.
						tagMap = new HashMap<>(tagMap);
						((SimpleRegistryTagLookup2Accessor<T>) tagLookup).fabric_setTagMap(tagMap);
					}

					tagMap.put((TagKey<T>) tag, createNamedEntryList((TagKey<T>) tag));
				}
			}

			List<RegistryEntry<T>> entriesAsList = List.copyOf(entries);

			// Replace the old entry list contents with the merged list.
			for (TagKey<?> tag : aliasGroup) {
				RegistryEntryList.Named<T> entryList = tagLookup.getOptional((TagKey<T>) tag).orElseThrow();
				entryList.entries = entriesAsList;
			}
		}

		LOGGER.debug("[Fabric] Loaded {} tag alias groups for {}", uniqueAliasGroups.size(), key.getValue());
		pendingTagAliasGroups = null;
	}

	@Override
	public void fabric_refreshTags() {
		refreshTags();
	}
}
