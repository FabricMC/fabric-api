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

package net.fabricmc.fabric.impl.networking.server;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.networking.AbstractChanneledNetworkAddon;
import net.fabricmc.fabric.impl.networking.ChannelInfoHolder;
import net.fabricmc.fabric.impl.networking.NetworkingImpl;
import net.fabricmc.fabric.impl.networking.RegistrationPayload;

public final class ServerPlayNetworkAddon extends AbstractChanneledNetworkAddon<ServerPlayNetworking.PlayPayloadHandler<?>> {
	private final ServerPlayNetworkHandler handler;
	private final MinecraftServer server;
	private final ServerPlayNetworking.Context context;

	private boolean sentInitialRegisterPacket;
	private boolean requestedReconfigure = false;

	public ServerPlayNetworkAddon(ServerPlayNetworkHandler handler, ClientConnection connection, MinecraftServer server) {
		super(ServerNetworkingImpl.PLAY, connection, "ServerPlayNetworkAddon for " + handler.player.getDisplayName());
		this.handler = handler;
		this.server = server;
		this.context = new ContextImpl(server, handler, this);

		// Must register pending channels via lateinit
		this.registerPendingChannels((ChannelInfoHolder) this.connection, NetworkPhase.PLAY);
	}

	@Override
	protected void invokeInitEvent() {
		ServerPlayConnectionEvents.INIT.invoker().onPlayInit(this.handler, this.server);
	}

	public void onClientReady() {
		ServerPlayConnectionEvents.JOIN.invoker().onPlayReady(this.handler, this, this.server);

		this.sendInitialChannelRegistrationPacket();
		this.sentInitialRegisterPacket = true;
	}

	@Override
	protected boolean isOnReceiveThread() {
		return server.getPacketApplyBatcher().isOnThread();
	}

	@Override
	protected void receive(ServerPlayNetworking.PlayPayloadHandler<?> payloadHandler, CustomPayload payload) {
		((ServerPlayNetworking.PlayPayloadHandler) payloadHandler).receive(payload, ServerPlayNetworkAddon.this.context);
	}

	// impl details

	@Override
	protected void schedule(Runnable task) {
		this.handler.player.getEntityWorld().getServer().execute(task);
	}

	@Override
	public Packet<?> createPacket(CustomPayload packet) {
		return ServerPlayNetworking.createS2CPacket(packet);
	}

	@Override
	protected void invokeRegisterEvent(List<Identifier> ids) {
		S2CPlayChannelEvents.REGISTER.invoker().onChannelRegister(this.handler, this, this.server, ids);
	}

	@Override
	protected void invokeUnregisterEvent(List<Identifier> ids) {
		S2CPlayChannelEvents.UNREGISTER.invoker().onChannelUnregister(this.handler, this, this.server, ids);
	}

	@Override
	protected void handleRegistration(Identifier channelName) {
		// If we can already send packets, immediately send the register packet for this channel
		if (this.sentInitialRegisterPacket) {
			RegistrationPayload registrationPayload = this.createRegistrationPayload(RegistrationPayload.REGISTER, Collections.singleton(channelName));

			if (registrationPayload != null) {
				this.sendPacket(registrationPayload);
			}
		}
	}

	@Override
	protected void handleUnregistration(Identifier channelName) {
		// If we can already send packets, immediately send the unregister packet for this channel
		if (this.sentInitialRegisterPacket) {
			RegistrationPayload registrationPayload = this.createRegistrationPayload(RegistrationPayload.UNREGISTER, Collections.singleton(channelName));

			if (registrationPayload != null) {
				this.sendPacket(registrationPayload);
			}
		}
	}

	@Override
	protected void invokeDisconnectEvent() {
		ServerPlayConnectionEvents.DISCONNECT.invoker().onPlayDisconnect(this.handler, this.server);
	}

	@Override
	protected boolean isReservedChannel(Identifier channelName) {
		return NetworkingImpl.isReservedCommonChannel(channelName);
	}

	public void reconfigure() {
		if (requestedReconfigure) {
			throw new IllegalStateException("Already requested reconfigure");
		}

		requestedReconfigure = true;
		handler.reconfigure();
	}

	public boolean requestedReconfigure() {
		return requestedReconfigure;
	}

	private record ContextImpl(MinecraftServer server, ServerPlayNetworkHandler handler, PacketSender responseSender) implements ServerPlayNetworking.Context {
		private ContextImpl {
			Objects.requireNonNull(server, "server");
			Objects.requireNonNull(handler, "handler");
			Objects.requireNonNull(responseSender, "responseSender");
		}

		@Override
		public ServerPlayerEntity player() {
			return handler.getPlayer();
		}
	}
}
