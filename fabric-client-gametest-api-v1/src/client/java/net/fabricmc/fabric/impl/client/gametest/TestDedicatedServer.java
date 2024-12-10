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

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.server.Main;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;

public class TestDedicatedServer implements Closeable {
	public static final AtomicReference<MinecraftDedicatedServer> DEDICATED_SERVER_REF = new AtomicReference<>();
	private static final Duration START_TIMEOUT = Duration.ofMinutes(5);

	final ExecutorService executor = Executors.newSingleThreadExecutor();
	MinecraftDedicatedServer server;

	public TestDedicatedServer() {
		assert DEDICATED_SERVER_REF.get() == null : "A dedicated server is already running";
		executor.execute(this::run);
		waitUntilReady();
		Objects.requireNonNull(server);
	}

	public String getConnectionAddress() {
		return "localhost:" + server.getServerPort();
	}

	public void runCommand(String command) {
		ThreadingImpl.runOnServer(() -> server.getCommandManager().executeWithPrefix(server.getCommandSource(), command));
	}

	private void run() {
		setupServer();
		Main.main(new String[]{});
	}

	private void setupServer() {
		try {
			Files.writeString(Paths.get("eula.txt"), "eula=true");
			Files.writeString(Paths.get("server.properties"), "online-mode=false");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void waitUntilReady() {
		long startTime = System.currentTimeMillis();

		while (DEDICATED_SERVER_REF.get() == null) {
			if (System.currentTimeMillis() - startTime > START_TIMEOUT.toMillis()) {
				throw new RuntimeException("Timeout while waiting for the server to start");
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		server = DEDICATED_SERVER_REF.get();
		DEDICATED_SERVER_REF.set(null);
	}

	@Override
	public void close() {
		server.stop(false);

		while (server.getThread().isAlive()) {
			ThreadingImpl.runTick();
		}

		executor.close();
	}
}
