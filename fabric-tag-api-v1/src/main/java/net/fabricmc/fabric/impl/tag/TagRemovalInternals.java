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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagLoader;

public class TagRemovalInternals {
	public static final ScopedValue<Identifier> TAG_ID_SCOPED_VALUE = ScopedValue.newInstance();
	private static final ThreadLocal<Map<Identifier, Set<TagLoader.EntryWithSource>>> REMOVE_REFERENCE = ThreadLocal.withInitial(HashMap::new);

	public static void addEntryToRemoveReference(Identifier tagId, TagLoader.EntryWithSource entry) {
		if (!REMOVE_REFERENCE.get().containsKey(tagId)) {
			REMOVE_REFERENCE.get().put(tagId, new HashSet<>());
		}

		REMOVE_REFERENCE.get()
				.get(tagId)
				.add(entry);
	}

	public static boolean isEntryInRemoveReference(TagLoader.EntryWithSource entry) {
		return REMOVE_REFERENCE.get()
				.getOrDefault(TAG_ID_SCOPED_VALUE.get(), Collections.emptySet())
				.contains(entry);
	}

	public static void removeRemoveReference() {
		REMOVE_REFERENCE.remove();
	}
}
