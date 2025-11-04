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

package net.fabricmc.fabric.impl.client.rendering;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.jetbrains.annotations.UnmodifiableView;

import net.minecraft.client.color.block.BlockTintCache;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.ColorResolver;

public final class ColorResolverRegistryImpl {
	// Includes vanilla resolvers
	private static final Set<ColorResolver> ALL_RESOLVERS = new HashSet<>();
	// Does not include vanilla resolvers
	private static final Set<ColorResolver> CUSTOM_RESOLVERS = new HashSet<>();
	private static final Set<ColorResolver> ALL_RESOLVERS_VIEW = Collections.unmodifiableSet(ALL_RESOLVERS);
	private static final Set<ColorResolver> CUSTOM_RESOLVERS_VIEW = Collections.unmodifiableSet(CUSTOM_RESOLVERS);

	static {
		ALL_RESOLVERS.add(BiomeColors.GRASS_COLOR_RESOLVER);
		ALL_RESOLVERS.add(BiomeColors.FOLIAGE_COLOR_RESOLVER);
		ALL_RESOLVERS.add(BiomeColors.WATER_COLOR_RESOLVER);
	}

	private ColorResolverRegistryImpl() {
	}

	public static void register(ColorResolver resolver) {
		ALL_RESOLVERS.add(resolver);
		CUSTOM_RESOLVERS.add(resolver);
	}

	@UnmodifiableView
	public static Set<ColorResolver> getAllResolvers() {
		return ALL_RESOLVERS_VIEW;
	}

	@UnmodifiableView
	public static Set<ColorResolver> getCustomResolvers() {
		return CUSTOM_RESOLVERS_VIEW;
	}

	public static Reference2ReferenceMap<ColorResolver, BlockTintCache> createCustomCacheMap(Function<ColorResolver, BlockTintCache> cacheFactory) {
		Reference2ReferenceOpenHashMap<ColorResolver, BlockTintCache> map = new Reference2ReferenceOpenHashMap<>();

		for (ColorResolver resolver : CUSTOM_RESOLVERS) {
			map.put(resolver, cacheFactory.apply(resolver));
		}

		map.trim();
		return map;
	}
}
