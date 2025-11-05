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

package net.fabricmc.fabric.mixin.networking;

import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import net.fabricmc.fabric.impl.networking.NetworkHandlerExtensions;
import net.fabricmc.fabric.impl.networking.PacketCallbackListener;
import net.fabricmc.fabric.impl.networking.payload.PacketByteBufLoginQueryResponse;
import net.fabricmc.fabric.impl.networking.server.ServerLoginNetworkAddon;

@Mixin(ServerLoginPacketListenerImpl.class)
abstract class ServerLoginPacketListenerImplMixin implements NetworkHandlerExtensions, PacketCallbackListener {
	@Shadow
	protected abstract void verifyLoginAndFinishConnectionSetup(GameProfile gameProfile);

	@Unique
	private ServerLoginNetworkAddon addon;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void initAddon(CallbackInfo ci) {
		this.addon = new ServerLoginNetworkAddon((ServerLoginPacketListenerImpl) (Object) this);
		// A bit of a hack but it allows the field above to be set in case someone registers handlers during INIT event which refers to said field
		this.addon.lateInit();
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;verifyLoginAndFinishConnectionSetup(Lcom/mojang/authlib/GameProfile;)V"))
	private void handlePlayerJoin(ServerLoginPacketListenerImpl instance, GameProfile profile) {
		// Do not accept the player, thereby moving into play stage until all login futures being waited on are completed
		if (this.addon.queryTick()) {
			this.verifyLoginAndFinishConnectionSetup(profile);
		}
	}

	@Inject(method = "handleCustomQueryPacket", at = @At("HEAD"), cancellable = true)
	private void handleCustomPayloadReceivedAsync(ServerboundCustomQueryAnswerPacket packet, CallbackInfo ci) {
		// Handle queries
		if (this.addon.handle(packet)) {
			ci.cancel();
		} else {
			if (packet.payload() instanceof PacketByteBufLoginQueryResponse response) {
				response.data().skipBytes(response.data().readableBytes());
			}
		}
	}

	@Redirect(method = "verifyLoginAndFinishConnectionSetup", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getCompressionThreshold()I", ordinal = 0))
	private int removeLateCompressionPacketSending(MinecraftServer server) {
		return -1;
	}

	@Override
	public void sent(Packet<?> packet) {
		if (packet instanceof ClientboundCustomQueryPacket) {
			this.addon.registerOutgoingPacket((ClientboundCustomQueryPacket) packet);
		}
	}

	@Override
	public ServerLoginNetworkAddon getAddon() {
		return this.addon;
	}
}
