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

package net.fabricmc.fabric.test.networking.client;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.test.networking.NetworkingTestmods;

public class DisconnectScreenTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		PayloadTypeRegistry.serverboundPlay().register(ServerboundSendBrokenOnReceiveCustomPacketToClient.TYPE, ServerboundSendBrokenOnReceiveCustomPacketToClient.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ServerboundSendBrokenOnSendCustomPacketToClient.TYPE, ServerboundSendBrokenOnSendCustomPacketToClient.STREAM_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ServerboundSendBrokenOnReceiveCustomPacketToClient.TYPE, (_, context) -> {
			context.responseSender().sendPacket(new BrokenOnReceive());
		});
		ServerPlayNetworking.registerGlobalReceiver(ServerboundSendBrokenOnSendCustomPacketToClient.TYPE, (_, context) -> {
			context.responseSender().sendPacket(new BrokenOnSend());
		});
		PayloadTypeRegistry.clientboundPlay().register(BrokenOnReceive.TYPE, BrokenOnReceive.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(BrokenOnSend.TYPE, BrokenOnSend.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(BrokenOnReceive.TYPE, BrokenOnReceive.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(BrokenOnSend.TYPE, BrokenOnSend.STREAM_CODEC);
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommands.literal("disconnect_screen_test").executes(context -> {
				StringBuilder builder = new StringBuilder("A very long disconnect reason:");

				for (int i = 0; i < 100; i++) {
					builder.append("\nLine ").append(i + 1);
				}

				context.getSource().getPlayer().connection.getConnection().disconnect(Component.nullToEmpty(builder.toString()));
				return 1;
			}));
			dispatcher.register(ClientCommands.literal("clientbound_broken_on_receive_test").executes(context -> {
				context.getSource().getPlayer().connection.send(ClientPlayNetworking.createServerboundPacket(new ServerboundSendBrokenOnReceiveCustomPacketToClient()));
				return 1;
			}));
			dispatcher.register(ClientCommands.literal("clientbound_broken_on_send_test").executes(context -> {
				context.getSource().getPlayer().connection.send(ClientPlayNetworking.createServerboundPacket(new ServerboundSendBrokenOnSendCustomPacketToClient()));
				return 1;
			}));
			dispatcher.register(ClientCommands.literal("serverbound_broken_on_receive_test").executes(context -> {
				context.getSource().getPlayer().connection.send(ClientPlayNetworking.createServerboundPacket(new BrokenOnReceive()));
				return 1;
			}));
			dispatcher.register(ClientCommands.literal("serverbound_broken_on_send_test").executes(context -> {
				context.getSource().getPlayer().connection.send(ClientPlayNetworking.createServerboundPacket(new BrokenOnSend()));
				return 1;
			}));
		});
	}

	record ServerboundSendBrokenOnReceiveCustomPacketToClient() implements CustomPacketPayload {
		static final StreamCodec<ByteBuf, ServerboundSendBrokenOnReceiveCustomPacketToClient> STREAM_CODEC = StreamCodec.unit(new ServerboundSendBrokenOnReceiveCustomPacketToClient());

		private static final Type<ServerboundSendBrokenOnReceiveCustomPacketToClient> TYPE = new Type<>(NetworkingTestmods.id("send_broken_on_receive"));

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	record ServerboundSendBrokenOnSendCustomPacketToClient() implements CustomPacketPayload {
		static final StreamCodec<ByteBuf, ServerboundSendBrokenOnSendCustomPacketToClient> STREAM_CODEC = StreamCodec.unit(new ServerboundSendBrokenOnSendCustomPacketToClient());

		private static final Type<ServerboundSendBrokenOnSendCustomPacketToClient> TYPE = new Type<>(NetworkingTestmods.id("send_broken_on_send"));

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	record BrokenOnReceive() implements CustomPacketPayload {
		static final StreamCodec<ByteBuf, BrokenOnReceive> STREAM_CODEC = new StreamCodec<>() {
			@Override
			public void encode(ByteBuf output, BrokenOnReceive value) {
				output.writeCharSequence("the quick brown fox jumps over the lazy dog", StandardCharsets.UTF_8);
			}

			@Override
			public BrokenOnReceive decode(ByteBuf input) {
				throw new RuntimeException("Error");
			}
		};

		private static final Type<BrokenOnReceive> TYPE = new Type<>(NetworkingTestmods.id("broken_on_receive"));

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	record BrokenOnSend() implements CustomPacketPayload {
		static final StreamCodec<ByteBuf, BrokenOnSend> STREAM_CODEC = new StreamCodec<>() {
			@Override
			public void encode(ByteBuf output, BrokenOnSend value) {
				throw new RuntimeException("Error");
			}

			@Override
			public BrokenOnSend decode(ByteBuf input) {
				return new BrokenOnSend();
			}
		};

		private static final Type<BrokenOnSend> TYPE = new Type<>(NetworkingTestmods.id("broken_on_send"));

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}
}
