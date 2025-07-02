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

package net.fabricmc.fabric.impl.registry.sync.entrylists.util;

import java.util.Set;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.impl.networking.FabricRegistryByteBuf;

/**
 * Allows checking if a {@link RegistryByteBuf} has the registry sync module installed on the other side.
 */
public class ChannelUtil {
	//this is a dummy, it should never actually be used
	private static final PacketCodec<ByteBuf, CustomPayload> CODEC = PacketCodec.unit(null);

	private static final CustomPayload.Id<CustomPayload> ID = new CustomPayload.Id<>(
			Identifier.of("fabric", "is_installed_v0")
	);

	public static void init() {
		PayloadTypeRegistry.configurationC2S().register(ID, CODEC);
		PayloadTypeRegistry.configurationS2C().register(ID, CODEC);
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean isInstalled(RegistryByteBuf buf) {
		if (!(buf instanceof FabricRegistryByteBuf fabricRegistryByteBuf)) {
			return false;
		}

		Set<Identifier> channels = fabricRegistryByteBuf.fabric_getSendableConfigurationChannels();

		if (channels == null) {
			return false;
		}

		return channels.contains(ID.id());
	}
}
