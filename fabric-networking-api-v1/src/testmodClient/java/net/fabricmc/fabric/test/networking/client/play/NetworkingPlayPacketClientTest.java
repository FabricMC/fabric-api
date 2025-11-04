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

package net.fabricmc.fabric.test.networking.client.play;

import java.util.Objects;

import com.mojang.brigadier.Command;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.test.networking.NetworkingTestmods;
import net.fabricmc.fabric.test.networking.play.NetworkingPlayPacketTest;

public final class NetworkingPlayPacketClientTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Register the payload only on the client.
		PayloadTypeRegistry.playC2S().register(UnknownPayload.ID, UnknownPayload.CODEC);

		ClientPlayConnectionEvents.INIT.register((handler, client) -> ClientPlayNetworking.registerReceiver(NetworkingPlayPacketTest.OverlayPacket.ID, (payload, context) -> {
			Objects.requireNonNull(context);
			Objects.requireNonNull(context.client());
			Objects.requireNonNull(context.player());

			context.client().gui.setOverlayMessage(payload.message(), true);
		}));

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(
				ClientCommandManager.literal("clientnetworktestcommand")
						.then(ClientCommandManager.literal("unknown").executes(context -> {
							ClientPlayNetworking.send(new UnknownPayload("Hello"));
							return Command.SINGLE_SUCCESS;
						}
		))));
	}

	private record UnknownPayload(String data) implements CustomPacketPayload {
		private static final CustomPacketPayload.Type<UnknownPayload> ID = new Type<>(NetworkingTestmods.id("unknown_test_channel_c2s"));
		private static final StreamCodec<FriendlyByteBuf, UnknownPayload> CODEC = ByteBufCodecs.STRING_UTF8.map(UnknownPayload::new, UnknownPayload::data).cast();

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}
}
