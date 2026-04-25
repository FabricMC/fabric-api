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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.DependencySorter;

import net.fabricmc.fabric.api.tag.v1.FabricTagFile;

public class TagRemovalInternals {
	private static final ThreadLocal<Map<Identifier, List<TagLoader.EntryWithSource>>> REMOVE_ENTRIES = ThreadLocal.withInitial(HashMap::new);

	public static Codec<TagFile> modifyTagFileCodec(Codec<TagFile> originalCodec) {
		return RecordCodecBuilder.create(i -> i.group(
				MapCodec.assumeMapUnsafe(originalCodec)
						.forGetter(Function.identity()),
				TagEntry.CODEC
						.listOf()
						.lenientOptionalFieldOf("fabric:remove", Collections.emptyList())
						.forGetter(FabricTagFile::remove)
		).apply(i, (tagFile, remove) -> {
			((TagFileHooks) (Object) tagFile).fabric_setRemove(remove);
			return tagFile;
		}));
	}

	public static void replaceRemoveEntries(Identifier tagId) {
		Map<Identifier, List<TagLoader.EntryWithSource>> removeEntries = REMOVE_ENTRIES.get();

		if (removeEntries.containsKey(tagId)) {
			removeEntries.get(tagId).clear();
		}
	}

	public static void loadRemoveEntries(Identifier tagId, List<TagEntry> removeEntries, String sourceId) {
		List<TagLoader.EntryWithSource> entriesWithSources = new ArrayList<>();

		for (TagEntry entry : removeEntries) {
			entriesWithSources.add(new TagLoader.EntryWithSource(entry, sourceId));
		}

		REMOVE_ENTRIES.get().put(tagId, entriesWithSources);
	}

	public static <T> Map<Identifier, List<T>> removeEntriesFromTags(Map<Identifier, List<T>> newTags, TagEntry.Lookup<T> lookup) {
		Map<Identifier, List<TagLoader.EntryWithSource>> removeEntries = REMOVE_ENTRIES.get();

		DependencySorter<Identifier, TagLoader.SortingEntry> removeSorter = new DependencySorter<>();

		for (Map.Entry<Identifier, List<TagLoader.EntryWithSource>> removeEntry : removeEntries.entrySet()) {
			removeSorter.addEntry(removeEntry.getKey(), new TagLoader.SortingEntry(removeEntry.getValue()));
		}

		removeSorter.orderByDependencies((tagId, sortingEntry) -> {
			List<T> newTag = new ArrayList<>(newTags.getOrDefault(tagId, Collections.emptyList()));

			for (TagLoader.EntryWithSource entry : sortingEntry.entries()) {
				TagEntry tagEntry = entry.entry();
				tagEntry.build(
						lookup,
						newTag::remove
				);
			}

			newTags.put(tagId, Collections.unmodifiableList(newTag));
		});

		REMOVE_ENTRIES.get().clear();
		return newTags;
	}
}
