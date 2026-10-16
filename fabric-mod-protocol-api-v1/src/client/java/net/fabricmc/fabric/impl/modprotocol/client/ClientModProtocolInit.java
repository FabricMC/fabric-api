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

package net.fabricmc.fabric.impl.modprotocol.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.resources.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.impl.modprotocol.ModProtocolInit;
import net.fabricmc.fabric.impl.modprotocol.ModProtocolManager;
import net.fabricmc.fabric.impl.modprotocol.RemoteModProtocol;
import net.fabricmc.fabric.impl.modprotocol.payload.ClientboundModProtocolRequestPayload;
import net.fabricmc.fabric.impl.modprotocol.payload.ServerboundModProtocolResponsePayload;

public final class ClientModProtocolInit implements ClientModInitializer {
	public static final boolean FORCE_ALWAYS_COMPATIBLE_CLIENT = System.getProperty("fabric-mod-protocol-v1.forceCompatibleClient") != null;

	public static final PacketContext.Key<ModProtocolManager.ValidationResult> VALIDATION_RESULT_KEY = PacketContext.key(Identifier.fromNamespaceAndPath("fabric-mod-protocol-v1", "validation_result"));
	public static final ScopedValue<ModProtocolManager.ValidationResult> VALIDATION_RESULT_SCOPED_VALUE = ScopedValue.newInstance();

	public void onInitializeClient() {
		ClientConfigurationNetworking.registerGlobalReceiver(ClientboundModProtocolRequestPayload.TYPE, (payload, context) -> {
			if (FORCE_ALWAYS_COMPATIBLE_CLIENT) {
				var versions = new HashMap<Identifier, String>(payload.entries().size());

				for (RemoteModProtocol protocol : payload.entries()) {
					versions.put(protocol.id(), protocol.version());
				}

				context.packetContext().set(ModProtocolManager.REMOTE_MOD_VERSIONS_KEY, versions);
				context.responseSender().sendPacket(new ServerboundModProtocolResponsePayload(versions, List.of()));
				return;
			}

			ModProtocolManager.ValidationResult validate = ModProtocolManager.validateClient(payload.entries());

			if (validate.isSuccess()) {
				context.packetContext().set(ModProtocolManager.REMOTE_MOD_VERSIONS_KEY, validate.supportedProtocols());
				context.packetContext().set(VALIDATION_RESULT_KEY, null);
				context.responseSender().sendPacket(new ServerboundModProtocolResponsePayload(validate.supportedProtocols(), List.of()));
				return;
			}

			if (payload.disconnect()) {
				var b = new StringBuilder();
				b.append("Disconnected due to mismatched protocols!").append('\n');
				b.append("Missing entries:").append('\n');
				ModProtocolManager.appendComponentEntries(validate.missing(), ModProtocolManager.LOCAL_MOD_PROTOCOLS_BY_ID, -1, text -> b.append(" - ").append(text.getString()), false);

				context.responseSender().disconnect(ModProtocolManager.getMismatchedVersionsMessage(validate.missing(), ModProtocolManager.LOCAL_MOD_PROTOCOLS_BY_ID));
				context.packetContext().set(VALIDATION_RESULT_KEY, validate);
				ModProtocolInit.LOGGER.warn(b.toString());
			} else {
				List<Identifier> missingServer = new ArrayList<>();

				for (RemoteModProtocol remoteModProtocol : validate.missingRemote()) {
					Identifier id = remoteModProtocol.id();
					missingServer.add(id);
				}

				context.responseSender().sendPacket(new ServerboundModProtocolResponsePayload(validate.supportedProtocols(), missingServer));
				context.packetContext().set(VALIDATION_RESULT_KEY, validate);
			}
		});
	}
}
