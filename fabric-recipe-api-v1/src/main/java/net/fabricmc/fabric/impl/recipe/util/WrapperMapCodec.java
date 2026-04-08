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

package net.fabricmc.fabric.impl.recipe.util;

import java.util.stream.Stream;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

public class WrapperMapCodec<A> extends MapCodec<A> {
	private final MapCodec<A> wrapped;
	private final Wrapper<A> wrapper;

	public WrapperMapCodec(MapCodec<A> wrapped, Wrapper<A> wrapper) {
		this.wrapped = wrapped;
		this.wrapper = wrapper;
	}

	@Override
	public <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
		return this.wrapper.encode(input, ops, prefix, this.wrapped);
	}

	@Override
	public <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input) {
		return this.wrapper.decode(ops, input, this.wrapped);
	}

	@Override
	public <T> Stream<T> keys(DynamicOps<T> ops) {
		return Stream.concat(wrapped.keys(ops), wrapper.keys(ops));
	}

	public interface Wrapper<A> {
		default <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix, MapEncoder<A> wrapped) {
			return wrapped.encode(input, ops, prefix);
		}

		default <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input, MapDecoder<A> wrapped) {
			return wrapped.decode(ops, input);
		}

		default <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.empty();
		}
	}
}
