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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryListCodec;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.event.registry.entrylists.CustomRegistryEntryList;
import net.fabricmc.fabric.api.event.registry.entrylists.CustomRegistryEntryListSerializer;

/**
 * A default implementation of {@link RegistryEntryList} that contains everything in all of it's backing lists.
 * the resulting list is cached as a set and a list, to provide a consistent iteration order, and O(1) contains
 *
 * @param <T> type of entries held by this list
 */

public class IntersectionRegistryEntryList<T> extends MultiPartRegistryEntryList<T> {
	public static final CustomRegistryEntryListSerializer SERIALIZER = new Serializer();

	public IntersectionRegistryEntryList(List<RegistryEntryList<T>> parts) {
		super(parts);
	}

	@Override
	protected Set<RegistryEntry<T>> createCache() {
		if (this.getParts().isEmpty()) {
			return Set.of();
		}

		if (this.getParts().size() == 1) {
			return this.getParts()
					.getFirst()
					.stream()
					.collect(Collectors.toUnmodifiableSet());
		}

		List<RegistryEntryList<T>> remaining = this.getParts().subList(1, getParts().size());

		return getParts()
				.getFirst()
				.stream()
				.filter(it -> remaining.stream().allMatch(
						registryEntryList -> registryEntryList.contains(it)
				))
				.collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public CustomRegistryEntryListSerializer getSerializer() {
		return SERIALIZER;
	}

	@Override
	public String toString() {
		return "Intersection{" + this.getParts() + "}";
	}

	private record Serializer() implements CustomRegistryEntryListSerializer {
		private static final Identifier ID = Identifier.of("fabric", "and");

		@Override
		public Identifier getIdentifier() {
			return ID;
		}

		@Override
		public <T> MapCodec<? extends CustomRegistryEntryList<T>> createCodec(RegistryKey<? extends Registry<T>> registryKey, Codec<RegistryEntry<T>> entryCodec, boolean forceList) {
			return RegistryEntryListCodec.create(registryKey, entryCodec, forceList)
					.listOf()
					.xmap(
							IntersectionRegistryEntryList::new,
							MultiPartRegistryEntryList::getParts
					)
					.fieldOf("values");
		}

		@Override
		public <T> PacketCodec<RegistryByteBuf, ? extends CustomRegistryEntryList<T>> createPacketCodec(RegistryKey<? extends Registry<T>> registryKey) {
			return PacketCodecs.registryEntryList(registryKey)
					.collect(PacketCodecs.toList())
					.xmap(
							IntersectionRegistryEntryList::new,
							MultiPartRegistryEntryList::getParts
					);
		}

		@Override
		public @NotNull String toString() {
			return "CustomRegistryEntryListSerializer{\"" + ID + "\"}";
		}
	}
}
