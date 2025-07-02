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

package net.fabricmc.fabric.impl.registry.sync.entrylists.util;

import java.util.stream.Stream;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;

/**
 * Utility class for working with {@link RegistryWrapper}s.
 */
public class RegistryWrapperUtils {
	/**
	 * gets a {@link RegistryWrapper} from a {@link RegistryEntryLookup}.
	 * first checks if the entryLookup is a registryWrapper, then, if not, constructs a more expensive wrapper the bridges the loss of information
	 *
	 * @param <T>                 the type of elements in the registry
	 * @param registryEntryLookup the entryLookup to wrap
	 * @return the entryLookup cast to a registryWrapper, or, if it isn't one, an expensive mimicry
	 */
	public static <T> RegistryWrapper<T> castOrCreateFromEntryLookup(RegistryEntryLookup<T> registryEntryLookup) {
		if (registryEntryLookup instanceof RegistryWrapper<T> registryWrapper) {
			return registryWrapper;
		}

		return new RegistryWrapperFromRegistryEntryLookup<>(registryEntryLookup);
	}

	/**
	 * Creates a {@link MapCodec} for a {@link RegistryWrapper} based on the provided {@link RegistryKey}.
	 * the Wrapper is not encoded in any way and is fetched from the RegistryOps on decode.
	 *
	 * @param registryKey the key of the registry to create a wrapper for
	 * @param <V>         the type of elements in the registry
	 * @return a MapCodec that can decode a {@link RegistryWrapper} from the registry key
	 */
	public static <V> MapCodec<RegistryWrapper<V>> createMapCodec(RegistryKey<? extends Registry<V>> registryKey) {
		return new MapCodec<>() {
			@Override
			public <T> Stream<T> keys(DynamicOps<T> ops) {
				// no keys, this doesn't touch this input or the output
				return Stream.empty();
			}

			@Override
			public <T> DataResult<RegistryWrapper<V>> decode(DynamicOps<T> ops, MapLike<T> input) {
				if (!(ops instanceof RegistryOps<T> registryOps)) {
					return DataResult.error(() -> "RegistryWrapperCodec requires RegistryOps");
				}

				return registryOps.getEntryLookup(registryKey)
						.map(RegistryWrapperUtils::castOrCreateFromEntryLookup)
						.map(DataResult::success)
						.orElseGet(() -> DataResult.error(() -> "Registry Not Found"));
			}

			@Override
			public <T> RecordBuilder<T> encode(RegistryWrapper<V> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
				// No need to encode
				return prefix;
			}
		};
	}

	/**
	 * Creates a {@link PacketCodec} for a {@link RegistryWrapper} based on the provided {@link RegistryKey}.
	 * the Wrapper is not encoded in any way and is fetched from the RegistryByteBuf on decode.
	 *
	 * @param registryKey the key of the registry to create a wrapper for
	 * @param <V>         the type of elements in the registry
	 * @return a PacketCodec that can decode a {@link RegistryWrapper} from the registry key
	 */
	public static <V> PacketCodec<RegistryByteBuf, RegistryWrapper<V>> createPacketCodec(RegistryKey<? extends Registry<V>> registryKey) {
		return new PacketCodec<>() {
			@Override
			public RegistryWrapper<V> decode(RegistryByteBuf buf) {
				return buf.getRegistryManager().getOrThrow(registryKey);
			}

			@Override
			public void encode(RegistryByteBuf buf, RegistryWrapper<V> value) {
				// No need to encode
			}
		};
	}
}
