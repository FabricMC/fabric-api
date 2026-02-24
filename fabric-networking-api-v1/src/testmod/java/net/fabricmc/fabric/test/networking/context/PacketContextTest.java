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

package net.fabricmc.fabric.test.networking.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.test.networking.NetworkingTestmods;

public class PacketContextTest implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(PacketContextTest.class);

	public static final PacketContext.Key<String> STRING_KEY = PacketContext.key(Identifier.fromNamespaceAndPath("fabric", "string_key"));
	public static final String STRING_VALUE = "Hello World!";

	@Override
	public void onInitialize() {
		// Context checking packet
		PayloadTypeRegistry.clientboundConfiguration().register(ContextCheckPacket.TYPE, ContextCheckPacket.CODEC);
		PayloadTypeRegistry.serverboundConfiguration().register(ContextCheckPacket.TYPE, ContextCheckPacket.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(ContextCheckPacket.TYPE, ContextCheckPacket.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ContextCheckPacket.TYPE, ContextCheckPacket.CODEC);

		// Store some example value when logging in.
		ServerLoginConnectionEvents.INIT.register((listener, server) -> {
			listener.getPacketContext().set(STRING_KEY, STRING_VALUE);
		});

		// Context check.
		ServerConfigurationConnectionEvents.CONFIGURE.register((listener, server) -> {
			listener.send(new ClientboundCustomPayloadPacket(new ContextCheckPacket("Server Configuration")));
		});

		// Write it in play.
		ServerPlayConnectionEvents.JOIN.register((listener, sender, server) -> {
			String stringValue = listener.getPacketContext().orElseThrow(STRING_KEY);

			LOGGER.info("PacketContext value: {}", stringValue);
			sender.sendPacket(new ContextCheckPacket("Server Play"));
		});
	}

	public record ContextCheckPacket(String source) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<ContextCheckPacket> TYPE = new Type<>(NetworkingTestmods.id("context_check"));
		public static final StreamCodec<FriendlyByteBuf, ContextCheckPacket> CODEC = CustomPacketPayload.codec(ContextCheckPacket::write, ContextCheckPacket::new);

		public ContextCheckPacket(FriendlyByteBuf buf) {
			this(buf.readUtf());

			try {
				PacketContext.orElseThrow();
			} catch (Throwable e) {
				LOGGER.error("Failed to retrieve read context! Packet source: {}", this.source);
				throw e;
			}
		}

		public void write(FriendlyByteBuf buf) {
			try {
				PacketContext.orElseThrow();
			} catch (Throwable e) {
				LOGGER.error("Failed to retrieve write context! Packet source: {}", this.source);
				throw e;
			}

			buf.writeUtf(this.source);
		}

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}
}
