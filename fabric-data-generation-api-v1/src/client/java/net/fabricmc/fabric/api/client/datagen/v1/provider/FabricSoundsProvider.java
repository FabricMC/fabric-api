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

package net.fabricmc.fabric.api.client.datagen.v1.provider;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.datagen.v1.builder.SoundTypeBuilder;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.impl.datagen.client.SoundTypeBuilderImpl;

/**
 * Extend this class and implement {@link FabricSoundsProvider#configure(RegistryWrapper.WrapperLookup, SoundExporter)}.
 *
 * <p>Register an instance of the class with {@link FabricDataGenerator.Pack#addProvider} in a {@link net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint}.
 *
 * <p>Registered sound types will be appended to their own {@code sounds.json} in a namespace corresponding to
 * the id of the sound event they are assigned to.
 */
public abstract class FabricSoundsProvider implements DataProvider {
	private static final Codec<Map<String, SoundTypeBuilderImpl.SoundType>> CODEC = Codec.unboundedMap(Codec.STRING, SoundTypeBuilderImpl.SoundType.CODEC);
	private final CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture;
	private final DataOutput output;

	public FabricSoundsProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
		this.registriesFuture = registriesFuture;
		this.output = output;
	}

	@Override
	public CompletableFuture<?> run(DataWriter writer) {
		return registriesFuture.thenCompose(lookup -> {
			final Map<String, Map<String, SoundTypeBuilderImpl.SoundType>> data = new LinkedHashMap<>();
			configure(lookup, (id, builder) -> {
				if (data.computeIfAbsent(id.getNamespace(), n -> new LinkedHashMap<>()).put(id.getPath(), ((SoundTypeBuilderImpl) builder).build()) != null) {
					throw new IllegalStateException("Duplicate sound for event " + id);
				}
			});

			return CompletableFuture.allOf(data.entrySet().stream().map(file -> {
				Path outputPath = output.resolvePath(DataOutput.OutputType.RESOURCE_PACK).resolve(file.getKey() + "/sounds.json");
				return DataProvider.writeCodecToPath(writer, lookup, CODEC, file.getValue(), outputPath);
			}).toArray(CompletableFuture[]::new));
		});
	}

	/**
	 * Implement this method and then use {@link BiConsumer#accept} to register sound events to be data-generated.
	 *
	 * <p>Registered sound types will be appended to their own sounds.json in a namespace corresponding to
	 * the id of the sound event they are assigned to.
	 */
	protected abstract void configure(RegistryWrapper.WrapperLookup registryLookup, SoundExporter exporter);

	/**
	 * A consumer used by {@link FabricSoundsProvider#configure}.
	 */
	@ApiStatus.NonExtendable
	@FunctionalInterface
	public interface SoundExporter {
		/**
		 * Adds a sound event.
		 *
		 * @param event   the sound event
		 * @param builder the sound event details
		 */
		default void add(SoundEvent event, SoundTypeBuilder builder) {
			add(event.id(), builder);
		}

		/**
		 * Adds a sound event.
		 *
		 * @param event   registry entry for sound event
		 * @param builder the sound event details
		 *
		 * @throws IllegalArgumentException if the registry entry provided has not been registered
		 */
		default void add(RegistryEntry<SoundEvent> event, SoundTypeBuilder builder) {
			add(event.getKey().orElseThrow(() -> new IllegalArgumentException("Direct (non-registered) sound event cannot be added")).getValue(), builder);
		}

		/**
		 * Adds a sound event.
		 *
		 * @param id	  the id of a sound event
		 * @param builder the sound event details
		 */
		void add(Identifier id, SoundTypeBuilder builder);
	}
}
