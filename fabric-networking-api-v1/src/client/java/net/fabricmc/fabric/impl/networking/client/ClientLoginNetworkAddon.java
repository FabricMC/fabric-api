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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.netty.channel.ChannelFutureListener;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.impl.networking.AbstractNetworkAddon;
import net.fabricmc.fabric.impl.networking.payload.PacketByteBufLoginQueryRequestPayload;
import net.fabricmc.fabric.impl.networking.payload.PacketByteBufLoginQueryResponse;
import net.fabricmc.fabric.mixin.networking.client.accessor.ClientHandshakePacketListenerImplAccessor;

public final class ClientLoginNetworkAddon extends AbstractNetworkAddon<ClientLoginNetworking.LoginQueryRequestHandler> {
	private final ClientHandshakePacketListenerImpl handler;
	private final Minecraft client;
	private boolean firstResponse = true;

	public ClientLoginNetworkAddon(ClientHandshakePacketListenerImpl handler, Minecraft client) {
		super(ClientNetworkingImpl.LOGIN, "ClientLoginNetworkAddon for Client");
		this.handler = handler;
		this.client = client;
	}

	@Override
	protected void invokeInitEvent() {
		ClientLoginConnectionEvents.INIT.invoker().onLoginStart(this.handler, this.client);
	}

	public boolean handlePacket(ClientboundCustomQueryPacket packet) {
		PacketByteBufLoginQueryRequestPayload payload = (PacketByteBufLoginQueryRequestPayload) packet.payload();
		return handlePacket(packet.transactionId(), packet.payload().id(), payload.data());
	}

	private boolean handlePacket(int queryId, Identifier channelName, FriendlyByteBuf originalBuf) {
		this.logger.debug("Handling inbound login response with id {} and channel with name {}", queryId, channelName);

		if (this.firstResponse) {
			ClientLoginConnectionEvents.QUERY_START.invoker().onLoginQueryStart(this.handler, this.client);
			this.firstResponse = false;
		}

		ClientLoginNetworking.@Nullable LoginQueryRequestHandler handler = this.getHandler(channelName);

		if (handler == null) {
			return false;
		}

		FriendlyByteBuf buf = PacketByteBufs.slice(originalBuf);
		List<ChannelFutureListener> callbacks = new ArrayList<>();

		try {
			CompletableFuture<@Nullable FriendlyByteBuf> future = handler.receive(this.client, this.handler, buf, callbacks::add);
			future.thenAccept(result -> {
				ServerboundCustomQueryAnswerPacket packet = new ServerboundCustomQueryAnswerPacket(queryId, result == null ? null : new PacketByteBufLoginQueryResponse(result));
				((ClientHandshakePacketListenerImplAccessor) this.handler).getConnection().send(packet, operation -> {
					for (ChannelFutureListener callback : callbacks) {
						callback.operationComplete(operation);
					}
				});
			});
		} catch (Throwable ex) {
			this.logger.error("Encountered exception while handling in channel with name \"{}\"", channelName, ex);
			throw ex;
		}

		return true;
	}

	@Override
	protected void handleRegistration(Identifier channelName) {
	}

	@Override
	protected void handleUnregistration(Identifier channelName) {
	}

	@Override
	protected void invokeDisconnectEvent() {
		ClientLoginConnectionEvents.DISCONNECT.invoker().onLoginDisconnect(this.handler, this.client);
	}

	@Override
	protected boolean isReservedChannel(Identifier channelName) {
		return false;
	}
}
