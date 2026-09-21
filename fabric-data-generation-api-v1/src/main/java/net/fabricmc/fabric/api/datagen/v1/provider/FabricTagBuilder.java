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

package net.fabricmc.fabric.api.datagen.v1.provider;

import java.util.List;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;

/**
 * Fabric extensions for {@link TagBuilder}.
 * Automatically implemented on {@link TagBuilder} via a mixin.
 *
 * <p>These methods add entries to the {@code fabric:remove} list, which is written to the generated
 * tag JSON and applied when the tag is loaded. Removals are prefixed with {@code !} when serialized
 * to a string list.
 *
 * <p>Removals are applied leniently: a removal whose element or referenced tag cannot be resolved is
 * skipped and never fails the tag, so there is no separate "optional removal" variant.
 */
public interface FabricTagBuilder {
	/**
	 * Returns the list of entries marked for removal, in insertion order.
	 *
	 * <p>The returned list is mutable and backed by this builder. Adding to it has the same effect as
	 * calling {@link #removeElement(Identifier)} or {@link #removeTag(Identifier)}, which exist as
	 * convenience methods. The list must not be replaced, only mutated.
	 *
	 * @return the removal entries
	 */
	default List<TagEntry> getRemove() {
		throw new AssertionError("Implemented via mixin");
	}

	/**
	 * Marks an existing entry for removal. Does nothing at load time when the entry is absent.
	 *
	 * @param id the entry id
	 */
	default void removeElement(Identifier id) {
		throw new AssertionError("Implemented via mixin");
	}

	/**
	 * Marks a referenced tag for removal. Does nothing at load time when the tag is absent.
	 *
	 * @param tag the tag id
	 */
	default void removeTag(Identifier tag) {
		throw new AssertionError("Implemented via mixin");
	}
}
