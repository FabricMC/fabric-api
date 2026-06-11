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

package net.fabricmc.fabric.impl.holder.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.holder.component.v1.FabricDataComponentInitializers;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.impl.holder.component.data.DataHolderComponentInitializer;
import net.fabricmc.fabric.impl.holder.component.sync.ClientboundUpdateComponentsPayload;

public class HolderComponentEntrypoint implements ModInitializer {
	// TODO: This size is enormous, I copied it straight out of FabricRegistryInit. This should be smaller but I don't know how to choose a good value.
	private static final int MAX_PACKET_SIZE = Integer.getInteger("fabric.holder.component.sync.max_packet_size", 128 * 1024 * 1024);

	public static final String MOD_ID = "fabric-holder-component-api-v1";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		FabricDataComponentInitializers.registerInitializer(
				FabricDataComponentInitializers.DATA_HOLDER_COMPONENTS,
				new DataHolderComponentInitializer()
		);

		PayloadTypeRegistry.clientboundPlay().registerLarge(
				ClientboundUpdateComponentsPayload.TYPE,
				ClientboundUpdateComponentsPayload.STREAM_CODEC,
				MAX_PACKET_SIZE
		);

		PayloadTypeRegistry.clientboundConfiguration().registerLarge(
				ClientboundUpdateComponentsPayload.TYPE,
				ClientboundUpdateComponentsPayload.STREAM_CODEC,
				MAX_PACKET_SIZE
		);
	}
}
