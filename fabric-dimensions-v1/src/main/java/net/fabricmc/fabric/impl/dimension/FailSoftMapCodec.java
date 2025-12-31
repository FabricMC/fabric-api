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

package net.fabricmc.fabric.impl.dimension;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.codecs.BaseMapCodec;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Has the same functionality as [UnboundedMapCodec].
/// But it will fail-soft when an entry cannot be deserialized.
public record FailSoftMapCodec<K, V>(Codec<K> keyCodec, Codec<V> elementCodec) implements BaseMapCodec<K, V>, Codec<Map<K, V>> {
	private static final Logger LOGGER = LoggerFactory.getLogger("FailSoftMapCodec");

	@Override
	public <T> DataResult<Pair<Map<K, V>, T>> decode(final DynamicOps<T> ops, final T input) {
		return ops.getMap(input).setLifecycle(Lifecycle.stable()).flatMap(map -> decode(ops, map)).map(r -> Pair.of(r, input));
	}

	@Override
	public <T> DataResult<T> encode(final Map<K, V> input, final DynamicOps<T> ops, final T prefix) {
		return encode(input, ops, ops.mapBuilder()).build(prefix);
	}

	/// In [BaseMapCodec#decode(DynamicOps, MapLike)],
	/// the whole deserialization will fail if one element fails.
	/// `apply2stable` will return fail when any of the two elements is failed.
	/// In this implementation, if one deserialization fails, it will log and ignore.
	/// The result will always be success.
	/// It will not output partial result when some entries fail deserialization because
	/// currently (MC 1.19.3) the dimension data deserialization rejects partial result.
	@Override
	public <T> DataResult<Map<K, V>> decode(final DynamicOps<T> ops, final MapLike<T> input) {
		final ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();

		input.entries().forEach(pair -> {
			try {
				final DataResult<K> k = keyCodec().parse(ops, pair.getFirst());
				final DataResult<V> v = elementCodec().parse(ops, pair.getSecond());

				Optional<K> optionalK = k.result();
				Optional<V> optionalV = v.result();

				if (optionalK.isEmpty()) {
					LOGGER.error("Failed to decode key {} from {}  {}", k, pair, k.resultOrPartial());
				}

				if (optionalV.isEmpty()) {
					LOGGER.error("Failed to decode value {} from {}  {}", k, pair, v.resultOrPartial());
				}

				if (optionalK.isPresent() && optionalV.isPresent()) {
					builder.put(optionalK.get(), optionalV.get());
				} else {
					// ignore failure
				}
			} catch (Throwable e) {
				LOGGER.error("Decoding {}", pair, e);
			}
		});

		final Map<K, V> elements = builder.build();

		return DataResult.success(elements);
	}

	@Override
	public String toString() {
		return "FailSoftMapCodec[" + keyCodec + " -> " + elementCodec + ']';
	}
}
