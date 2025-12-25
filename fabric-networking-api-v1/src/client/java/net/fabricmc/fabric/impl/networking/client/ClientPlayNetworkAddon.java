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

package net.fabricmc.fabric.impl.networking.client;

import java.util.List;
import java.util.Objects;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ServerboundPlayChannelEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.impl.networking.ChannelInfoHolder;

public final class ClientPlayNetworkAddon extends ClientCommonNetworkAddon<ClientPlayNetworking.PlayPayloadHandler<?>, ClientPacketListener> {
	private final ContextImpl context;

	private static final Logger LOGGER = LogUtils.getLogger();

	public ClientPlayNetworkAddon(ClientPacketListener listener, Minecraft client) {
		super(ClientNetworkingImpl.PLAY, listener.getConnection(), "ClientPlayNetworkAddon for " + listener.getLocalGameProfile().name(), listener, client);
		this.context = new ContextImpl(client, this);

		// Must register pending channels via lateinit
		this.registerPendingChannels((ChannelInfoHolder) this.connection, ConnectionProtocol.PLAY);
	}

	@Override
	protected void invokeInitEvent() {
		ClientPlayConnectionEvents.INIT.invoker().onPlayInit(this.listener, this.client);
	}

	@Override
	public void onServerReady() {
		try {
			ClientPlayConnectionEvents.JOIN.invoker().onPlayReady(this.listener, this, this.client);
		} catch (RuntimeException e) {
			LOGGER.error("Exception thrown while invoking ClientPlayConnectionEvents.JOIN", e);
		}

		// The client cannot send any packets, including `minecraft:register` until after ClientboundLoginPacket is received.
		this.sendInitialChannelRegistrationPacket();
		super.onServerReady();
	}

	@Override
	protected boolean isOnReceiveThread() {
		return client.packetProcessor().isSameThread();
	}

	@Override
	protected void receive(ClientPlayNetworking.PlayPayloadHandler<?> handler, CustomPacketPayload payload) {
		((ClientPlayNetworking.PlayPayloadHandler) handler).receive(payload, context);
	}

	// impl details
	@Override
	public Packet<?> createPacket(CustomPacketPayload packet) {
		return ClientPlayNetworking.createServerboundPacket(packet);
	}

	@Override
	protected void invokeRegisterEvent(List<Identifier> ids) {
		ServerboundPlayChannelEvents.REGISTER.invoker().onChannelRegister(this.listener, this, this.client, ids);
	}

	@Override
	protected void invokeUnregisterEvent(List<Identifier> ids) {
		ServerboundPlayChannelEvents.UNREGISTER.invoker().onChannelUnregister(this.listener, this, this.client, ids);
	}

	@Override
	protected void invokeDisconnectEvent() {
		ClientPlayConnectionEvents.DISCONNECT.invoker().onPlayDisconnect(this.listener, this.client);
	}

	private record ContextImpl(Minecraft client, PacketSender responseSender) implements ClientPlayNetworking.Context {
		private ContextImpl {
			Objects.requireNonNull(client, "client");
			Objects.requireNonNull(responseSender, "responseSender");
		}

		@Override
		public LocalPlayer player() {
			return Objects.requireNonNull(client.player, "player");
		}
	}
}
