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

package net.fabricmc.fabric.impl.client.holder.component;

import java.util.List;

import net.minecraft.client.multiplayer.RegistryDataCollector;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.network.chat.Component;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.impl.holder.component.sync.ClientboundUpdateComponentsPayload;
import net.fabricmc.fabric.impl.holder.component.sync.HolderComponentSynchronization;
import net.fabricmc.fabric.mixin.client.holder.component.ClientConfigurationPacketListenerImplAccessor;

public class HolderComponentClientEntrypoint implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientConfigurationNetworking.registerGlobalReceiver(
				ClientboundUpdateComponentsPayload.TYPE,
				(payload, context) -> {
					RegistryDataCollector collector = ((ClientConfigurationPacketListenerImplAccessor) context.packetListener()).getRegistryDataCollector();
					FabricRegistryDataCollector fabricCollector = ((FabricRegistryDataCollector) collector);

					fabricCollector.fabric$appendComponents(payload);
				}
		);

		ClientPlayNetworking.registerGlobalReceiver(
				ClientboundUpdateComponentsPayload.TYPE,
				(payload, context) -> {
					List<DataComponentInitializers.PendingComponents<?>> pending = HolderComponentSynchronization.deserialize(
							payload,
							context.packetContext().orElseThrow(PacketContext.REGISTRY_ACCESS)
					);

					// we already check for integrated server when sending, but anything could send a packet and applying it will cause serious issues.
					if (context.packetContext().orElseThrow(PacketContext.CONNECTION).isMemoryConnection()) {
						context.responseSender().disconnect(Component.literal("A ClientboundUpdateComponentsPayload was sent to the integrated server host. This is a bug, whichever mod sent this packet should check if the receiver is the server host before sending it."));
						return;
					}

					pending.forEach(DataComponentInitializers.PendingComponents::apply);
				}
		);
	}
}
