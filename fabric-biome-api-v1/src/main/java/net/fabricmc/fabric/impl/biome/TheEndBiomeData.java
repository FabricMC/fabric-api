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

package net.fabricmc.fabric.impl.biome;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import org.jspecify.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

/**
 * Internal data for modding Vanilla's {@link TheEndBiomeSource}.
 */
public final class TheEndBiomeData {
	public static final ThreadLocal<HolderGetter<Biome>> biomeRegistry = new ThreadLocal<>();
	public static final Set<ResourceKey<Biome>> ADDED_BIOMES = new HashSet<>();
	private static final Map<ResourceKey<Biome>, WeightedPicker<ResourceKey<Biome>>> END_BIOMES_MAP = new IdentityHashMap<>();
	private static final Map<ResourceKey<Biome>, WeightedPicker<ResourceKey<Biome>>> END_MIDLANDS_MAP = new IdentityHashMap<>();
	private static final Map<ResourceKey<Biome>, WeightedPicker<ResourceKey<Biome>>> END_BARRENS_MAP = new IdentityHashMap<>();

	static {
		END_BIOMES_MAP.computeIfAbsent(Biomes.THE_END, key -> new WeightedPicker<>())
				.add(Biomes.THE_END, 1.0);
		END_BIOMES_MAP.computeIfAbsent(Biomes.END_HIGHLANDS, key -> new WeightedPicker<>())
				.add(Biomes.END_HIGHLANDS, 1.0);
		END_BIOMES_MAP.computeIfAbsent(Biomes.SMALL_END_ISLANDS, key -> new WeightedPicker<>())
				.add(Biomes.SMALL_END_ISLANDS, 1.0);

		END_MIDLANDS_MAP.computeIfAbsent(Biomes.END_HIGHLANDS, key -> new WeightedPicker<>())
				.add(Biomes.END_MIDLANDS, 1.0);
		END_BARRENS_MAP.computeIfAbsent(Biomes.END_HIGHLANDS, key -> new WeightedPicker<>())
				.add(Biomes.END_BARRENS, 1.0);
	}

	private TheEndBiomeData() {
	}

	public static void addEndBiomeReplacement(ResourceKey<Biome> replaced, ResourceKey<Biome> variant, double weight) {
		Preconditions.checkNotNull(replaced, "replaced entry is null");
		Preconditions.checkNotNull(variant, "variant entry is null");
		Preconditions.checkArgument(weight > 0.0, "Weight is less than or equal to 0.0 (got %s)", weight);
		END_BIOMES_MAP.computeIfAbsent(replaced, key -> new WeightedPicker<>()).add(variant, weight);
		ADDED_BIOMES.add(variant);
	}

	public static void addEndMidlandsReplacement(ResourceKey<Biome> highlands, ResourceKey<Biome> midlands, double weight) {
		Preconditions.checkNotNull(highlands, "highlands entry is null");
		Preconditions.checkNotNull(midlands, "midlands entry is null");
		Preconditions.checkArgument(weight > 0.0, "Weight is less than or equal to 0.0 (got %s)", weight);
		END_MIDLANDS_MAP.computeIfAbsent(highlands, key -> new WeightedPicker<>()).add(midlands, weight);
		ADDED_BIOMES.add(midlands);
	}

	public static void addEndBarrensReplacement(ResourceKey<Biome> highlands, ResourceKey<Biome> barrens, double weight) {
		Preconditions.checkNotNull(highlands, "highlands entry is null");
		Preconditions.checkNotNull(barrens, "midlands entry is null");
		Preconditions.checkArgument(weight > 0.0, "Weight is less than or equal to 0.0 (got %s)", weight);
		END_BARRENS_MAP.computeIfAbsent(highlands, key -> new WeightedPicker<>()).add(barrens, weight);
		ADDED_BIOMES.add(barrens);
	}

	public static Overrides createOverrides(HolderGetter<Biome> biomes) {
		return new Overrides(biomes);
	}

	/**
	 * An instance of this class is attached to each {@link TheEndBiomeSource}.
	 */
	public static class Overrides {
		public final Set<Holder<Biome>> customBiomes;

		// Vanilla entries to compare against
		private final Holder<Biome> endMidlands;
		private final Holder<Biome> endBarrens;
		private final Holder<Biome> endHighlands;

		// Maps where the keys have been resolved to actual entries
		private final @Nullable Map<Holder<Biome>, WeightedPicker<Holder<Biome>>> endBiomesMap;
		private final @Nullable Map<Holder<Biome>, WeightedPicker<Holder<Biome>>> endMidlandsMap;
		private final @Nullable Map<Holder<Biome>, WeightedPicker<Holder<Biome>>> endBarrensMap;

		// cache for our own sampler (used for random biome replacement selection)
		private final Map<Climate.Sampler, ImprovedNoise> samplers = new WeakHashMap<>();

		public Overrides(HolderGetter<Biome> biomeRegistry) {
			this.customBiomes = ADDED_BIOMES.stream().map(biomeRegistry::getOrThrow).collect(Collectors.toSet());

			this.endMidlands = biomeRegistry.getOrThrow(Biomes.END_MIDLANDS);
			this.endBarrens = biomeRegistry.getOrThrow(Biomes.END_BARRENS);
			this.endHighlands = biomeRegistry.getOrThrow(Biomes.END_HIGHLANDS);

			this.endBiomesMap = resolveOverrides(biomeRegistry, END_BIOMES_MAP, Biomes.THE_END);
			this.endMidlandsMap = resolveOverrides(biomeRegistry, END_MIDLANDS_MAP, Biomes.END_MIDLANDS);
			this.endBarrensMap = resolveOverrides(biomeRegistry, END_BARRENS_MAP, Biomes.END_BARRENS);
		}

		// Resolves all RegistryKey instances to RegistryEntries
		private @Nullable Map<Holder<Biome>, WeightedPicker<Holder<Biome>>> resolveOverrides(HolderGetter<Biome> biomeRegistry, Map<ResourceKey<Biome>, WeightedPicker<ResourceKey<Biome>>> overrides, ResourceKey<Biome> vanillaKey) {
			Map<Holder<Biome>, WeightedPicker<Holder<Biome>>> result = new Object2ObjectOpenCustomHashMap<>(overrides.size(), RegistryKeyHashStrategy.INSTANCE);

			for (Map.Entry<ResourceKey<Biome>, WeightedPicker<ResourceKey<Biome>>> entry : overrides.entrySet()) {
				WeightedPicker<ResourceKey<Biome>> picker = entry.getValue();
				int count = picker.getEntryCount();
				if (count == 0 || (count == 1 && entry.getKey() == vanillaKey)) continue; // don't use no-op entries, for vanilla key biome check 1 as we have default entry

				result.put(biomeRegistry.getOrThrow(entry.getKey()), picker.map(biomeRegistry::getOrThrow));
			}

			return result.isEmpty() ? null : result;
		}

		public Holder<Biome> pick(int x, int y, int z, Climate.Sampler noise, Holder<Biome> vanillaBiome) {
			boolean isMidlands = vanillaBiome.is(endMidlands::is);

			if (isMidlands || vanillaBiome.is(endBarrens::is)) {
				// select a random highlands biome replacement, then try to replace it with a midlands or barrens biome replacement
				Holder<Biome> highlandsReplacement = pick(endHighlands, endHighlands, endBiomesMap, x, z, noise);
				Map<Holder<Biome>, WeightedPicker<Holder<Biome>>> map = isMidlands ? endMidlandsMap : endBarrensMap;

				return pick(highlandsReplacement, vanillaBiome, map, x, z, noise);
			} else {
				if (!END_BIOMES_MAP.containsKey(vanillaBiome.unwrapKey().orElseThrow())) {
					throw new IllegalStateException("Biome is not an End biome: " + vanillaBiome);
				}

				return pick(vanillaBiome, vanillaBiome, endBiomesMap, x, z, noise);
			}
		}

		private <T extends Holder<Biome>> T pick(T key, T defaultValue, Map<T, WeightedPicker<T>> pickers, int x, int z, Climate.Sampler noise) {
			if (pickers == null) return defaultValue;

			WeightedPicker<T> picker = pickers.get(key);
			if (picker == null) return defaultValue;
			int count = picker.getEntryCount();
			if (count == 0 || (count == 1 && key.is(endHighlands::is))) return defaultValue;

			// The x and z of the entry are divided by 64 to ensure custom biomes are large enough; going larger than this
			// seems to make custom biomes too hard to find.
			return picker.pickFromNoise(((MultiNoiseSamplerHooks) (Object) noise).fabric_getEndBiomesSampler(), x / 64.0, 0, z / 64.0);
		}
	}

	enum RegistryKeyHashStrategy implements Hash.Strategy<Holder<?>> {
		INSTANCE;
		@Override
		public boolean equals(Holder<?> a, Holder<?> b) {
			if (a == b) return true;
			if (a == null || b == null) return false;
			if (a.kind() != b.kind()) return false;
			// This Optional#get is safe - if a has key, b should also have key
			// given a.getType() != b.getType() check above
			// noinspection OptionalGetWithoutIsPresent
			return a.unwrap().map(key -> b.unwrapKey().get() == key, b.value()::equals);
		}

		@Override
		public int hashCode(Holder<?> a) {
			if (a == null) return 0;
			return a.unwrap().map(System::identityHashCode, Object::hashCode);
		}
	}
}
