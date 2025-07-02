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

package net.fabricmc.fabric.impl.registry.sync.entrylists;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.event.registry.entrylists.CustomRegistryEntryListSerializer;

public final class CustomRegistryEntryListSerializerImpl {
	private CustomRegistryEntryListSerializerImpl() {
		throw new UnsupportedOperationException();
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomRegistryEntryListSerializerImpl.class);
	private static final Map<Identifier, CustomRegistryEntryListSerializer> SERIALIZERS = new HashMap<>();
	public static final Codec<CustomRegistryEntryListSerializer> SERIALIZER_CODEC = Identifier.CODEC.flatXmap(
			id -> Optional.ofNullable(SERIALIZERS.get(id))
					.map(DataResult::success)
					.orElseGet(
							() -> DataResult.error(
									() -> "Unknown CustomRegistryEntryListSerializer: " + id
							)
					),
			serializer -> Optional.of(serializer.getIdentifier())
					.filter(SERIALIZERS::containsKey)
					.map(DataResult::success)
					.orElseGet(
							() -> DataResult.error(
									() -> "Unknown CustomRegistryEntryListSerializer: " + serializer
							)
					)
	);

	public static <T> boolean isSerializedCustomRegistryEntryList(DynamicOps<T> ops, T entryList) {
		return ops.getMap(entryList)
				.map(it -> it.get("fabric:type"))
				.map(Objects::nonNull)
				.result()
				.orElse(false);
	}

	public static void registerSerializer(CustomRegistryEntryListSerializer serializer) {
		CustomRegistryEntryListSerializer previous = SERIALIZERS.put(serializer.getIdentifier(), serializer);

		if (previous != null) {
			LOGGER.warn("Overwriting CustomRegistryEntryListSerializer {} with {}", previous, serializer);
		}
	}

	public static CustomRegistryEntryListSerializer getSerializer(Identifier id) {
		return SERIALIZERS.get(id);
	}
}
