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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import io.netty.buffer.ByteBuf;

import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;

import net.minecraft.core.Holder;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;

public class HolderComponentSynchronization {
	public static ClientboundUpdateComponentsPayload serialize(LayeredRegistryAccess<RegistryLayer> registries) {
		return new ClientboundUpdateComponentsPayload(
				RegistrySynchronization.networkSafeRegistries(registries)
						.collect(Collectors.toMap(
								RegistryAccess.RegistryEntry::key,
								entry -> serialize(registries.compositeAccess(), entry.value())
						))
		);
	}

	private static Map<Identifier, List<PackedComponentMap>> serialize(
			RegistryAccess registries,
			Registry<?> registry
	) {
		Map<Identifier, List<PackedComponentMap>> result = new HashMap<>();

		registry.listElements().forEach(holder -> {
			if (holder.components().isEmpty()) return;

			List<PackedComponentMap> serialized = result.computeIfAbsent(holder.key().identifier(), _ -> new ArrayList<>());
			for (TypedDataComponent<?> component : holder.components()) {
				Identifier id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component.type());
				RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(FriendlyByteBufs.create(), registries);
				encode(component, buf);

				serialized.add(
						new PackedComponentMap(
								id,
								buf
						)
				);
			}
		});

		return result;
	}

	private static <T> void encode(TypedDataComponent<T> component, RegistryFriendlyByteBuf buf) {
		component.type().streamCodec().encode(buf, component.value());
	}

	private record BakedEntry<T>(Holder.Reference<T> element, DataComponentMap components) {
		public void apply() {
			this.element.bindComponents(this.components);
		}
	}

	public static List<DataComponentInitializers.PendingComponents<?>> deserialize(
			ClientboundUpdateComponentsPayload payload,
			RegistryAccess registries
	) {
		List<DataComponentInitializers.PendingComponents<?>> result = new ArrayList<>();

		payload.registryToComponents().forEach((registryKey, holderComponents) -> {
			result.add(deserialize(registries, registries.lookupOrThrow(registryKey), holderComponents));
		});

		return result;
	}

	private static <T> DataComponentInitializers.PendingComponents<T> deserialize(RegistryAccess registries, Registry<T> registry, Map<Identifier, List<PackedComponentMap>> holderComponents) {
		List<BakedEntry<T>> entries = new ArrayList<>();

		holderComponents.forEach((id, components) -> {
			DataComponentMap.Builder builder = DataComponentMap.builder();

			for (PackedComponentMap map : components) {
				DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.getOptional(map.id).orElseThrow();
				parse(registries, type, map.data, builder);

				map.data.release();
			}

			entries.add(new BakedEntry<>(registry.get(id).orElseThrow(), builder.build()));
		});

		return new DataComponentInitializers.PendingComponents<>() {
			@Override
			public ResourceKey<? extends Registry<? extends T>> key() {
				return registry.key();
			}

			@Override
			public void forEach(BiConsumer<Holder.Reference<T>, DataComponentMap> output) {
				for (BakedEntry<T> entry : entries) {
					output.accept(entry.element, entry.components);
				}
			}

			@Override
			public void apply() {
				for (BakedEntry<T> entry : entries) {
					entry.apply();
				}
			}
		};
	}

	private static <T> void parse(RegistryAccess registries, DataComponentType<T> type, ByteBuf buf, DataComponentMap.Builder builder) {
		T result = type.streamCodec().decode(new RegistryFriendlyByteBuf(buf, registries));
		builder.set(
				type,
				result
		);
	}

	public record PackedComponentMap(Identifier id, ByteBuf data) {
		public static final StreamCodec<ByteBuf, PackedComponentMap> STREAM_CODEC = StreamCodec.composite(
				Identifier.STREAM_CODEC, PackedComponentMap::id,
				StreamCodec.of(
						(output, value) -> {
							output.writeInt(value.readableBytes());
							output.writeBytes(value);
						},
						buf -> buf.readRetainedSlice(buf.readInt())
				), PackedComponentMap::data,
				PackedComponentMap::new
		);
	}
}
