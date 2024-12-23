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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.server.Main;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.util.Util;

public final class DedicatedServerImplUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-client-gametest-api-v1");
	private static final Properties DEFAULT_SERVER_PROPERTIES = Util.make(new Properties(), properties -> {
		// allow non-authenticated connections from localhost
		properties.setProperty("online-mode", "false");

		// disable sync-chunk-writes on unix systems, it slows world saving down a LOT and doesn't really help anything
		properties.setProperty("sync-chunk-writes", String.valueOf(Util.getOperatingSystem() == Util.OperatingSystem.WINDOWS));

		// allow non-opped players to place blocks at spawn
		properties.setProperty("spawn-protection", "0");

		// stops other players from joining the server and interfering with the tests
		properties.setProperty("max-players", "1");
	});

	// If this field is set, it causes the create world screen to write the level.dat file to the specified folder
	@Nullable
	public static Path saveLevelDataTo = null;
	@Nullable
	public static CompletableFuture<MinecraftDedicatedServer> serverFuture = null;

	private DedicatedServerImplUtil() {
	}

	public static MinecraftDedicatedServer start(Properties serverProperties) {
		setupServer(serverProperties);
		serverFuture = new CompletableFuture<>();

		new Thread(() -> Main.main(new String[0])).start();

		try {
			return serverFuture.get(10, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw new RuntimeException(e);
		} finally {
			serverFuture = null;
		}
	}

	private static void setupServer(Properties customServerProperties) {
		Properties serverProperties = new Properties();
		serverProperties.putAll(DEFAULT_SERVER_PROPERTIES);
		serverProperties.putAll(customServerProperties);

		try {
			try (BufferedWriter writer = Files.newBufferedWriter(Path.of("server.properties"))) {
				serverProperties.store(writer, null);
			}
		} catch (IOException e) {
			LOGGER.error("Failed to write server properties", e);
		}
	}
}
