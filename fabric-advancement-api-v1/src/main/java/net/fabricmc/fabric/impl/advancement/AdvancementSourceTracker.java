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

package net.fabricmc.fabric.impl.advancement;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.Resource;

import net.fabricmc.fabric.api.advancement.v1.AdvancementSource;
import net.fabricmc.fabric.impl.resource.pack.BuiltinModPackSource;
import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;

/**
 * Tracks where the advancements of an in-progress reload were loaded from.
 *
 * <p>Advancements are scanned in {@code SimpleJsonResourceReloadListener#prepare} on a worker thread
 * and applied in {@code ServerAdvancementManager#apply} on the server thread, so the sources cannot be
 * stored in a {@link ThreadLocal} like the loot API does. They are instead keyed by the identity of the
 * map that the scan writes its results into, which is the same map instance that is later applied.
 * This keeps concurrent reloads, such as two servers reloading from the same game instance, separate.
 */
public final class AdvancementSourceTracker {
	private static final Map<Map<?, ?>, Map<Identifier, AdvancementSource>> SOURCES = Collections.synchronizedMap(new IdentityHashMap<>());

	private AdvancementSourceTracker() {
	}

	public static void put(Map<?, ?> preparations, Identifier id, AdvancementSource source) {
		SOURCES.computeIfAbsent(preparations, key -> Collections.synchronizedMap(new HashMap<>())).put(id, source);
	}

	/**
	 * Returns and forgets the sources of the advancements scanned into {@code preparations}.
	 */
	public static Map<Identifier, AdvancementSource> remove(Map<?, ?> preparations) {
		Map<Identifier, AdvancementSource> sources = SOURCES.remove(preparations);
		return sources != null ? sources : Map.of();
	}

	public static AdvancementSource determineSource(Resource resource) {
		if (resource != null) {
			PackSource packSource = resource.getFabricPackSource();

			if (packSource == PackSource.BUILT_IN) {
				return AdvancementSource.VANILLA;
			} else if (packSource == ModResourcePackCreator.RESOURCE_PACK_SOURCE || packSource instanceof BuiltinModPackSource) {
				return AdvancementSource.MOD;
			}
		}

		// If not builtin or mod, assume external data pack.
		// It might also be a virtual advancement injected via mixin instead of being loaded
		// from a resource, but we can't determine that here.
		return AdvancementSource.DATA_PACK;
	}
}
