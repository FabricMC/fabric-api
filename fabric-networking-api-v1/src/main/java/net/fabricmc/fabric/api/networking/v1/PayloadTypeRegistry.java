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

package net.fabricmc.fabric.api.networking.v1;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;

/**
 * A registry for payload types.
 */
@ApiStatus.NonExtendable
public interface PayloadTypeRegistry<B extends FriendlyByteBuf> {
	/**
	 * Registers a custom payload type.
	 *
	 * <p>This must be done on both the sending and receiving side, usually during mod initialization
	 * and <strong>before registering a packet handler</strong>.
	 *
	 * @param id    the id of the payload type
	 * @param codec the codec for the payload type
	 * @param <T>   the payload type
	 * @return the registered payload type
	 */
	<T extends CustomPacketPayload> CustomPacketPayload.TypeAndCodec<? super B, T> register(CustomPacketPayload.Type<T> id, StreamCodec<? super B, T> codec);

	/**
	 * Registers a large custom payload type.
	 *
	 * <p>This must be done on both the sending and receiving side, usually during mod initialization
	 * and <strong>before registering a packet handler</strong>.
	 *
	 * <p>Payload types registered with this method will be split into multiple packets,
	 * allowing to send packets larger than vanilla limited size.
	 *
	 * @param id            the id of the payload type
	 * @param codec         the codec for the payload type
	 * @param <T>           the payload type
	 * @param maxPacketSize the maximum size of payload packet
	 * @return the registered payload type
	 */
	<T extends CustomPacketPayload> CustomPacketPayload.TypeAndCodec<? super B, T> registerLarge(CustomPacketPayload.Type<T> id, StreamCodec<? super B, T> codec, int maxPacketSize);

	/**
	 * @return the {@link PayloadTypeRegistry} instance for the client to server configuration channel.
	 */
	static PayloadTypeRegistry<FriendlyByteBuf> configurationC2S() {
		return PayloadTypeRegistryImpl.CONFIGURATION_C2S;
	}

	/**
	 * @return the {@link PayloadTypeRegistry} instance for the server to client configuration channel.
	 */
	static PayloadTypeRegistry<FriendlyByteBuf> configurationS2C() {
		return PayloadTypeRegistryImpl.CONFIGURATION_S2C;
	}

	/**
	 * @return the {@link PayloadTypeRegistry} instance for the client to server play channel.
	 */
	static PayloadTypeRegistry<RegistryFriendlyByteBuf> playC2S() {
		return PayloadTypeRegistryImpl.PLAY_C2S;
	}

	/**
	 * @return the {@link PayloadTypeRegistry} instance for the server to client play channel.
	 */
	static PayloadTypeRegistry<RegistryFriendlyByteBuf> playS2C() {
		return PayloadTypeRegistryImpl.PLAY_S2C;
	}
}
