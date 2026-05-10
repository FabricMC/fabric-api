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

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import net.minecraft.core.Holder;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;

public class DataComponentNetworkSerialization {
	public static ClientboundUpdateComponentsPayload serialize(DynamicOps<Tag> ops, LayeredRegistryAccess<RegistryLayer> registries) {
		return new ClientboundUpdateComponentsPayload(
				RegistrySynchronization.networkSafeRegistries(registries)
						.collect(Collectors.toMap(
								RegistryAccess.RegistryEntry::key,
								entry -> serialize(ops, entry.value())
						))
		);
	}

	private static Map<Identifier, Map<Identifier, Tag>> serialize(DynamicOps<Tag> ops, Registry<?> registry) {
		Map<Identifier, Map<Identifier, Tag>> result = new HashMap<>();

		registry.listElements().forEach(holder -> {
			if (holder.components().isEmpty()) return;

			Map<Identifier, Tag> serialized = result.computeIfAbsent(holder.key().identifier(), _ -> new HashMap<>());
			holder.components().forEach(component -> {
				serialized.put(
						BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component.type()),
						component.encodeValue(ops).getOrThrow()
				);
			});
		});

		return result;
	}

	private record BakedEntry<T>(Holder.Reference<T> element, DataComponentMap components) {
		public void apply() {
			this.element.bindComponents(this.components);
		}
	}

	public static List<DataComponentInitializers.PendingComponents<?>> deserialize(
			Map<ResourceKey<? extends Registry<?>>, Map<Identifier, Map<Identifier, Tag>>> registryToComponents,
			RegistryAccess registries
	) {
		RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);

		List<DataComponentInitializers.PendingComponents<?>> result = new ArrayList<>();

		registryToComponents.forEach((registryKey, holderComponents) -> {
			result.add(deserialize(ops, registries.lookupOrThrow(registryKey), holderComponents));
		});

		return result;
	}

	private static <T> DataComponentInitializers.PendingComponents<T> deserialize(RegistryOps<Tag> ops, Registry<T> registry, Map<Identifier, Map<Identifier, Tag>> holderComponents) {
		List<BakedEntry<T>> entries = new ArrayList<>();

		holderComponents.forEach((id, components) -> {
			DataComponentMap.Builder builder = DataComponentMap.builder();

			components.forEach((componentId, encodedValue) -> {
				DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.getOptional(componentId).orElseThrow();
				parse(ops, type, encodedValue, builder);
			});

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

	private static <T> void parse(RegistryOps<Tag> ops, DataComponentType<T> type, Tag tag, DataComponentMap.Builder builder) {
		DataResult<T> result = type.codecOrThrow().parse(ops, tag);
		builder.set(
				type,
				result.getOrThrow()
		);
	}
}
