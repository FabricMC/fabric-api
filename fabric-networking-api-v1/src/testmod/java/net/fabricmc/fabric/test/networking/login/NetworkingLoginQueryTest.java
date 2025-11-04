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

package net.fabricmc.fabric.test.networking.login;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.util.Util;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.LoginPacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.test.networking.NetworkingTestmods;

public final class NetworkingLoginQueryTest implements ModInitializer {
	private static final boolean useLoginDelayTest = System.getProperty("fabric-networking-api-v1.loginDelayTest") != null;

	public static final Identifier GLOBAL_TEST_CHANNEL = NetworkingTestmods.id("global_test_channel");
	public static final Identifier LOCAL_TEST_CHANNEL = NetworkingTestmods.id("local_test_channel");

	@Override
	public void onInitialize() {
		ServerLoginConnectionEvents.QUERY_START.register(this::onLoginStart);
		ServerLoginConnectionEvents.QUERY_START.register(this::delaySimply);

		// login delaying example
		ServerLoginNetworking.registerGlobalReceiver(GLOBAL_TEST_CHANNEL, (server, handler, understood, buf, synchronizer, sender) -> {
			if (understood) {
				NetworkingTestmods.LOGGER.info("Understood response from client in {}", GLOBAL_TEST_CHANNEL);

				if (useLoginDelayTest) {
					FutureTask<?> future = new FutureTask<>(() -> {
						for (int i = 0; i <= 10; i++) {
							Thread.sleep(300);
							NetworkingTestmods.LOGGER.info("Delayed login for number {} 300 milliseconds", i);
						}

						return null;
					});

					// Execute the task on a worker thread as not to block the server thread
					Util.backgroundExecutor().execute(future);
					synchronizer.waitFor(future);
				}
			} else {
				NetworkingTestmods.LOGGER.info("Client did not understand response query message with channel name {}", GLOBAL_TEST_CHANNEL);
			}
		});

		ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
			ServerLoginNetworking.registerReceiver(handler, LOCAL_TEST_CHANNEL, (server1, handler1, understood, buf, synchronizer1, responseSender) -> {
				if (understood) {
					NetworkingTestmods.LOGGER.info("Understood response from client in {}", LOCAL_TEST_CHANNEL);
				} else {
					NetworkingTestmods.LOGGER.info("Client did not understand response query message with channel name {}", LOCAL_TEST_CHANNEL);
				}
			});

			sender.sendPacket(LOCAL_TEST_CHANNEL, PacketByteBufs.create());
		});
	}

	private void delaySimply(ServerLoginPacketListenerImpl handler, MinecraftServer server, PacketSender sender, ServerLoginNetworking.LoginSynchronizer synchronizer) {
		if (useLoginDelayTest) {
			synchronizer.waitFor(CompletableFuture.runAsync(() -> {
				NetworkingTestmods.LOGGER.info("Starting simple delay task for 3000 milliseconds");

				try {
					Thread.sleep(3000);
					NetworkingTestmods.LOGGER.info("Simple delay task completed");
				} catch (InterruptedException e) {
					NetworkingTestmods.LOGGER.error("Delay task caught exception", e);
				}
			}));
		}
	}

	private void onLoginStart(ServerLoginPacketListenerImpl networkHandler, MinecraftServer server, LoginPacketSender sender, ServerLoginNetworking.LoginSynchronizer synchronizer) {
		// Send a dummy query when the client starts accepting queries.
		sender.sendPacket(GLOBAL_TEST_CHANNEL, PacketByteBufs.empty()); // dummy packet
	}
}
