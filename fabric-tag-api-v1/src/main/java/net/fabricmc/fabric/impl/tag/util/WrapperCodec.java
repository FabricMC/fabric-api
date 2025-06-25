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

package net.fabricmc.fabric.impl.tag.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;

public record WrapperCodec<A>(Codec<A> wrapped, Wrapper<A> wrapper) implements Codec<A> {
	@Override
	public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
		return wrapper.decode(ops, input, wrapped);
	}

	@Override
	public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
		return wrapper.encode(input, ops, prefix, wrapped);
	}

	public interface Wrapper<A> {
		default <T> DataResult<T> encode(final A input, final DynamicOps<T> ops, final T prefix, Encoder<A> wrapped) {
			return wrapped.encode(input, ops, prefix);
		}
		default <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input, Decoder<A> wrapped) {
			return wrapped.decode(ops, input);
		}
	}
}
