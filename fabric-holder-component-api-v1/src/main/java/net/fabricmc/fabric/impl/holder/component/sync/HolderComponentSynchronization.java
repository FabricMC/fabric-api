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

package net.fabricmc.fabric.impl.holder.component.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;

import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;

public class HolderComponentSynchronization {
	public static final StreamCodec<ByteBuf, Identifier> SHORT_IDENTIFIER_CODEC = ByteBufCodecs.STRING_UTF8
			.map(Identifier::parse, Identifier::toShortString);
	public static final StreamCodec<ByteBuf, ResourceKey<? extends Registry<?>>> SHORT_REGISTRY_KEY_CODEC = SHORT_IDENTIFIER_CODEC
			.map(ResourceKey::createRegistryKey, ResourceKey::identifier);
	public static final StreamCodec<RegistryFriendlyByteBuf, DataComponentMap> COMPONENT_MAP_CODEC = TypedDataComponent.STREAM_CODEC
			.apply(ByteBufCodecs.list())
			.map(
					components -> DataComponentMap.builder().setAll(components).build(),
					componentMap -> ImmutableList.copyOf(componentMap.iterator())
			);
	public static final StreamCodec<ByteBuf, ByteBuf> BYTE_BUF_SLICE_CODEC = StreamCodec.of(
			(output, value) -> {
				VarInt.write(output, value.readableBytes());
				output.writeBytes(value);
			},
			buf -> buf.readRetainedSlice(VarInt.read(buf))
	);

	public static ClientboundUpdateComponentsPayload serialize(LayeredRegistryAccess<RegistryLayer> registries) {
		return new ClientboundUpdateComponentsPayload(
				RegistrySynchronization.networkSafeRegistries(registries)
						.map(registryEntry -> serialize(registries.compositeAccess(), registryEntry))
						.toList()
		);
	}

	private static <T> ByteBuf serialize(
			RegistryAccess registries,
			RegistryAccess.RegistryEntry<T> registryEntry
	) {
		Map<Holder.Reference<T>, DataComponentMap> entries = new Reference2ObjectOpenHashMap<>();

		registryEntry.value().listElements()
				.filter(holder -> !holder.components().isEmpty())
				.forEach(holder -> entries.put(holder, holder.components()));

		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(FriendlyByteBufs.create(), registries);
		BakedEntries.CODEC.encode(buf, new BakedEntries<>(registryEntry.key(), entries));
		return buf;
	}

	private record BakedEntries<T>(ResourceKey<? extends Registry<? extends T>> key, Map<Holder.Reference<T>, DataComponentMap> entries) implements DataComponentInitializers.PendingComponents<T> {
		private static final StreamCodec<RegistryFriendlyByteBuf, BakedEntries<?>> CODEC = new StreamCodec<>() {
			@Override
			public BakedEntries<?> decode(RegistryFriendlyByteBuf input) {
				ResourceKey<? extends Registry<?>> registryKey = SHORT_REGISTRY_KEY_CODEC.decode(input);
				return decode(registryKey, input);
			}

			@Override
			public void encode(RegistryFriendlyByteBuf output, BakedEntries<?> value) {
				encode(value, output);
			}

			private static <T> BakedEntries<T> decode(ResourceKey<? extends Registry<? extends T>> registryKey, RegistryFriendlyByteBuf input) {
				int count = VarInt.read(input);
				Registry<T> registry = input.registryAccess().lookupOrThrow(registryKey);
				Map<Holder.Reference<T>, DataComponentMap> entries = new Reference2ObjectOpenHashMap<>();

				for (int i = 0; i < count; i++) {
					entries.put(registry.get(VarInt.read(input)).orElseThrow(), COMPONENT_MAP_CODEC.decode(input));
				}

				return new BakedEntries<>(registryKey, entries);
			}

			private static <T> void encode(BakedEntries<T> bakedEntries, RegistryFriendlyByteBuf output) {
				SHORT_REGISTRY_KEY_CODEC.encode(output, bakedEntries.key);
				IdMap<Holder<T>> idMap = output.registryAccess().lookupOrThrow(bakedEntries.key).asHolderIdMap();
				VarInt.write(output, bakedEntries.entries.size());

				for (Map.Entry<Holder.Reference<T>, DataComponentMap> entry : bakedEntries.entries.entrySet()) {
					VarInt.write(output, idMap.getId(entry.getKey()));
					COMPONENT_MAP_CODEC.encode(output, entry.getValue());
				}
			}
		};

		@Override
		public void forEach(BiConsumer<Holder.Reference<T>, DataComponentMap> output) {
			entries.forEach(output);
		}

		@Override
		public void apply() {
			forEach(Holder.Reference::bindComponents);
		}
	}

	public static List<DataComponentInitializers.PendingComponents<?>> deserialize(
			ClientboundUpdateComponentsPayload payload,
			RegistryAccess registries
	) {
		List<DataComponentInitializers.PendingComponents<?>> result = new ArrayList<>();

		for (ByteBuf buf : payload.registryToComponents()) {
			result.add(deserialize(registries, buf));
		}

		return result;
	}

	private static BakedEntries<?> deserialize(RegistryAccess registries, ByteBuf buf) {
		return BakedEntries.CODEC.decode(new RegistryFriendlyByteBuf(buf, registries));
	}
}
