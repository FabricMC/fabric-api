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

package net.fabricmc.fabric.impl.resource.pack;

import java.util.Set;
import java.util.function.Predicate;

/**
 * Fabric addition to ResourcePackProfile.
 * @see ModResourcePackCreator
 */
public interface FabricPack {
	/**
	 * Returns whether the pack is internal and hidden from end users.
	 */
	default boolean fabric$isHidden() {
		return false;
	}

	/**
	 * Returns whether every parent is enabled. If this is not empty, the pack's status
	 * is synced to that of the parent pack(s), where the pack gets enabled if and only
	 * if each of the parent is enabled. Note that non-Fabric packs always return {@code true}.
	 *
	 * @return {@code true} if every parent is enabled, or {@code false} otherwise
	 */
	default boolean fabric$parentsEnabled(Set<String> enabled) {
		return true;
	}

	default void fabric$setParentsPredicate(Predicate<Set<String>> predicate) {
	}
}
