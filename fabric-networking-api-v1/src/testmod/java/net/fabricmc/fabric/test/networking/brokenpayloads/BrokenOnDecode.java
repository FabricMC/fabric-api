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

package net.fabricmc.fabric.test.networking.brokenpayloads;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.fabricmc.fabric.test.networking.NetworkingTestmods;

public class BrokenOnDecode implements CustomPacketPayload {
	public static final StreamCodec<ByteBuf, BrokenOnDecode> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public void encode(ByteBuf output, BrokenOnDecode value) {
			output.writeCharSequence("the quick brown fox jumps over the lazy dog", StandardCharsets.UTF_8);
		}

		@Override
		public BrokenOnDecode decode(ByteBuf input) {
			input.readCharSequence(15, StandardCharsets.UTF_8);
			input.markReaderIndex();
			throw new RuntimeException("Error");
		}
	};

	public static final Type<BrokenOnDecode> TYPE = new Type<>(NetworkingTestmods.id("broken_on_decode"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
