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

import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;

import net.fabricmc.fabric.api.client.gametest.v1.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.TestClientWorldContext;
import net.fabricmc.fabric.api.client.gametest.v1.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.TestServerConnection;

public class TestDedicatedServerContextImpl extends TestServerContextImpl implements TestDedicatedServerContext {
	private final ClientGameTestContext context;

	public TestDedicatedServerContextImpl(ClientGameTestContext context, MinecraftDedicatedServer server) {
		super(server);
		this.context = context;
	}

	@Override
	public TestServerConnection connect() {
		ThreadingImpl.checkOnGametestThread("connect");

		context.runOnClient(client -> {
			final var serverInfo = new ServerInfo("localhost", getConnectionAddress(), ServerInfo.ServerType.OTHER);
			ConnectScreen.connect(client.currentScreen, client, ServerAddress.parse(getConnectionAddress()), serverInfo, false, null);
		});

		ClientGameTestImpl.waitForWorldLoad(context);

		TestClientWorldContext clientWorld = new TestClientWorldContextImpl(context);
		return new TestServerConnectionImpl(context, clientWorld);
	}

	private String getConnectionAddress() {
		return "localhost:" + server.getServerPort();
	}

	@Override
	public void close() {
		ThreadingImpl.checkOnGametestThread("close");

		if (!ThreadingImpl.isServerRunning || !server.getThread().isAlive()) {
			throw new AssertionError("Stopped the dedicated server before closing the dedicated server context");
		}

		server.stop(false);
		context.waitFor(client -> !ThreadingImpl.isServerRunning && !server.getThread().isAlive());
	}
}
