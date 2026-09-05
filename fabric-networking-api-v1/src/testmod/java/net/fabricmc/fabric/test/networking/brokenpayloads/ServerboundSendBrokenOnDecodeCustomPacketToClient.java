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

public record ServerboundSendBrokenOnDecodeCustomPacketToClient() implements CustomPacketPayload {
	public static final StreamCodec<ByteBuf, ServerboundSendBrokenOnDecodeCustomPacketToClient> STREAM_CODEC = StreamCodec.unit(new ServerboundSendBrokenOnDecodeCustomPacketToClient());

	public static final Type<ServerboundSendBrokenOnDecodeCustomPacketToClient> TYPE = new Type<>(NetworkingTestmods.id("send_broken_on_decode"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
