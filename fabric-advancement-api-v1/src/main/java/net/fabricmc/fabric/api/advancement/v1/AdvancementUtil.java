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

package net.fabricmc.fabric.api.advancement.v1;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.Resource;

import net.fabricmc.fabric.impl.resource.pack.BuiltinModPackSource;
import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;

public final class AdvancementUtil {
	public static final Map<Identifier, AdvancementSource> SOURCES = new ConcurrentHashMap<>();

	private AdvancementUtil() {
	}

	public static AdvancementSource determineSource(Resource resource) {
		PackSource packSource = resource.getFabricPackSource();

		if (packSource == PackSource.BUILT_IN) {
			return AdvancementSource.VANILLA;
		} else if (packSource == ModResourcePackCreator.RESOURCE_PACK_SOURCE || packSource instanceof BuiltinModPackSource) {
			return AdvancementSource.MOD;
		}

		// If not builtin or mod, assume external data pack.
		// It might also be a virtual advancement injected via mixin instead of being loaded
		// from a resource, but we can't determine that here.
		return AdvancementSource.DATA_PACK;
	}
}
