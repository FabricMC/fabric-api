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

package net.fabricmc.fabric.impl.networking.splitter;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.PacketEncoder;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.networking.NetworkingImpl;

/**
 * A fake packet implementation used to pass already encoded data from {@link FabricPacketSplitter} to {@link PacketEncoder}.
 * Allows to avoid requiring to serialize the packet twice.
 */
public record PassthroughPacket(ByteBuf buf) implements Packet<PacketListener> {
	private static final PacketType<? extends Packet<PacketListener>> FAKE_TYPE = new PacketType<>(PacketFlow.SERVERBOUND, Identifier.fromNamespaceAndPath(NetworkingImpl.MOD_ID, "passthrough"));

	@Override
	public PacketType<? extends Packet<PacketListener>> type() {
		return FAKE_TYPE;
	}

	@Override
	public void handle(PacketListener listener) {
		throw new UnsupportedOperationException("This is not a real packet!");
	}
}
