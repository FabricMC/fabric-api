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

package net.fabricmc.fabric.impl.client.gametest;

import net.minecraft.client.Minecraft;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl;
import net.fabricmc.fabric.impl.client.gametest.util.GameTestSyncPayload;

public class FabricClientGameTestImpl implements ClientModInitializer {
	public static final String MOD_ID = "fabric-client-gametest-api-v1";

	@Override
	public void onInitializeClient() {
		ThreadingImpl.unsafeClientInstance = Minecraft.getInstance();

		PayloadTypeRegistry.serverboundPlay().register(GameTestSyncPayload.TYPE, GameTestSyncPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(GameTestSyncPayload.TYPE, GameTestSyncPayload.CODEC);
		ClientPlayNetworking.registerGlobalReceiver(GameTestSyncPayload.TYPE, (_, _) -> ThreadingImpl.networkSyncReceived = true);
		ServerPlayNetworking.registerGlobalReceiver(GameTestSyncPayload.TYPE, (_, _) -> ThreadingImpl.networkSyncReceived = true);
	}
}
