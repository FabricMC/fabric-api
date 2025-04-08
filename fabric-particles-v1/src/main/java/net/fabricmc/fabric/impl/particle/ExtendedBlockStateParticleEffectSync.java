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

import java.util.Set;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.impl.networking.FabricRegistryByteBuf;

public class ExtendedBlockStateParticleEffectSync implements ModInitializer {
	private static final Identifier PACKET_ID = Identifier.of("fabric", "extended_block_state_particle_effect_sync");

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.configurationS2C().register(DummyPayload.ID, DummyPayload.CODEC);
	}

	public static boolean shouldEncodeFallback(RegistryByteBuf buf) {
		Set<Identifier> channels = ((FabricRegistryByteBuf) buf).fabric_getSendableConfigurationChannels();

		if (channels == null) {
			return true;
		}

		return !channels.contains(ExtendedBlockStateParticleEffectSync.PACKET_ID);
	}

	public record DummyPayload() implements CustomPayload {
		public static final DummyPayload INSTANCE = new DummyPayload();
		public static final PacketCodec<PacketByteBuf, DummyPayload> CODEC = PacketCodec.unit(INSTANCE);
		public static final CustomPayload.Id<DummyPayload> ID = new Id<>(PACKET_ID);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}
}
