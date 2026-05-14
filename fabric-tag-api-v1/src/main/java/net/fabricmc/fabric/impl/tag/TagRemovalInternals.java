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
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagLoader;

import net.fabricmc.fabric.api.tag.v1.FabricTagFile;

public class TagRemovalInternals {
	public static final ScopedValue<Identifier> TAG_ID_SCOPED_VALUE = ScopedValue.newInstance();
	private static final ThreadLocal<Map<Identifier, List<String>>> TAG_SOURCE_ORDER = ThreadLocal.withInitial(HashMap::new);
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

	public static void addTagSource(Identifier tagId, String source) {
		if (!TAG_SOURCE_ORDER.get().containsKey(tagId)) {
			TAG_SOURCE_ORDER.get().put(tagId, new ArrayList<>());
		}

		TAG_SOURCE_ORDER.get()
				.get(tagId)
				.add(source);
	}

	public static void addRemoveEntry(Identifier tagId, TagLoader.EntryWithSource entry) {
		if (!REMOVE_ENTRIES.get().containsKey(tagId)) {
			REMOVE_ENTRIES.get().put(tagId, new ArrayList<>());
		}

		REMOVE_ENTRIES.get()
				.get(tagId)
				.add(entry);
	}

	public static boolean isEntryRemove(TagLoader.EntryWithSource entry) {
		return REMOVE_ENTRIES.get()
				.getOrDefault(TAG_ID_SCOPED_VALUE.get(), Collections.emptyList())
				.contains(entry);
	}

	public static List<TagLoader.EntryWithSource> mergeAddedAndRemovedEntries(Identifier tagId, List<TagLoader.EntryWithSource> entries) {
		List<TagLoader.EntryWithSource> newEntries = new ArrayList<>();

		if (REMOVE_ENTRIES.get().isEmpty()) {
			return entries;
		}

		for (String sourceId : TAG_SOURCE_ORDER.get().getOrDefault(tagId, Collections.emptyList())) {
			newEntries.addAll(Stream.concat(
					// 'values' key should be added before 'fabric:remove' key.
					entries.stream()
							.filter(entry -> entry.source().equals(sourceId)),
					REMOVE_ENTRIES.get()
							.getOrDefault(tagId, Collections.emptyList())
							.stream()
							.filter(entry -> entry.source().equals(sourceId))
			).toList());
		}

		return newEntries;
	}

	public static void removeTagRemovalReference(Identifier tagKey) {
		TAG_SOURCE_ORDER.get().remove(tagKey);
		REMOVE_ENTRIES.get().remove(tagKey);
	}

	public static void removeTagRemovalReferences() {
		TAG_SOURCE_ORDER.remove();
		REMOVE_ENTRIES.remove();
	}
}
