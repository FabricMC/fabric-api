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

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.client.networking.v1.C2SConfigurationChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.impl.networking.ChannelInfoHolder;
import net.fabricmc.fabric.impl.networking.RegistrationPayload;
import net.fabricmc.fabric.mixin.networking.client.accessor.ClientCommonPacketListenerImplAccessor;
import net.fabricmc.fabric.mixin.networking.client.accessor.ClientConfigurationPacketListenerImplAccessor;

public final class ClientConfigurationNetworkAddon extends ClientCommonNetworkAddon<ClientConfigurationNetworking.ConfigurationPayloadHandler<?>, ClientConfigurationPacketListenerImpl> {
	private final ContextImpl context;
	private boolean sentInitialRegisterPacket;
	private boolean hasStarted;

	public ClientConfigurationNetworkAddon(ClientConfigurationPacketListenerImpl handler, Minecraft client) {
		super(ClientNetworkingImpl.CONFIGURATION, ((ClientCommonPacketListenerImplAccessor) handler).getConnection(), "ClientPlayNetworkAddon for " + ((ClientConfigurationPacketListenerImplAccessor) handler).getProfile().name(), handler, client);
		this.context = new ContextImpl(client, handler, this);

		// Must register pending channels via lateinit
		this.registerPendingChannels((ChannelInfoHolder) this.connection, ConnectionProtocol.CONFIGURATION);
	}

	@Override
	protected void invokeInitEvent() {
		ClientConfigurationConnectionEvents.INIT.invoker().onConfigurationInit(this.handler, this.client);
	}

	@Override
	public void onServerReady() {
		super.onServerReady();
		invokeStartEvent();
	}

	@Override
	protected void receiveRegistration(boolean register, RegistrationPayload payload) {
		super.receiveRegistration(register, payload);

		if (register && !this.sentInitialRegisterPacket) {
			this.sendInitialChannelRegistrationPacket();
			this.sentInitialRegisterPacket = true;

			this.onServerReady();
		}
	}

	@Override
	public boolean handle(CustomPacketPayload payload) {
		boolean result = super.handle(payload);

		if (payload instanceof BrandPayload) {
			// If we have received this without first receiving the registration packet, its likely a vanilla server.
			invokeStartEvent();
		}

		return result;
	}

	@Override
	protected boolean isOnReceiveThread() {
		// Configuration packets are handled on the network thread.
		return true;
	}

	private void invokeStartEvent() {
		if (!hasStarted) {
			hasStarted = true;
			ClientConfigurationConnectionEvents.START.invoker().onConfigurationStart(this.handler, this.client);
		}
	}

	@Override
	protected void receive(ClientConfigurationNetworking.ConfigurationPayloadHandler<?> handler, CustomPacketPayload payload) {
		((ClientConfigurationNetworking.ConfigurationPayloadHandler) handler).receive(payload, this.context);
	}

	// impl details
	@Override
	public Packet<?> createPacket(CustomPacketPayload packet) {
		return ClientPlayNetworking.createC2SPacket(packet);
	}

	@Override
	protected void invokeRegisterEvent(List<Identifier> ids) {
		C2SConfigurationChannelEvents.REGISTER.invoker().onChannelRegister(this.handler, this, this.client, ids);
	}

	@Override
	protected void invokeUnregisterEvent(List<Identifier> ids) {
		C2SConfigurationChannelEvents.UNREGISTER.invoker().onChannelUnregister(this.handler, this, this.client, ids);
	}

	public void handleComplete() {
		ClientConfigurationConnectionEvents.COMPLETE.invoker().onConfigurationComplete(this.handler, this.client);
		ClientConfigurationConnectionEvents.READY.invoker().onConfigurationReady(this.handler, this.client);
		ClientNetworkingImpl.setClientConfigurationAddon(null);
	}

	@Override
	protected void invokeDisconnectEvent() {
		ClientConfigurationConnectionEvents.DISCONNECT.invoker().onConfigurationDisconnect(this.handler, this.client);
	}

	public ChannelInfoHolder getChannelInfoHolder() {
		return (ChannelInfoHolder) ((ClientCommonPacketListenerImplAccessor) handler).getConnection();
	}

	private record ContextImpl(Minecraft client, ClientConfigurationPacketListenerImpl networkHandler, PacketSender responseSender) implements ClientConfigurationNetworking.Context {
		private ContextImpl {
			Objects.requireNonNull(client, "client");
			Objects.requireNonNull(networkHandler, "networkHandler");
			Objects.requireNonNull(responseSender, "responseSender");
		}
	}
}
