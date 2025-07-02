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

package net.fabricmc.fabric.impl.registry.sync.entrylists.defaults;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;

import net.fabricmc.fabric.api.event.registry.entrylists.CustomRegistryEntryList;
import net.fabricmc.fabric.api.event.registry.entrylists.CustomRegistryEntryListSerializer;
import net.fabricmc.fabric.impl.registry.sync.entrylists.util.RegistryWrapperUtils;

/**
 * A default implementation of {@link RegistryEntryList} that contains everything in a given registry
 * No caching is done.
 *
 * @param <T> type of entries held by this list
 */
public record UniversalRegistryEntryList<T>(RegistryWrapper<T> source) implements CustomRegistryEntryList<T> {
	public static final CustomRegistryEntryListSerializer SERIALIZER = new Serializer();

	@Override
	public Stream<RegistryEntry<T>> stream() {
		return source.streamEntries().map(Function.identity());
	}

	@Override
	public int size() {
		return Math.toIntExact(source.streamEntries().count());
	}

	@Override
	public boolean isBound() {
		return true;
	}

	@Override
	public Either<TagKey<T>, List<RegistryEntry<T>>> getStorage() {
		return Either.right(stream().toList());
	}

	@Override
	public Optional<RegistryEntry<T>> getRandom(Random random) {
		return Util.getRandomOrEmpty(stream().toList(), random);
	}

	@Override
	public RegistryEntry<T> get(int index) {
		return stream().toList().get(index);
	}

	@Override
	public boolean contains(RegistryEntry<T> entry) {
		return entry.getKey()
				.flatMap(source::getOptional)
				.isPresent();
	}

	@Override
	public boolean ownerEquals(RegistryEntryOwner<T> owner) {
		return true;
	}

	@Override
	public Optional<TagKey<T>> getTagKey() {
		return Optional.empty();
	}

	@Override
	public @NotNull Iterator<RegistryEntry<T>> iterator() {
		return stream().iterator();
	}

	@Override
	public CustomRegistryEntryListSerializer getSerializer() {
		return SERIALIZER;
	}

	@Override
	public @NotNull String toString() {
		return "Universal{" + source.toString() + "}";
	}

	private record Serializer() implements CustomRegistryEntryListSerializer {
		private static final Identifier ID = Identifier.of("fabric", "all");

		@Override
		public Identifier getIdentifier() {
			return ID;
		}

		@Override
		public <T1> MapCodec<? extends CustomRegistryEntryList<T1>> createCodec(RegistryKey<? extends Registry<T1>> registryKey, Codec<RegistryEntry<T1>> registryEntryCodec, boolean forceList) {
			return RegistryWrapperUtils.createMapCodec(registryKey).xmap(
					UniversalRegistryEntryList::new,
					UniversalRegistryEntryList::source
			);
		}

		@Override
		public <T1> PacketCodec<RegistryByteBuf, ? extends CustomRegistryEntryList<T1>> createPacketCodec(RegistryKey<? extends Registry<T1>> registryKey) {
			return RegistryWrapperUtils.createPacketCodec(registryKey).xmap(
					UniversalRegistryEntryList::new,
					UniversalRegistryEntryList::source
			);
		}

		@Override
		public @NotNull String toString() {
			return "CustomRegistryEntryListSerializer{\"" + ID + "\"}";
		}
	}
}
