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

package net.fabricmc.fabric.impl.dimension.modification;

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
import net.minecraft.world.level.dimension.DimensionType;

import net.fabricmc.fabric.api.dimension.v1.DimensionModificationContext;
import net.fabricmc.fabric.api.dimension.v1.DimensionSelectionContext;
import net.fabricmc.fabric.api.dimension.v1.ModificationPhase;

public class DimensionModificationImpl {
	private static final Logger LOGGER = LoggerFactory.getLogger(DimensionModificationImpl.class);

	private static final Comparator<ModifierRecord> MODIFIER_ORDER_COMPARATOR = Comparator.<ModifierRecord>comparingInt(r -> r.phase.ordinal()).thenComparingInt(r -> r.order).thenComparing(r -> r.id);

	public static final DimensionModificationImpl INSTANCE = new DimensionModificationImpl();

	private final List<ModifierRecord> modifiers = new ArrayList<>();

	private boolean modifiersUnsorted = true;

	private DimensionModificationImpl() {
	}

	public void addModifier(Identifier id, ModificationPhase phase, Predicate<DimensionSelectionContext> selector, BiConsumer<DimensionSelectionContext, DimensionModificationContext> modifier) {
		Objects.requireNonNull(selector);
		Objects.requireNonNull(modifier);

		modifiers.add(new ModifierRecord(phase, id, selector, modifier));
		modifiersUnsorted = true;
	}

	public void addModifier(Identifier id, ModificationPhase phase, Predicate<DimensionSelectionContext> selector, Consumer<DimensionModificationContext> modifier) {
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

		// Now that we apply dimension modifications inside the MinecraftServer constructor, we should only ever do
		// this once for a dynamic registry manager. Marking the dynamic registry manager as modified ensures a crash
		// if the precondition is violated.
		DimensionModificationMarker modificationTracker = (DimensionModificationMarker) impl;
		modificationTracker.fabric_markDimensionsModified();

		Registry<DimensionType> dimensions = impl.lookupOrThrow(Registries.DIMENSION_TYPE);

		// Build a list of all dimension keys in ascending order of their raw-id to get a consistent result in case
		// someone does something stupid.
		List<ResourceKey<DimensionType>> keys = dimensions.entrySet().stream()
				.map(Map.Entry::getKey)
				.sorted(Comparator.comparingInt(key -> dimensions.getId(dimensions.getValueOrThrow(key))))
				.toList();

		List<ModifierRecord> sortedModifiers = getSortedModifiers();

		int dimensionsChanged = 0;
		int dimensionsProcessed = 0;
		int modifiersApplied = 0;

		for (ResourceKey<DimensionType> key : keys) {
			DimensionType dimension = dimensions.getValueOrThrow(key);

			dimensionsProcessed++;

			// Make a copy of the dimension to allow selection contexts to see it unmodified,
			// But do so only once it's known anything wants to modify the dimension at all
			DimensionSelectionContext context = new DimensionSelectionContextImpl(impl, key, dimension);
			DimensionModificationContextImpl modificationContext = null;

			for (ModifierRecord modifier : sortedModifiers) {
				if (modifier.selector.test(context)) {
					LOGGER.trace("Applying modifier {} to {}", modifier, key.identifier());

					// Create the copy only if at least one modifier applies, since it's pretty costly
					if (modificationContext == null) {
						dimensionsChanged++;
						modificationContext = new DimensionModificationContextImpl(dimension);
					}

					modifier.apply(context, modificationContext);
					modifiersApplied++;
				}
			}

			// Re-freeze and apply certain cleanup actions
			if (modificationContext != null) {
				if (dimensions instanceof MappedRegistry<DimensionType> registry) {
					RegistrationInfo info = registry.registrationInfos.get(key);
					RegistrationInfo newInfo = new RegistrationInfo(Optional.empty(), info.lifecycle());
					registry.registrationInfos.put(key, newInfo);
				}
			}
		}

		if (dimensionsProcessed > 0) {
			LOGGER.info("Applied {} dimensions modifications to {} of {} new dimensions in {}", modifiersApplied, dimensionsChanged,
					dimensionsProcessed, sw);
		}
	}

	private static class ModifierRecord {
		private final ModificationPhase phase;

		private final Identifier id;

		private final Predicate<DimensionSelectionContext> selector;

		private final BiConsumer<DimensionSelectionContext, DimensionModificationContext> contextSensitiveModifier;

		private final Consumer<DimensionModificationContext> modifier;

		// Whenever this is modified, the modifiers need to be resorted
		private int order;

		ModifierRecord(ModificationPhase phase, Identifier id, Predicate<DimensionSelectionContext> selector, Consumer<DimensionModificationContext> modifier) {
			this.phase = phase;
			this.id = id;
			this.selector = selector;
			this.modifier = modifier;
			this.contextSensitiveModifier = null;
		}

		ModifierRecord(ModificationPhase phase, Identifier id, Predicate<DimensionSelectionContext> selector, BiConsumer<DimensionSelectionContext, DimensionModificationContext> modifier) {
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

		public void apply(DimensionSelectionContext context, DimensionModificationContextImpl modificationContext) {
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
