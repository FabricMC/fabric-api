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

package net.fabricmc.fabric.impl.recipe.ingredient;

import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.network.handler.EncoderHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerConfigurationTask;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.mixin.networking.accessor.ServerCommonNetworkHandlerAccessor;
import net.fabricmc.fabric.mixin.recipe.ingredient.EncoderHandlerMixin;

/**
 * To reasonably support server-side only custom ingredients, we only send custom ingredients to clients that support them.
 * If a specific client doesn't support a custom ingredient, we send the matching stacks as a regular ingredient.
 * This is fine since all recipe computation happens server-side anyway.
 *
 * <ul>
 *     <li>Each client sends a packet with the set of custom ingredients it supports.</li>
 *     <li>We store that set inside the {@link EncoderHandler} using {@link EncoderHandlerMixin}.</li>
 *     <li>When serializing a custom ingredient, we get access to the current {@link EncoderHandler},
 *     and based on that we decide whether to send the custom ingredient, or a vanilla ingredient with the matching stacks.</li>
 * </ul>
 */
public class CustomIngredientSync implements ModInitializer {
	public static final Identifier PACKET_ID = Identifier.of("fabric", "custom_ingredient_sync");
	public static final int PROTOCOL_VERSION_1 = 1;
	public static final ThreadLocal<Set<Identifier>> CURRENT_SUPPORTED_INGREDIENTS = new ThreadLocal<>();

	public static CustomIngredientPayloadC2S createResponsePayload(int serverProtocolVersion) {
		if (serverProtocolVersion < PROTOCOL_VERSION_1) {
			// Not supposed to happen - notify the server that we didn't understand the query.
			return null;
		}

		// Always send protocol 1 - the server should support it even if it supports more recent protocols.
		return new CustomIngredientPayloadC2S(PROTOCOL_VERSION_1, CustomIngredientImpl.REGISTERED_SERIALIZERS.keySet());
	}

	public static Set<Identifier> decodeResponsePayload(CustomIngredientPayloadC2S payload) {
		int protocolVersion = payload.protocolVersion();
		switch (protocolVersion) {
		case PROTOCOL_VERSION_1 -> {
			Set<Identifier> serializers = payload.registeredSerializers();
			// Remove unknown keys to save memory
			serializers.removeIf(id -> !CustomIngredientImpl.REGISTERED_SERIALIZERS.containsKey(id));
			return serializers;
		}
		default -> {
			throw new IllegalArgumentException("Unknown ingredient sync protocol version: " + protocolVersion);
		}
		}
	}

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.configurationC2S()
				.register(CustomIngredientPayloadC2S.ID, CustomIngredientPayloadC2S.CODEC);
		PayloadTypeRegistry.configurationS2C()
				.register(CustomIngredientPayloadS2C.ID, CustomIngredientPayloadS2C.CODEC);

		ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
			if (ServerConfigurationNetworking.canSend(handler, PACKET_ID)) {
				handler.addTask(new IngredientSyncTask());
			}
		});

		ServerConfigurationNetworking.registerGlobalReceiver(CustomIngredientPayloadC2S.ID, (payload, context) -> {
			Set<Identifier> supportedCustomIngredients = decodeResponsePayload(payload);
			((SupportedIngredientsClientConnection) ((ServerCommonNetworkHandlerAccessor) context.networkHandler()).getConnection()).fabric_setSupportedCustomIngredients(supportedCustomIngredients);
			context.networkHandler().completeTask(IngredientSyncTask.KEY);
		});
	}

	private record IngredientSyncTask() implements ServerPlayerConfigurationTask {
		public static final Key KEY = new Key(PACKET_ID.toString());

		@Override
		public void sendPacket(Consumer<Packet<?>> sender) {
			// Send packet with 1 so the client can send us back the list of supported tags.
			// 1 is sent in case we need a different protocol later for some reason.
			sender.accept(ServerConfigurationNetworking.createS2CPacket(new CustomIngredientPayloadS2C(PROTOCOL_VERSION_1)));
		}

		@Override
		public Key getKey() {
			return KEY;
		}
	}
}
