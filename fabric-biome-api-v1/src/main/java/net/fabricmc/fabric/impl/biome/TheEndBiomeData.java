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
import org.jetbrains.annotations.Nullable;

import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

/**
 * Internal data for modding Vanilla's {@link TheEndBiomeSource}.
 */
public final class TheEndBiomeData {
	public static final ThreadLocal<RegistryEntryLookup<Biome>> biomeRegistry = new ThreadLocal<>();
	public static final Set<RegistryKey<Biome>> ADDED_BIOMES = new HashSet<>();
	private static final Map<RegistryKey<Biome>, WeightedPicker<RegistryKey<Biome>>> END_BIOMES_MAP = new IdentityHashMap<>();
	private static final Map<RegistryKey<Biome>, WeightedPicker<RegistryKey<Biome>>> END_MIDLANDS_MAP = new IdentityHashMap<>();
	private static final Map<RegistryKey<Biome>, WeightedPicker<RegistryKey<Biome>>> END_BARRENS_MAP = new IdentityHashMap<>();

	static {
		END_BIOMES_MAP.computeIfAbsent(BiomeKeys.THE_END, key -> new WeightedPicker<>())
				.add(BiomeKeys.THE_END, 1.0);
		END_BIOMES_MAP.computeIfAbsent(BiomeKeys.END_HIGHLANDS, key -> new WeightedPicker<>())
				.add(BiomeKeys.END_HIGHLANDS, 1.0);
		END_BIOMES_MAP.computeIfAbsent(BiomeKeys.SMALL_END_ISLANDS, key -> new WeightedPicker<>())
				.add(BiomeKeys.SMALL_END_ISLANDS, 1.0);

		END_MIDLANDS_MAP.computeIfAbsent(BiomeKeys.END_HIGHLANDS, key -> new WeightedPicker<>())
				.add(BiomeKeys.END_MIDLANDS, 1.0);
		END_BARRENS_MAP.computeIfAbsent(BiomeKeys.END_HIGHLANDS, key -> new WeightedPicker<>())
				.add(BiomeKeys.END_BARRENS, 1.0);
	}

	private TheEndBiomeData() {
	}

	public static void addEndBiomeReplacement(RegistryKey<Biome> replaced, RegistryKey<Biome> variant, double weight) {
		Preconditions.checkNotNull(replaced, "replaced entry is null");
		Preconditions.checkNotNull(variant, "variant entry is null");
		Preconditions.checkArgument(weight > 0.0, "Weight is less than or equal to 0.0 (got %s)", weight);
		END_BIOMES_MAP.computeIfAbsent(replaced, key -> new WeightedPicker<>()).add(variant, weight);
		ADDED_BIOMES.add(variant);
	}

	public static void addEndMidlandsReplacement(RegistryKey<Biome> highlands, RegistryKey<Biome> midlands, double weight) {
		Preconditions.checkNotNull(highlands, "highlands entry is null");
		Preconditions.checkNotNull(midlands, "midlands entry is null");
		Preconditions.checkArgument(weight > 0.0, "Weight is less than or equal to 0.0 (got %s)", weight);
		END_MIDLANDS_MAP.computeIfAbsent(highlands, key -> new WeightedPicker<>()).add(midlands, weight);
		ADDED_BIOMES.add(midlands);
	}

	public static void addEndBarrensReplacement(RegistryKey<Biome> highlands, RegistryKey<Biome> barrens, double weight) {
		Preconditions.checkNotNull(highlands, "highlands entry is null");
		Preconditions.checkNotNull(barrens, "midlands entry is null");
		Preconditions.checkArgument(weight > 0.0, "Weight is less than or equal to 0.0 (got %s)", weight);
		END_BARRENS_MAP.computeIfAbsent(highlands, key -> new WeightedPicker<>()).add(barrens, weight);
		ADDED_BIOMES.add(barrens);
	}

	public static Overrides createOverrides(RegistryEntryLookup<Biome> biomes) {
		return new Overrides(biomes);
	}

	/**
	 * An instance of this class is attached to each {@link TheEndBiomeSource}.
	 */
	public static class Overrides {
		public final Set<RegistryEntry<Biome>> customBiomes;

		// Vanilla entries to compare against
		private final RegistryEntry<Biome> endMidlands;
		private final RegistryEntry<Biome> endBarrens;
		private final RegistryEntry<Biome> endHighlands;

		// Maps where the keys have been resolved to actual entries
		private final @Nullable Map<RegistryEntry<Biome>, WeightedPicker<RegistryEntry<Biome>>> endBiomesMap;
		private final @Nullable Map<RegistryEntry<Biome>, WeightedPicker<RegistryEntry<Biome>>> endMidlandsMap;
		private final @Nullable Map<RegistryEntry<Biome>, WeightedPicker<RegistryEntry<Biome>>> endBarrensMap;

		// cache for our own sampler (used for random biome replacement selection)
		private final Map<MultiNoiseUtil.MultiNoiseSampler, PerlinNoiseSampler> samplers = new WeakHashMap<>();

		public Overrides(RegistryEntryLookup<Biome> biomeRegistry) {
			this.customBiomes = ADDED_BIOMES.stream().map(biomeRegistry::getOrThrow).collect(Collectors.toSet());

			this.endMidlands = biomeRegistry.getOrThrow(BiomeKeys.END_MIDLANDS);
			this.endBarrens = biomeRegistry.getOrThrow(BiomeKeys.END_BARRENS);
			this.endHighlands = biomeRegistry.getOrThrow(BiomeKeys.END_HIGHLANDS);

			this.endBiomesMap = resolveOverrides(biomeRegistry, END_BIOMES_MAP, BiomeKeys.THE_END);
			this.endMidlandsMap = resolveOverrides(biomeRegistry, END_MIDLANDS_MAP, BiomeKeys.END_MIDLANDS);
			this.endBarrensMap = resolveOverrides(biomeRegistry, END_BARRENS_MAP, BiomeKeys.END_BARRENS);
		}

		// Resolves all RegistryKey instances to RegistryEntries
		private @Nullable Map<RegistryEntry<Biome>, WeightedPicker<RegistryEntry<Biome>>> resolveOverrides(RegistryEntryLookup<Biome> biomeRegistry, Map<RegistryKey<Biome>, WeightedPicker<RegistryKey<Biome>>> overrides, RegistryKey<Biome> vanillaKey) {
			Map<RegistryEntry<Biome>, WeightedPicker<RegistryEntry<Biome>>> result = new Object2ObjectOpenCustomHashMap<>(overrides.size(), RegistryKeyHashStrategy.INSTANCE);

			for (Map.Entry<RegistryKey<Biome>, WeightedPicker<RegistryKey<Biome>>> entry : overrides.entrySet()) {
				WeightedPicker<RegistryKey<Biome>> picker = entry.getValue();
				int count = picker.getEntryCount();
				if (count == 0 || (count == 1 && entry.getKey() == vanillaKey)) continue; // don't use no-op entries, for vanilla key biome check 1 as we have default entry

				result.put(biomeRegistry.getOrThrow(entry.getKey()), picker.map(biomeRegistry::getOrThrow));
			}

			return result.isEmpty() ? null : result;
		}

		public RegistryEntry<Biome> pick(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler noise, RegistryEntry<Biome> vanillaBiome) {
			boolean isMidlands = vanillaBiome.matches(endMidlands::matchesKey);

			if (isMidlands || vanillaBiome.matches(endBarrens::matchesKey)) {
				// select a random highlands biome replacement, then try to replace it with a midlands or barrens biome replacement
				RegistryEntry<Biome> highlandsReplacement = pick(endHighlands, endHighlands, endBiomesMap, x, z, noise);
				Map<RegistryEntry<Biome>, WeightedPicker<RegistryEntry<Biome>>> map = isMidlands ? endMidlandsMap : endBarrensMap;

				return pick(highlandsReplacement, vanillaBiome, map, x, z, noise);
			} else {
				if (!END_BIOMES_MAP.containsKey(vanillaBiome.getKey().orElseThrow())) {
					throw new IllegalStateException("Biome is not an End biome: " + vanillaBiome);
				}

				return pick(vanillaBiome, vanillaBiome, endBiomesMap, x, z, noise);
			}
		}

		private <T extends RegistryEntry<Biome>> T pick(T key, T defaultValue, Map<T, WeightedPicker<T>> pickers, int x, int z, MultiNoiseUtil.MultiNoiseSampler noise) {
			if (pickers == null) return defaultValue;

			WeightedPicker<T> picker = pickers.get(key);
			if (picker == null) return defaultValue;
			int count = picker.getEntryCount();
			if (count == 0 || (count == 1 && key.matches(endHighlands::matchesKey))) return defaultValue;

			// The x and z of the entry are divided by 64 to ensure custom biomes are large enough; going larger than this
			// seems to make custom biomes too hard to find.
			return picker.pickFromNoise(((MultiNoiseSamplerHooks) (Object) noise).fabric_getEndBiomesSampler(), x / 64.0, 0, z / 64.0);
		}
	}

	enum RegistryKeyHashStrategy implements Hash.Strategy<RegistryEntry<?>> {
		INSTANCE;
		@Override
		public boolean equals(RegistryEntry<?> a, RegistryEntry<?> b) {
			if (a == b) return true;
			if (a == null || b == null) return false;
			if (a.getType() != b.getType()) return false;
			// This Optional#get is safe - if a has key, b should also have key
			// given a.getType() != b.getType() check above
			// noinspection OptionalGetWithoutIsPresent
			return a.getKeyOrValue().map(key -> b.getKey().get() == key, b.value()::equals);
		}

		@Override
		public int hashCode(RegistryEntry<?> a) {
			if (a == null) return 0;
			return a.getKeyOrValue().map(System::identityHashCode, Object::hashCode);
		}
	}
}
