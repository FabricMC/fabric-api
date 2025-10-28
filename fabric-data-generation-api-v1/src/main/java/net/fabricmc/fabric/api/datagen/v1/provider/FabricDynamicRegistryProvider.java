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

package net.fabricmc.fabric.api.datagen.v1.provider;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.PlacedFeature;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.fabricmc.fabric.impl.registry.sync.DynamicRegistriesImpl;

/**
 * A provider to help with data-generation of dynamic registry objects,
 * such as biomes, features, or message types.
 */
public abstract class FabricDynamicRegistryProvider implements DataProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(FabricDynamicRegistryProvider.class);

	private final FabricDataOutput output;
	private final CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture;

	public FabricDynamicRegistryProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
		this.output = output;
		this.registriesFuture = registriesFuture;
	}

	protected abstract void configure(RegistryWrapper.WrapperLookup registries, Entries entries);

	public static final class Entries {
		private final RegistryWrapper.WrapperLookup registries;
		// Registry ID -> Entries for that registry
		private final Map<Identifier, RegistryEntries<?>> queuedEntries;
		private final String modId;

		@ApiStatus.Internal
		Entries(RegistryWrapper.WrapperLookup registries, String modId) {
			this.registries = registries;
			this.queuedEntries = DynamicRegistries.getDynamicRegistries().stream()
					// Some modded dynamic registries might not be in the wrapper lookup, filter them out
					.filter(e -> registries.getOptional(e.key()).isPresent())
					.collect(Collectors.toMap(
							e -> e.key().getValue(),
							e -> RegistryEntries.create(registries, e)
					));
			this.modId = modId;
		}

		/**
		 * Gets access to all registry lookups.
		 */
		public RegistryWrapper.WrapperLookup getLookups() {
			return registries;
		}

		/**
		 * Gets a lookup for entries from the given registry.
		 */
		public <T> RegistryEntryLookup<T> getLookup(RegistryKey<? extends Registry<T>> registryKey) {
			return registries.getOrThrow(registryKey);
		}

		/**
		 * Returns a lookup for placed features. Useful when creating biomes.
		 */
		public RegistryEntryLookup<PlacedFeature> placedFeatures() {
			return getLookup(RegistryKeys.PLACED_FEATURE);
		}

		/**
		 * Returns a lookup for configured carvers. Useful when creating biomes.
		 */
		public RegistryEntryLookup<ConfiguredCarver<?>> configuredCarvers() {
			return getLookup(RegistryKeys.CONFIGURED_CARVER);
		}

		/**
		 * Gets a reference to a registry entry for use in other registrations.
		 */
		public <T> RegistryEntry<T> ref(RegistryKey<T> key) {
			RegistryEntries<T> entries = getQueuedEntries(key);
			return RegistryEntry.Reference.standAlone(entries.lookup, key);
		}

		/**
		 * Adds a new object to be data generated.
		 *
		 * @param key    The key of the resource to register.
		 * @param object The object to register.
		 * @return a reference to it for use in other objects.
		 */
		public <T> RegistryEntry<T> add(RegistryKey<T> key, T object) {
			return getQueuedEntries(key).add(key, object, null);
		}

		/**
		 * Adds a new object to be data generated with several resource conditions.
		 *
		 * @param key        The key of the resource to register.
		 * @param object     The object to register.
		 * @param conditions Conditions that must be satisfied to load this object.
		 * @return a reference to it for use in other objects.
		 */
		public <T> RegistryEntry<T> add(RegistryKey<T> key, T object, ResourceCondition... conditions) {
			return getQueuedEntries(key).add(key, object, conditions);
		}

		/**
		 * Adds a new object to be data generated.
		 *
		 * @param object The object to generate. This registry entry must have both a
		 *               {@linkplain RegistryEntry#hasKeyAndValue() key and value}.
		 */
		public <T> void add(RegistryEntry.Reference<T> object) {
			add(object.registryKey(), object.value());
		}

		/**
		 * Adds a new object to be data generated with several resource conditions.
		 *
		 * @param object     The object to generate. This registry entry must have both a
		 *                   {@linkplain RegistryEntry#hasKeyAndValue() key and value}.
		 * @param conditions Conditions that must be satisfied to load this object.
		 */
		public <T> void add(RegistryEntry.Reference<T> object, ResourceCondition... conditions) {
			add(object.registryKey(), object.value(), conditions);
		}

		/**
		 * Adds a new {@link RegistryKey} from a given {@link RegistryWrapper.Impl} to be data generated.
		 *
		 * @return a reference to it for use in other objects.
		 */
		public <T> RegistryEntry<T> add(RegistryWrapper.Impl<T> registry, RegistryKey<T> valueKey) {
			return add(valueKey, registry.getOrThrow(valueKey).value());
		}

		/**
		 * Adds a new {@link RegistryKey} from a given {@link RegistryWrapper.Impl} to be data generated.
		 *
		 * @param conditions Conditions that must be satisfied to load this object.
		 * @return a reference to it for use in other objects.
		 */
		public <T> RegistryEntry<T> add(RegistryWrapper.Impl<T> registry, RegistryKey<T> valueKey, ResourceCondition... conditions) {
			return add(valueKey, registry.getOrThrow(valueKey).value(), conditions);
		}

		/**
		 * All the registry entries whose namespace matches the current effective mod ID will be data generated.
		 */
		public <T> List<RegistryEntry<T>> addAll(RegistryWrapper.Impl<T> registry) {
			return registry.streamKeys()
					.filter(registryKey -> registryKey.getValue().getNamespace().equals(modId))
					.map(key -> add(registry, key))
					.toList();
		}

		@SuppressWarnings("unchecked")
		<T> RegistryEntries<T> getQueuedEntries(RegistryKey<T> key) {
			RegistryEntries<?> regEntries = queuedEntries.get(key.getRegistry());

			if (regEntries == null) {
				throw new IllegalArgumentException("Registry " + key.getRegistry() + " is not loaded from datapacks");
			}

			return (RegistryEntries<T>) regEntries;
		}
	}

	private record ConditionalEntry<T>(T value, @Nullable ResourceCondition... conditions) {
	}

	private static class RegistryEntries<T> {
		final RegistryEntryOwner<T> lookup;
		final RegistryKey<? extends Registry<T>> registry;
		final Codec<T> elementCodec;
		Map<RegistryKey<T>, ConditionalEntry<T>> entries = new IdentityHashMap<>();

		RegistryEntries(RegistryEntryOwner<T> lookup,
						RegistryKey<? extends Registry<T>> registry,
						Codec<T> elementCodec) {
			this.lookup = lookup;
			this.registry = registry;
			this.elementCodec = elementCodec;
		}

		static <T> RegistryEntries<T> create(RegistryWrapper.WrapperLookup lookups, RegistryLoader.Entry<T> loaderEntry) {
			RegistryWrapper.Impl<T> lookup = lookups.getOrThrow(loaderEntry.key());
			return new RegistryEntries<>(lookup, loaderEntry.key(), loaderEntry.elementCodec());
		}

		RegistryEntry<T> add(RegistryKey<T> key, T value, @Nullable ResourceCondition[] conditions) {
			if (entries.put(key, new ConditionalEntry<>(value, conditions)) != null) {
				throw new IllegalArgumentException("Trying to add registry key " + key + " more than once.");
			}

			return RegistryEntry.Reference.standAlone(lookup, key);
		}
	}

	@Override
	public CompletableFuture<?> run(DataWriter writer) {
		return registriesFuture.thenCompose(registries -> {
			return CompletableFuture
					.supplyAsync(() -> {
						Entries entries = new Entries(registries, output.getModId());
						configure(registries, entries);
						return entries;
					})
					.thenCompose(entries -> {
						final RegistryOps<JsonElement> dynamicOps = registries.getOps(JsonOps.INSTANCE);
						ArrayList<CompletableFuture<?>> futures = new ArrayList<>();

						for (RegistryEntries<?> registryEntries : entries.queuedEntries.values()) {
							futures.add(writeRegistryEntries(writer, dynamicOps, registryEntries));
						}

						return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
					});
		});
	}

	private <T> CompletableFuture<?> writeRegistryEntries(DataWriter writer, RegistryOps<JsonElement> ops, RegistryEntries<T> entries) {
		final RegistryKey<? extends Registry<T>> registry = entries.registry;
		final boolean shouldOmitNamespace = registry.getValue().getNamespace().equals(Identifier.DEFAULT_NAMESPACE) || !DynamicRegistriesImpl.FABRIC_DYNAMIC_REGISTRY_KEYS.contains(registry);
		final String directoryName = shouldOmitNamespace ? registry.getValue().getPath() : registry.getValue().getNamespace() + "/" + registry.getValue().getPath();
		final DataOutput.PathResolver pathResolver = output.getResolver(DataOutput.OutputType.DATA_PACK, directoryName);
		final List<CompletableFuture<?>> futures = new ArrayList<>();

		for (Map.Entry<RegistryKey<T>, ConditionalEntry<T>> entry : entries.entries.entrySet()) {
			Path path = pathResolver.resolveJson(entry.getKey().getValue());
			futures.add(writeToPath(path, writer, ops, entries.elementCodec, entry.getValue().value(), entry.getValue().conditions()));
		}

		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
	}

	private static <E> CompletableFuture<?> writeToPath(Path path, DataWriter cache, DynamicOps<JsonElement> json, Encoder<E> encoder, E value, @Nullable ResourceCondition[] conditions) {
		Optional<JsonElement> optional = encoder.encodeStart(json, value).resultOrPartial((error) -> {
			LOGGER.error("Couldn't serialize element {}: {}", path, error);
		});

		if (optional.isPresent()) {
			JsonElement jsonElement = optional.get();

			if (conditions != null && conditions.length > 0) {
				if (!jsonElement.isJsonObject()) {
					throw new IllegalStateException("Cannot add conditions to " + path + ": JSON is a non-object value");
				} else {
					FabricDataGenHelper.addConditions(jsonElement.getAsJsonObject(), conditions);
				}
			}

			return DataProvider.writeToPath(cache, jsonElement, path);
		}

		return CompletableFuture.completedFuture(null);
	}
}
