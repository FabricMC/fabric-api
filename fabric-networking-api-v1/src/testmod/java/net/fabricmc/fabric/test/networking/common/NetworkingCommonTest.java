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

package net.fabricmc.fabric.test.networking.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.test.networking.NetworkingTestmods;

public class NetworkingCommonTest implements ModInitializer {
	private boolean firstLoad = true;
	private List<String> receivedPlay = new ArrayList<>();
	private List<String> receivedConfig = new ArrayList<>();

	@Override
	public void onInitialize() {
		// Register the payload on both sides for play and configuration
		PayloadTypeRegistry.playS2C().register(CommonPayload.ID, CommonPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(CommonPayload.ID, CommonPayload.CODEC);
		PayloadTypeRegistry.configurationS2C().register(CommonPayload.ID, CommonPayload.CODEC);
		PayloadTypeRegistry.configurationC2S().register(CommonPayload.ID, CommonPayload.CODEC);

		// When the client joins, send a packet expecting it to be echoed back
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sender.sendPacket(new CommonPayload("play")));
		ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> ServerConfigurationNetworking.send(handler, new CommonPayload("configuration")));

		// Store the player uuid once received from the client
		ServerPlayNetworking.registerGlobalReceiver(CommonPayload.ID, (payload, context) -> receivedPlay.add(context.player().getStringUUID()));
		ServerConfigurationNetworking.registerGlobalReceiver(CommonPayload.ID, (payload, context) -> receivedConfig.add(context.networkHandler().getOwner().id().toString()));

		AtomicLong runOnTick = new AtomicLong(-1);
		AtomicReference<String> uuid = new AtomicReference<>();

		// Ensure that the packets were received on the server
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (!firstLoad) {
				// No need to check again if the player changes dimensions
				return;
			}

			firstLoad = false;

			if (entity instanceof ServerPlayer player) {
				uuid.set(player.getStringUUID());
				runOnTick.set(player.level().getServer().getLevel(Level.OVERWORLD).getGameTime() + 50);
			}
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getLevel(Level.OVERWORLD).getGameTime() != runOnTick.get()) {
				return;
			}

			if (!receivedPlay.remove(uuid.get())) {
				throw new IllegalStateException("Did not receive play response");
			}

			if (!receivedConfig.remove(uuid.get())) {
				throw new IllegalStateException("Did not receive configuration response");
			}

			runOnTick.set(-1);
		});
	}

	// A payload registered on both sides, for play and configuration
	// This tests that the server can send a packet to the client, and then receive a response from the client
	public record CommonPayload(String data) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<CommonPayload> ID = new Type<>(NetworkingTestmods.id("common_payload"));
		public static final StreamCodec<FriendlyByteBuf, CommonPayload> CODEC = ByteBufCodecs.STRING_UTF8.map(CommonPayload::new, CommonPayload::data).cast();

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}
}
