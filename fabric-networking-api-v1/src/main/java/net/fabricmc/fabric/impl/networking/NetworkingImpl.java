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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.impl.networking.splitter.FabricSplitPacketPayload;

public final class NetworkingImpl {
	public static final String MOD_ID = "fabric-networking-api-v1";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/**
	 * Id of packet used to register supported channels.
	 */
	public static final Identifier REGISTER_CHANNEL = Identifier.withDefaultNamespace("register");

	/**
	 * Id of packet used to unregister supported channels.
	 */
	public static final Identifier UNREGISTER_CHANNEL = Identifier.withDefaultNamespace("unregister");

	public static boolean isReservedCommonChannel(Identifier channelName) {
		return channelName.equals(REGISTER_CHANNEL) || channelName.equals(UNREGISTER_CHANNEL);
	}

	public static void init() {
		// Legacy register / unregister packets
		PayloadTypeRegistry.configurationS2C().register(RegistrationPayload.REGISTER, RegistrationPayload.REGISTER_CODEC);
		PayloadTypeRegistry.configurationS2C().register(RegistrationPayload.UNREGISTER, RegistrationPayload.UNREGISTER_CODEC);
		PayloadTypeRegistry.configurationC2S().register(RegistrationPayload.REGISTER, RegistrationPayload.REGISTER_CODEC);
		PayloadTypeRegistry.configurationC2S().register(RegistrationPayload.UNREGISTER, RegistrationPayload.UNREGISTER_CODEC);
		PayloadTypeRegistry.playS2C().register(RegistrationPayload.REGISTER, RegistrationPayload.REGISTER_CODEC);
		PayloadTypeRegistry.playS2C().register(RegistrationPayload.UNREGISTER, RegistrationPayload.UNREGISTER_CODEC);
		PayloadTypeRegistry.playC2S().register(RegistrationPayload.REGISTER, RegistrationPayload.REGISTER_CODEC);
		PayloadTypeRegistry.playC2S().register(RegistrationPayload.UNREGISTER, RegistrationPayload.UNREGISTER_CODEC);

		// Fabric Packet Splitter packet
		registerGeneric(FabricSplitPacketPayload.ID, FabricSplitPacketPayload.CODEC);
	}

	private static <T extends CustomPacketPayload> void registerGeneric(CustomPacketPayload.Type<T> id, StreamCodec<? super FriendlyByteBuf, T> codec) {
		PayloadTypeRegistry.configurationS2C().register(id, codec);
		PayloadTypeRegistry.configurationC2S().register(id, codec);
		PayloadTypeRegistry.playS2C().register(id, codec);
		PayloadTypeRegistry.playC2S().register(id, codec);
	}
}
