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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryListCodec;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;

import net.fabricmc.fabric.api.event.registry.entrylists.CustomRegistryEntryList;
import net.fabricmc.fabric.api.event.registry.entrylists.CustomRegistryEntryListSerializer;
import net.fabricmc.fabric.impl.registry.sync.entrylists.util.RegistryWrapperUtils;

/**
 * A default implementation of {@link RegistryEntryList} that contains everything not in a given list.
 * A best effort is made to keep a cache of the values, to avoid iterating the registry for every call.
 *
 * @param <T> type of entries held by this list
 */
public class InverseRegistryEntryList<T> implements CustomRegistryEntryList<T> {
	public static final CustomRegistryEntryListSerializer SERIALIZER = new Serializer();

	private final RegistryWrapper<T> wrapper;
	private final RegistryEntryList<T> opposite;
	@Nullable
	private List<RegistryEntry<T>> cache = null;

	public InverseRegistryEntryList(RegistryWrapper<T> wrapper, RegistryEntryList<T> opposite) {
		this.wrapper = wrapper;
		this.opposite = opposite;
		this.opposite.registerDependency(this);
	}

	public RegistryEntryList<T> getOpposite() {
		return opposite;
	}

	public RegistryWrapper<T> getWrapper() {
		return wrapper;
	}

	@Override
	public Stream<RegistryEntry<T>> stream() {
		return getList().stream();
	}

	@Override
	public int size() {
		return getList().size();
	}

	@Override
	public boolean isBound() {
		return opposite.isBound();
	}

	@Override
	public Either<TagKey<T>, List<RegistryEntry<T>>> getStorage() {
		return Either.right(this.getList());
	}

	@Override
	public Optional<RegistryEntry<T>> getRandom(Random random) {
		return Util.getRandomOrEmpty(getList(), random);
	}

	@Override
	public RegistryEntry<T> get(int index) {
		return getList().get(index);
	}

	@Override
	public boolean contains(RegistryEntry<T> entry) {
		return !this.opposite.contains(entry);
	}

	@Override
	public boolean ownerEquals(RegistryEntryOwner<T> owner) {
		return this.opposite.ownerEquals(owner);
	}

	@Override
	public Optional<TagKey<T>> getTagKey() {
		return Optional.empty();
	}

	@Override
	public @NotNull Iterator<RegistryEntry<T>> iterator() {
		return getList().iterator();
	}

	private List<RegistryEntry<T>> getList() {
		if (this.cache == null) {
			this.cache = this.wrapper
					.streamEntries()
					.filter(Predicate.not(this.opposite::contains))
					.collect(Collectors.toUnmodifiableList());
		}

		return this.cache;
	}

	@Override
	public CustomRegistryEntryListSerializer getSerializer() {
		return SERIALIZER;
	}

	@Override
	public void invalidate() {
		this.cache = null;
		CustomRegistryEntryList.super.invalidate();
	}

	@Override
	public String toString() {
		return "Inverse{" + this.getOpposite().toString() + "}";
	}

	private record Serializer() implements CustomRegistryEntryListSerializer {
		private static final Identifier ID = Identifier.of("fabric", "not");

		@Override
		public Identifier getIdentifier() {
			return ID;
		}

		@Override
		public <T1> MapCodec<? extends CustomRegistryEntryList<T1>> createCodec(RegistryKey<? extends Registry<T1>> registryKey, Codec<RegistryEntry<T1>> registryEntryCodec, boolean forceList) {
			return RecordCodecBuilder.<InverseRegistryEntryList<T1>>mapCodec(
					instance -> instance.apply2(
							InverseRegistryEntryList::new,
							RegistryWrapperUtils.createMapCodec(registryKey).forGetter(InverseRegistryEntryList::getWrapper),
							RegistryEntryListCodec.create(registryKey, registryEntryCodec, forceList).fieldOf("value").forGetter(InverseRegistryEntryList::getOpposite)
					)
			);
		}

		@Override
		public <T1> PacketCodec<RegistryByteBuf, ? extends CustomRegistryEntryList<T1>> createPacketCodec(RegistryKey<? extends Registry<T1>> registryKey) {
			return PacketCodec.<RegistryByteBuf, InverseRegistryEntryList<T1>, RegistryWrapper<T1>, RegistryEntryList<T1>>tuple(
					RegistryWrapperUtils.createPacketCodec(registryKey),
					InverseRegistryEntryList::getWrapper,
					PacketCodecs.registryEntryList(registryKey),
					InverseRegistryEntryList::getOpposite,
					InverseRegistryEntryList::new
			);
		}

		@Override
		public @NotNull String toString() {
			return "CustomRegistryEntryListSerializer{\"" + ID + "\"}";
		}
	}
}
