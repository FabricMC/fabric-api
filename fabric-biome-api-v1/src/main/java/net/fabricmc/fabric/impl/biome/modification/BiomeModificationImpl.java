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

package net.fabricmc.fabric.impl.biome.modification;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.base.Stopwatch;
import com.google.common.base.Suppliers;
import org.jetbrains.annotations.TestOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FeatureSorter;

import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;

public class BiomeModificationImpl {
	private static final Logger LOGGER = LoggerFactory.getLogger(BiomeModificationImpl.class);

	private static final Comparator<ModifierRecord> MODIFIER_ORDER_COMPARATOR = Comparator.<ModifierRecord>comparingInt(r -> r.phase.ordinal()).thenComparingInt(r -> r.order).thenComparing(r -> r.id);

	public static final BiomeModificationImpl INSTANCE = new BiomeModificationImpl();

	private final List<ModifierRecord> modifiers = new ArrayList<>();

	private boolean modifiersUnsorted = true;

	private BiomeModificationImpl() {
	}

	public void addModifier(Identifier id, ModificationPhase phase, Predicate<BiomeSelectionContext> selector, BiConsumer<BiomeSelectionContext, BiomeModificationContext> modifier) {
		Objects.requireNonNull(selector);
		Objects.requireNonNull(modifier);

		modifiers.add(new ModifierRecord(phase, id, selector, modifier));
		modifiersUnsorted = true;
	}

	public void addModifier(Identifier id, ModificationPhase phase, Predicate<BiomeSelectionContext> selector, Consumer<BiomeModificationContext> modifier) {
		Objects.requireNonNull(selector);
		Objects.requireNonNull(modifier);

		modifiers.add(new ModifierRecord(phase, id, selector, modifier));
		modifiersUnsorted = true;
	}

	/**
	 * This is currently not publicly exposed but likely useful for modpack support mods.
	 */
	void changeOrder(Identifier id, int order) {
		modifiersUnsorted = true;

		for (ModifierRecord modifierRecord : modifiers) {
			if (id.equals(modifierRecord.id)) {
				modifierRecord.setOrder(order);
			}
		}
	}

	@TestOnly
	void clearModifiers() {
		modifiers.clear();
		modifiersUnsorted = true;
	}

	private List<ModifierRecord> getSortedModifiers() {
		if (modifiersUnsorted) {
			// Resort modifiers
			modifiers.sort(MODIFIER_ORDER_COMPARATOR);
			modifiersUnsorted = false;
		}

		return modifiers;
	}

	public void finalizeWorldGen(RegistryAccess impl) {
		Stopwatch sw = Stopwatch.createStarted();

		// Now that we apply biome modifications inside the MinecraftServer constructor, we should only ever do
		// this once for a dynamic registry manager. Marking the dynamic registry manager as modified ensures a crash
		// if the precondition is violated.
		BiomeModificationMarker modificationTracker = (BiomeModificationMarker) impl;
		modificationTracker.fabric_markModified();

		Registry<Biome> biomes = impl.lookupOrThrow(Registries.BIOME);

		// Build a list of all biome keys in ascending order of their raw-id to get a consistent result in case
		// someone does something stupid.
		List<ResourceKey<Biome>> keys = biomes.entrySet().stream()
				.map(Map.Entry::getKey)
				.sorted(Comparator.comparingInt(key -> biomes.getId(biomes.getValueOrThrow(key))))
				.toList();

		List<ModifierRecord> sortedModifiers = getSortedModifiers();

		int biomesChanged = 0;
		int biomesProcessed = 0;
		int modifiersApplied = 0;

		for (ResourceKey<Biome> key : keys) {
			Biome biome = biomes.getValueOrThrow(key);

			biomesProcessed++;

			// Make a copy of the biome to allow selection contexts to see it unmodified,
			// But do so only once it's known anything wants to modify the biome at all
			BiomeSelectionContext context = new BiomeSelectionContextImpl(impl, key, biome);
			BiomeModificationContextImpl modificationContext = null;

			for (ModifierRecord modifier : sortedModifiers) {
				if (modifier.selector.test(context)) {
					LOGGER.trace("Applying modifier {} to {}", modifier, key.identifier());

					// Create the copy only if at least one modifier applies, since it's pretty costly
					if (modificationContext == null) {
						biomesChanged++;
						modificationContext = new BiomeModificationContextImpl(impl, biome);
					}

					modifier.apply(context, modificationContext);
					modifiersApplied++;
				}
			}

			// Re-freeze and apply certain cleanup actions
			if (modificationContext != null) {
				modificationContext.freeze();

				if (modificationContext.shouldRebuildFeatures()) {
					impl.lookupOrThrow(Registries.LEVEL_STEM).stream().forEach(dimensionOptions -> {
						dimensionOptions.generator().featuresPerStep = Suppliers.memoize(
							() -> FeatureSorter.buildFeaturesPerStep(
									List.copyOf(dimensionOptions.generator().getBiomeSource().possibleBiomes()),
									biomeEntry -> dimensionOptions.generator().getBiomeGenerationSettings(biomeEntry).features(),
									true
							)
						);
					});
				}

				if (biomes instanceof MappedRegistry<Biome> registry) {
					RegistrationInfo info = registry.registrationInfos.get(key);
					RegistrationInfo newInfo = new RegistrationInfo(Optional.empty(), info.lifecycle());
					registry.registrationInfos.put(key, newInfo);
				}
			}
		}

		if (biomesProcessed > 0) {
			LOGGER.info("Applied {} biome modifications to {} of {} new biomes in {}", modifiersApplied, biomesChanged,
					biomesProcessed, sw);
		}
	}

	private static class ModifierRecord {
		private final ModificationPhase phase;

		private final Identifier id;

		private final Predicate<BiomeSelectionContext> selector;

		private final BiConsumer<BiomeSelectionContext, BiomeModificationContext> contextSensitiveModifier;

		private final Consumer<BiomeModificationContext> modifier;

		// Whenever this is modified, the modifiers need to be resorted
		private int order;

		ModifierRecord(ModificationPhase phase, Identifier id, Predicate<BiomeSelectionContext> selector, Consumer<BiomeModificationContext> modifier) {
			this.phase = phase;
			this.id = id;
			this.selector = selector;
			this.modifier = modifier;
			this.contextSensitiveModifier = null;
		}

		ModifierRecord(ModificationPhase phase, Identifier id, Predicate<BiomeSelectionContext> selector, BiConsumer<BiomeSelectionContext, BiomeModificationContext> modifier) {
			this.phase = phase;
			this.id = id;
			this.selector = selector;
			this.contextSensitiveModifier = modifier;
			this.modifier = null;
		}

		@Override
		public String toString() {
			if (modifier != null) {
				return modifier.toString();
			} else {
				return contextSensitiveModifier.toString();
			}
		}

		public void apply(BiomeSelectionContext context, BiomeModificationContextImpl modificationContext) {
			if (contextSensitiveModifier != null) {
				contextSensitiveModifier.accept(context, modificationContext);
			} else {
				modifier.accept(modificationContext);
			}
		}

		public void setOrder(int order) {
			this.order = order;
		}
	}
}
