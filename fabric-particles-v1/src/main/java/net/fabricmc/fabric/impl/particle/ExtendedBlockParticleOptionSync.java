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

package net.fabricmc.fabric.impl.particle;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public class ExtendedBlockParticleOptionSync implements ModInitializer {
	private static final PacketContext.Key<Boolean> ENCODE_FALLBACK = PacketContext.key(Identifier.fromNamespaceAndPath("fabric", "extended_block_particle_fallback"));
	private static final Identifier PACKET_ID = Identifier.fromNamespaceAndPath("fabric", "extended_block_particle_option_sync");

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.clientboundConfiguration().register(DummyPayload.ID, DummyPayload.CODEC);
		ServerConfigurationConnectionEvents.CONFIGURE.register((listener, _) -> {
			listener.getPacketContext().set(ENCODE_FALLBACK, !ServerConfigurationNetworking.canSend(listener, PACKET_ID));
		});
	}

	public static boolean shouldEncodeFallback() {
		PacketContext context = PacketContext.get();

		if (context == null) {
			return true;
		}

		return context.orElse(ENCODE_FALLBACK, true);
	}

	public record DummyPayload() implements CustomPacketPayload {
		public static final DummyPayload INSTANCE = new DummyPayload();
		public static final StreamCodec<FriendlyByteBuf, DummyPayload> CODEC = StreamCodec.unit(INSTANCE);
		public static final CustomPacketPayload.Type<DummyPayload> ID = new Type<>(PACKET_ID);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}
}
