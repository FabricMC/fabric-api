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

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.fabricmc.fabric.test.networking.NetworkingTestmods;

public record BrokenOnEncode() implements CustomPacketPayload {
	public static final StreamCodec<ByteBuf, BrokenOnEncode> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public void encode(ByteBuf output, BrokenOnEncode value) {
			throw new RuntimeException("Error");
		}

		@Override
		public BrokenOnEncode decode(ByteBuf input) {
			return new BrokenOnEncode();
		}
	};

	public static final Type<BrokenOnEncode> TYPE = new Type<>(NetworkingTestmods.id("broken_on_encode"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
