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

import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ConfigurationTask;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

/**
 * To reasonably support server-side only custom ingredients, we only send custom ingredients to clients that support them.
 * If a specific client doesn't support a custom ingredient, we send the matching stacks as a regular ingredient.
 * This is fine since all recipe computation happens server-side anyway.
 *
 * <ul>
 *     <li>Each client sends a packet with the set of custom ingredients it supports.</li>
 *     <li>We store that set inside the {@link PacketContext} using {@link CustomIngredientSync#SUPPORTED_CUSTOM_INGREDIENTS}.</li>
 *     <li>When serializing a custom ingredient, we get access to {@link PacketContext},
 *     and based on that we decide whether to send the custom ingredient, or a vanilla ingredient with the matching stacks.</li>
 * </ul>
 */
public class CustomIngredientSync implements ModInitializer {
	public static final Identifier PACKET_ID = Identifier.fromNamespaceAndPath("fabric", "custom_ingredient_sync");
	public static final int PROTOCOL_VERSION_1 = 1;
	public static final PacketContext.Key<Set<Identifier>> SUPPORTED_CUSTOM_INGREDIENTS = PacketContext.key(Identifier.fromNamespaceAndPath("fabric", "supported_custom_ingredients"));

	public static ServerboundCustomIngredientPayload createResponsePayload(int serverProtocolVersion) {
		if (serverProtocolVersion < PROTOCOL_VERSION_1) {
			// Not supposed to happen - notify the server that we didn't understand the query.
			return null;
		}

		// Always send protocol 1 - the server should support it even if it supports more recent protocols.
		return new ServerboundCustomIngredientPayload(PROTOCOL_VERSION_1, CustomIngredientImpl.REGISTERED_SERIALIZERS.keySet());
	}

	public static Set<Identifier> decodeResponsePayload(ServerboundCustomIngredientPayload payload) {
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
		PayloadTypeRegistry.serverboundConfiguration()
				.register(ServerboundCustomIngredientPayload.TYPE, ServerboundCustomIngredientPayload.CODEC);
		PayloadTypeRegistry.clientboundConfiguration()
				.register(ClientboundCustomIngredientPayload.TYPE, ClientboundCustomIngredientPayload.CODEC);

		ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
			if (ServerConfigurationNetworking.canSend(handler, PACKET_ID)) {
				handler.addTask(new IngredientSyncTask());
			}
		});

		ServerConfigurationNetworking.registerGlobalReceiver(ServerboundCustomIngredientPayload.TYPE, (payload, context) -> {
			Set<Identifier> supportedCustomIngredients = decodeResponsePayload(payload);
			context.packetListener().getPacketContext().set(SUPPORTED_CUSTOM_INGREDIENTS, supportedCustomIngredients);
			context.packetListener().completeTask(IngredientSyncTask.KEY);
		});
	}

	private record IngredientSyncTask() implements ConfigurationTask {
		public static final Type KEY = new Type(PACKET_ID.toString());

		@Override
		public void start(Consumer<Packet<?>> sender) {
			// Send packet with 1 so the client can send us back the list of supported tags.
			// 1 is sent in case we need a different protocol later for some reason.
			sender.accept(ServerConfigurationNetworking.createClientboundPacket(new ClientboundCustomIngredientPayload(PROTOCOL_VERSION_1)));
		}

		@Override
		public Type type() {
			return KEY;
		}
	}
}
