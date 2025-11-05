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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.llamalad7.mixinextras.sugar.Local;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.UnconfiguredPipelineHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.networking.ChannelInfoHolder;
import net.fabricmc.fabric.impl.networking.NetworkHandlerExtensions;
import net.fabricmc.fabric.impl.networking.PacketCallbackListener;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.fabricmc.fabric.impl.networking.VanillaPacketTypes;
import net.fabricmc.fabric.impl.networking.splitter.FabricPacketMerger;
import net.fabricmc.fabric.impl.networking.splitter.FabricPacketSplitter;

@Mixin(Connection.class)
abstract class ConnectionMixin implements ChannelInfoHolder {
	@Shadow
	private PacketListener packetListener;

	@Unique
	private Map<ConnectionProtocol, Collection<Identifier>> playChannels;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void initAddedFields(PacketFlow side, CallbackInfo ci) {
		this.playChannels = new ConcurrentHashMap<>();
	}

	@Inject(method = "sendPacket", at = @At(value = "FIELD", target = "Lnet/minecraft/network/Connection;sentPackets:I"))
	private void checkPacket(Packet<?> packet, ChannelFutureListener callback, boolean flush, CallbackInfo ci) {
		if (this.packetListener instanceof PacketCallbackListener) {
			((PacketCallbackListener) this.packetListener).sent(packet);
		}
	}

	@Inject(method = "validateListener", at = @At("HEAD"))
	private void unwatchAddon(ProtocolInfo<?> state, PacketListener listener, CallbackInfo ci) {
		if (this.packetListener instanceof NetworkHandlerExtensions oldListener) {
			oldListener.getAddon().endSession();
		}
	}

	@Inject(method = "channelInactive", at = @At("HEAD"))
	private void disconnectAddon(ChannelHandlerContext channelHandlerContext, CallbackInfo ci) {
		if (packetListener instanceof NetworkHandlerExtensions extension) {
			extension.getAddon().handleDisconnect();
		}
	}

	@Inject(method = "handleDisconnection", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketListener;onDisconnect(Lnet/minecraft/network/DisconnectionDetails;)V"))
	private void disconnectAddon(CallbackInfo ci) {
		if (packetListener instanceof NetworkHandlerExtensions extension) {
			extension.getAddon().handleDisconnect();
		}
	}

	@ModifyArg(method = "setupInboundProtocol", at = @At(value = "INVOKE", target = "Lio/netty/channel/Channel;writeAndFlush(Ljava/lang/Object;)Lio/netty/channel/ChannelFuture;"))
	private Object injectFabricPacketSlitterHandlerInbound(Object transitioner, @Local(argsOnly = true) ProtocolInfo<?> state) {
		PayloadTypeRegistryImpl<?> payloadTypeRegistry = PayloadTypeRegistryImpl.get(state);

		if (payloadTypeRegistry == null) {
			return transitioner;
		}

		return ((UnconfiguredPipelineHandler.InboundConfigurationTask) transitioner).andThen((context) -> {
			FabricPacketMerger merger = new FabricPacketMerger(context.pipeline().get(PacketDecoder.class), payloadTypeRegistry, VanillaPacketTypes.get(state));
			context.pipeline().addAfter("decoder", "fabric:merger", merger);
		});
	}

	@ModifyArg(method = "setupOutboundProtocol", at = @At(value = "INVOKE", target = "Lio/netty/channel/Channel;writeAndFlush(Ljava/lang/Object;)Lio/netty/channel/ChannelFuture;"))
	private Object injectFabricPacketSlitterHandlerOutbound(Object transitioner, @Local(argsOnly = true) ProtocolInfo<?> state) {
		PayloadTypeRegistryImpl<?> payloadTypeRegistry = PayloadTypeRegistryImpl.get(state);

		if (payloadTypeRegistry == null) {
			return transitioner;
		}

		return ((UnconfiguredPipelineHandler.OutboundConfigurationTask) transitioner).andThen((context) -> {
			FabricPacketSplitter splitter = new FabricPacketSplitter(context.pipeline().get(PacketEncoder.class), payloadTypeRegistry);
			context.pipeline().addAfter("encoder", "fabric:splitter", splitter);
		});
	}

	@Override
	public Collection<Identifier> fabric_getPendingChannelsNames(ConnectionProtocol state) {
		return this.playChannels.computeIfAbsent(state, (key) -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
	}
}
