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

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import net.fabricmc.fabric.impl.tag.SimpleRegistryExtension;
import net.fabricmc.fabric.impl.tag.TagAliasEnabledRegistryWrapper;

/**
 * Adds tag alias support to {@code SimpleRegistry}, the primary registry implementation.
 *
 * <p>Additionally, the {@link TagAliasEnabledRegistryWrapper} implementation is for dynamic registry tag loading.
 */
@Mixin(MappedRegistry.class)
abstract class MappedRegistryMixin<T> implements SimpleRegistryExtension, TagAliasEnabledRegistryWrapper {
	@Unique
	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-tag-api-v1");

	@Unique
	private Map<TagKey<?>, Set<TagKey<?>>> pendingTagAliasGroups;

	@Shadow
	@Final
	private ResourceKey<? extends Registry<T>> key;

	@Shadow
	MappedRegistry.TagSet<T> allTags;

	@Shadow
	protected abstract HolderSet.Named<T> createTag(TagKey<T> tag);

	@Shadow
	abstract void refreshTagsInHolders();

	@Shadow
	public abstract ResourceKey<? extends Registry<T>> key();

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
			Set<Holder<T>> entries = Sets.newIdentityHashSet();

			// Fetch all entries from each tag.
			for (TagKey<?> tag : aliasGroup) {
				HolderSet.Named<T> entryList = allTags.get((TagKey<T>) tag).orElse(null);

				if (entryList != null) {
					entries.addAll(entryList.contents);
				} else {
					LOGGER.info("[Fabric] Creating a new empty tag {} for unknown tag used in a tag alias group in {}", tag.location(), tag.registry().identifier());
					Map<TagKey<T>, HolderSet.Named<T>> tagMap = ((SimpleRegistryTagLookup2Accessor<T>) allTags).fabric_getTagMap();

					if (!(tagMap instanceof HashMap<?, ?>)) {
						// Unfreeze the backing map.
						tagMap = new HashMap<>(tagMap);
						((SimpleRegistryTagLookup2Accessor<T>) allTags).fabric_setTagMap(tagMap);
					}

					tagMap.put((TagKey<T>) tag, createTag((TagKey<T>) tag));
				}
			}

			List<Holder<T>> entriesAsList = List.copyOf(entries);

			// Replace the old entry list contents with the merged list.
			for (TagKey<?> tag : aliasGroup) {
				HolderSet.Named<T> entryList = allTags.get((TagKey<T>) tag).orElseThrow();
				entryList.contents = entriesAsList;
			}
		}

		LOGGER.debug("[Fabric] Loaded {} tag alias groups for {}", uniqueAliasGroups.size(), key.identifier());
		pendingTagAliasGroups = null;
	}

	@Override
	public void fabric_refreshTags() {
		refreshTagsInHolders();
	}
}
