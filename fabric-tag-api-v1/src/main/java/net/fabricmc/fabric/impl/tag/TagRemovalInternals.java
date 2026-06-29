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
import net.fabricmc.fabric.mixin.tag.TagEntryAccessor;

public class TagRemovalInternals {
	private static final ThreadLocal<Identifier> TAG_ID_THREAD_LOCAL = new ThreadLocal<>();
	private static final ThreadLocal<Map<Identifier, List<String>>> TAG_SOURCE_ORDER = ThreadLocal.withInitial(HashMap::new);
	private static final ThreadLocal<Map<Identifier, List<TagLoader.EntryWithSource>>> REMOVE_ENTRIES = ThreadLocal.withInitial(HashMap::new);

	public static void setTagId(Identifier tagId) {
		TAG_ID_THREAD_LOCAL.set(tagId);
	}

	public static void clearTagId() {
		TAG_ID_THREAD_LOCAL.remove();
	}

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

	public static Identifier normalizeTagResourceId(Identifier resourceId) {
		String path = resourceId.getPath();

		if (path.startsWith("tags/")) {
			path = path.substring("tags/".length());
		}

		int firstSeparator = path.indexOf('/');

		if (firstSeparator < 0) {
			return resourceId.withPath(path.replace(".json", ""));
		}

		int secondSeparator = path.indexOf('/', firstSeparator + 1);
		int tagStart = secondSeparator >= 0 ? secondSeparator + 1 : firstSeparator + 1;
		String normalizedPath = path.substring(tagStart);

		if (normalizedPath.endsWith(".json")) {
			normalizedPath = normalizedPath.substring(0, normalizedPath.length() - ".json".length());
		}

		return resourceId.withPath(normalizedPath);
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

		TagEntryAccessor accessor = ((TagEntryAccessor) entry.entry());
		TagEntry optional = accessor.fabric_getTag() ? TagEntry.optionalTag(accessor.fabric_getId()) : TagEntry.optionalElement(accessor.fabric_getId());
		TagLoader.EntryWithSource toAdd = new TagLoader.EntryWithSource(optional, entry.source());

		REMOVE_ENTRIES.get()
				.get(tagId)
				.add(toAdd);
	}

	public static boolean isEntryRemove(TagLoader.EntryWithSource entry) {
		Identifier tagId = TAG_ID_THREAD_LOCAL.get();
		if (tagId == null) return false;

		return REMOVE_ENTRIES.get().getOrDefault(tagId, Collections.emptyList()).contains(entry);
	}

	public static void mergeAddedAndRemovedEntries(Identifier tagId, List<TagLoader.EntryWithSource> entries) {
		if (!REMOVE_ENTRIES.get().containsKey(tagId)) {
			return;
		}

		List<TagLoader.EntryWithSource> newEntries = new ArrayList<>();

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

		entries.clear();
		entries.addAll(newEntries);
	}

	public static void removeTagRemovalReferences() {
		TAG_SOURCE_ORDER.remove();
		REMOVE_ENTRIES.remove();
	}
}
