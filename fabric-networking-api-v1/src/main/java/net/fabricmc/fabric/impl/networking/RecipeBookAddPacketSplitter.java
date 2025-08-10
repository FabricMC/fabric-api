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

package net.fabricmc.fabric.impl.networking;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.handler.PacketInflater;
import net.minecraft.network.packet.s2c.play.RecipeBookAddS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

public final class RecipeBookAddPacketSplitter {
	// -1 byte due the the "replace" boolean
	private static final int MAXIMUM_PACKET_SIZE = PacketInflater.MAXIMUM_PACKET_SIZE - 1;

	private RecipeBookAddPacketSplitter() {
	}

	public static void split(RecipeBookAddS2CPacket packet, ServerPlayNetworkHandler networkHandler, Consumer<RecipeBookAddS2CPacket> packetSender) {
		RegistryByteBuf byteBuf = new RegistryByteBuf(PacketByteBufs.create(), networkHandler.getPlayer().getRegistryManager());
		List<RecipeBookAddS2CPacket.Entry> collectedEntries = new ArrayList<>();
		// Ensure that only the first packet sets the replace flag
		boolean shouldReplace = packet.replace();

		for (RecipeBookAddS2CPacket.Entry entry : packet.entries()) {
			int beforeSize = byteBuf.readableBytes();
			RecipeBookAddS2CPacket.Entry.PACKET_CODEC.encode(byteBuf, entry);
			int entrySize = byteBuf.readableBytes() - beforeSize;

			if (entrySize > MAXIMUM_PACKET_SIZE) {
				// Individual entry is larger than the max size, cannot be sent.
				throw new IllegalStateException("Entry is larger than the max size: " + entry);
			}

			if (byteBuf.readableBytes() >= MAXIMUM_PACKET_SIZE) {
				// Packet would be larger than the max size, send what fits, the current entry will be sent in the next packet.
				packetSender.accept(new RecipeBookAddS2CPacket(collectedEntries, shouldReplace));

				collectedEntries = new ArrayList<>();
				byteBuf = new RegistryByteBuf(PacketByteBufs.create(), networkHandler.getPlayer().getRegistryManager());
				shouldReplace = false;

				// Must re-encode the current entry so it gets counted.
				RecipeBookAddS2CPacket.Entry.PACKET_CODEC.encode(byteBuf, entry);
			}

			collectedEntries.add(entry);
		}

		packetSender.accept(new RecipeBookAddS2CPacket(collectedEntries, shouldReplace));
	}
}
