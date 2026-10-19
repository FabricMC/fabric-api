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

package net.fabricmc.fabric.impl.modprotocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.impl.modprotocol.payload.ClientboundModProtocolRequestPayload;
import net.fabricmc.fabric.impl.modprotocol.payload.ServerboundModProtocolResponsePayload;

public final class ModProtocolInit implements ModInitializer {
	public static final String MOD_ID = "fabric-mod-protocol-api-v1";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static boolean frozen = false;

	public void onInitialize() {
		Identifier phase = Identifier.fromNamespaceAndPath("fabric", "mod_protocol");
		Identifier registrySync = Identifier.fromNamespaceAndPath("fabric", "registry_sync");

		ServerConfigurationConnectionEvents.BEFORE_CONFIGURE.addPhaseOrdering(phase, Event.DEFAULT_PHASE);
		ServerConfigurationConnectionEvents.BEFORE_CONFIGURE.addPhaseOrdering(phase, registrySync);
		ServerConfigurationConnectionEvents.BEFORE_CONFIGURE.register(phase, ModProtocolManager::setupClient);

		PayloadTypeRegistry.serverboundConfiguration().register(ServerboundModProtocolResponsePayload.TYPE, ServerboundModProtocolResponsePayload.PACKET_CODEC);
		PayloadTypeRegistry.clientboundConfiguration().register(ClientboundModProtocolRequestPayload.TYPE, ClientboundModProtocolRequestPayload.PACKET_CODEC);
		ServerConfigurationNetworking.registerGlobalReceiver(ServerboundModProtocolResponsePayload.TYPE, (payload, context) -> {
			context.packetContext().set(ModProtocolManager.REMOTE_MOD_VERSIONS_KEY, payload.supported());
			context.packetListener().completeTask(ModProtocolManager.SyncConfigurationTask.TYPE);
		});

		ModProtocolManager.collectModProtocols();
	}
}
