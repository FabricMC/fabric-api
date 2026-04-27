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

import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;

import net.minecraft.tags.TagLoader;

public class TagRemovalInternals {
	private static final ThreadLocal<ReferenceSet<TagLoader.EntryWithSource>> REMOVE_ENTRIES = ThreadLocal.withInitial(ReferenceArraySet::new);

	public static void setEntryAsRemove(TagLoader.EntryWithSource entry) {
		REMOVE_ENTRIES.get().add(entry);
	}

	public static boolean isEntryRemove(TagLoader.EntryWithSource entry) {
		boolean isRemove = REMOVE_ENTRIES.get().contains(entry);

		if (isRemove) {
			REMOVE_ENTRIES.get().remove(entry);
			return true;
		}

		return false;
	}

	public static void removeRemoveEntriesReference() {
		REMOVE_ENTRIES.remove();
	}
}
