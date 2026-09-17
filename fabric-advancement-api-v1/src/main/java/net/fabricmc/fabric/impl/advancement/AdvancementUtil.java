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
import java.util.Map;
import java.util.WeakHashMap;

import org.jspecify.annotations.Nullable;

import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import net.fabricmc.fabric.api.advancement.v1.AdvancementEvents;
import net.fabricmc.fabric.api.advancement.v1.AdvancementSource;
import net.fabricmc.fabric.api.advancement.v1.FabricAdvancementBuilder;
import net.fabricmc.fabric.impl.resource.pack.BuiltinModPackSource;
import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;

// Exact copy of LootUtil
public final class AdvancementUtil {
	private static final Map<ResourceManager, Provider> RELOAD_PROVIDERS = Collections.synchronizedMap(new WeakHashMap<>());

	public static void startReload(ResourceManager resourceManager, HolderLookup.Provider provider) {
		RELOAD_PROVIDERS.put(resourceManager, provider);
	}

	public static void endReload(ResourceManager resourceManager) {
		RELOAD_PROVIDERS.remove(resourceManager);
	}

	public static HolderLookup.@Nullable Provider getActiveReloadProvider(ResourceManager resourceManager) {
		return RELOAD_PROVIDERS.get(resourceManager);
	}

	public static Advancement modifyAdvancement(Identifier id, Advancement advancement, AdvancementSource source, HolderLookup.Provider provider) {
		Advancement replacement = AdvancementEvents.REPLACE.invoker().replaceAdvancement(id, advancement, source, provider);

		if (replacement != null) {
			advancement = replacement;
			source = AdvancementSource.REPLACED;
		}

		Advancement.Builder builder = FabricAdvancementBuilder.copyOf(advancement);
		AdvancementEvents.MODIFY.invoker().modifyAdvancement(id, builder, source, provider);
		return builder.build(id).value();
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
