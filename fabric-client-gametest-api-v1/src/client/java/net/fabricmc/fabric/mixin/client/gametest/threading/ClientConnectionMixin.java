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

package net.fabricmc.fabric.mixin.client.gametest.threading;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.netty.channel.ChannelHandlerContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.Packet;

import net.fabricmc.fabric.impl.client.gametest.threading.NetworkSynchronizer;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
	@Shadow
	@Final
	private NetworkSide side;

	@WrapMethod(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V")
	private void onNettyReceivePacket(ChannelHandlerContext context, Packet<?> packet, Operation<Void> original) {
		NetworkSynchronizer synchronizer = side == NetworkSide.CLIENTBOUND ? NetworkSynchronizer.CLIENTBOUND : NetworkSynchronizer.SERVERBOUND;
		synchronizer.preNettyHandlePacket();

		try {
			original.call(context, packet);
		} finally {
			synchronizer.postNettyHandlePacket();
		}
	}

	@Inject(method = "sendImmediately", at = @At("HEAD"))
	private void onSendPacket(CallbackInfo ci) {
		NetworkSynchronizer synchronizer = side == NetworkSide.CLIENTBOUND ? NetworkSynchronizer.SERVERBOUND : NetworkSynchronizer.CLIENTBOUND;
		synchronizer.preSendPacket();
	}
}
