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

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Suppliers;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.TestOnly;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FeatureSorter;

import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.biome.v1.data.BiomeModifier;
import net.fabricmc.fabric.api.biome.v1.data.BiomeModifiers;

public class BiomeModificationImpl {
	private static final Logger LOGGER = LoggerFactory.getLogger(BiomeModificationImpl.class);

	private static final Comparator<ModifierRecord> MODIFIER_ORDER_COMPARATOR = Comparator.<ModifierRecord>comparingInt(r -> r.phase.ordinal()).thenComparingInt(r -> r.order).thenComparing(r -> r.id);

	public static final BiomeModificationImpl INSTANCE = new BiomeModificationImpl();

	private final List<ModifierRecord> modifiers = new ArrayList<>();

	private BiomeModificationImpl() {
	}

	public void addModifier(Identifier id, ModificationPhase phase, Predicate<BiomeSelectionContext> selector, BiConsumer<BiomeSelectionContext, BiomeModificationContext> modifier) {
		Objects.requireNonNull(selector);
		Objects.requireNonNull(modifier);

		modifiers.add(new ModifierRecord(phase, id, selector, modifier));
	}

	public void addModifier(Identifier id, ModificationPhase phase, Predicate<BiomeSelectionContext> selector, Consumer<BiomeModificationContext> modifier) {
		Objects.requireNonNull(selector);
		Objects.requireNonNull(modifier);

		modifiers.add(new ModifierRecord(phase, id, selector, modifier));
	}

	/**
	 * This is currently not publicly exposed but likely useful for modpack support mods.
	 */
	void changeOrder(Identifier id, int order) {
		for (ModifierRecord modifierRecord : modifiers) {
			if (id.equals(modifierRecord.id)) {
				modifierRecord.setOrder(order);
			}
		}
	}

	@TestOnly
	void clearModifiers() {
		modifiers.clear();
	}

	private List<ModifierRecord> getSortedModifiers(RegistryAccess registries) {
		List<ModifierRecord> combinedModifiers = new ArrayList<>(modifiers);

		for (Map.Entry<ResourceKey<BiomeModifier>, BiomeModifier> entry : registries.lookupOrThrow(BiomeModifiers.REGISTRY_KEY).entrySet()) {
			BiomeModifier modifier = entry.getValue();
			combinedModifiers.add(new ModifierRecord(modifier.getPhase(), entry.getKey().identifier(), modifier::matches, modifier::modify));
		}

		combinedModifiers.sort(MODIFIER_ORDER_COMPARATOR);
		return combinedModifiers;
	}

	public void finalizeWorldGen(RegistryAccess impl) {
		Stopwatch sw = Stopwatch.createStarted();

		// Now that we apply biome modifications inside the MinecraftServer constructor, we should only ever do
		// this once for a RegistryAccess. Marking the RegistryAccess as modified ensures a crash
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

		List<ModifierRecord> sortedModifiers = getSortedModifiers(impl);

		int biomesChanged = 0;
		int biomesProcessed = 0;
		int modifiersApplied = 0;

		for (ResourceKey<Biome> key : keys) {
			Biome biome = biomes.getValueOrThrow(key);

			biomesProcessed++;

			Applied applied = applyModifiers(impl, key, biome, sortedModifiers);
			modifiersApplied += applied.count();
			BiomeModificationContextImpl modificationContext = applied.context();

			// Apply certain cleanup actions
			if (modificationContext != null) {
				biomesChanged++;

				if (modificationContext.shouldRebuildFeatures()) {
					impl.lookupOrThrow(Registries.LEVEL_STEM).stream().forEach(levelStem -> {
						levelStem.generator().featuresPerStep = Suppliers.memoize(
							() -> FeatureSorter.buildFeaturesPerStep(
									List.copyOf(levelStem.generator().getBiomeSource().possibleBiomes()),
									biomeHolder -> levelStem.generator().getBiomeGenerationSettings(biomeHolder).features(),
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

	/**
	 * Applies the modifiers that select the biome to it, in order, and re-freezes it.
	 *
	 * @return the modification context, or null if no modifier selected the biome, and how many modifiers were applied
	 */
	private static Applied applyModifiers(RegistryAccess registries, ResourceKey<Biome> key, Biome biome, List<ModifierRecord> modifiers) {
		// Make a copy of the biome to allow selection contexts to see it unmodified,
		// But do so only once it's known anything wants to modify the biome at all
		BiomeSelectionContext context = new BiomeSelectionContextImpl(registries, key, biome);
		BiomeModificationContextImpl modificationContext = null;
		int count = 0;

		for (ModifierRecord modifier : modifiers) {
			if (modifier.selector.test(context)) {
				LOGGER.trace("Applying modifier {} to {}", modifier, key.identifier());

				// Create the copy only if at least one modifier applies, since it's pretty costly
				if (modificationContext == null) {
					modificationContext = new BiomeModificationContextImpl(registries, biome);
				}

				modifier.apply(context, modificationContext);
				count++;
			}
		}

		if (modificationContext != null) {
			modificationContext.freeze();
		}

		return new Applied(modificationContext, count);
	}

	private record Applied(@Nullable BiomeModificationContextImpl context, int count) {
	}

	/**
	 * Copies a biome from registries that a server hasn't modified yet, and applies every biome modification to the
	 * copy as {@link #finalizeWorldGen} would. The biome in the registries is left as it is.
	 */
	public Biome createModifiedCopy(RegistryAccess registries, ResourceKey<Biome> key) {
		Preconditions.checkState(!((BiomeModificationMarker) registries).fabric_isModified(),
				"Biome modifications have already been applied to these registries");
		Preconditions.checkArgument(registries.lookup(Registries.LEVEL_STEM).isPresent(),
				"The registries have no dimensions, so modifications selecting biomes by dimension can't be applied");
		Biome original = registries.lookupOrThrow(Registries.BIOME).getValueOrThrow(key);
		RegistryOps<JsonElement> ops = registries.createSerializationContext(JsonOps.INSTANCE);
		Biome copy = Biome.DIRECT_CODEC.encodeStart(ops, original)
				.flatMap(json -> Biome.DIRECT_CODEC.parse(ops, json))
				.getOrThrow(error -> new IllegalStateException("Could not copy biome " + key.identifier() + ": " + error));
		applyModifiers(registries, key, copy, getSortedModifiers(registries));
		return copy;
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
