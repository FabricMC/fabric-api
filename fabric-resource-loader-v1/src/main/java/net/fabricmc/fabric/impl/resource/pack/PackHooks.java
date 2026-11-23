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
 * Internal hooks for {@link net.minecraft.server.packs.repository.Pack}.
 *
 * <p>An internal pack is a pack whose activation is tied to a set of parent packs and which is always
 * hidden from the user. This machinery is not part of the public API.
 */
public interface PackHooks {
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

	/**
	 * Sets the predicate deciding whether the pack is enabled, based on the set of enabled pack ids.
	 * Setting a predicate marks the pack as an internal pack, which also makes it hidden.
	 */
	default void fabric$setParentsPredicate(Predicate<Set<String>> predicate) {
	}

	/**
	 * Returns whether this pack is hidden because it is an internal, parent-gated pack, i.e. whether
	 * a parent predicate has been set.
	 *
	 * <p>This is the old {@code FabricPack#fabric$isHidden()} behavior. It must be used by the
	 * auto-enable gating instead of the public
	 * {@link net.fabricmc.fabric.api.resource.v1.pack.FabricPack#isHidden()}, which also considers
	 * the explicit hidden flag. Using the public method there would cause explicitly hidden packs to
	 * be treated as parent-gated and silently auto-enabled.
	 *
	 * @see ModPackResourcesUtil#refreshAutoEnabledPacks(java.util.List, java.util.Map)
	 */
	default boolean fabric$isHiddenByParents() {
		return false;
	}
}
