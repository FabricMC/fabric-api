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

package net.fabricmc.fabric.impl.client.gametest.context;

import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.server.dedicated.DedicatedServer;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestClientWorldContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;
import net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl;
import net.fabricmc.fabric.impl.client.gametest.util.ClientGameTestImpl;

public class TestDedicatedServerContextImpl extends TestServerContextImpl implements TestDedicatedServerContext {
	private final ClientGameTestContext context;

	public TestDedicatedServerContextImpl(ClientGameTestContext context, DedicatedServer server) {
		super(server);
		this.context = context;
	}

	@Override
	public TestServerConnection connect() {
		ThreadingImpl.checkOnGametestThread("connect");

		context.runOnClient(client -> {
			final var serverInfo = new ServerData("localhost", getConnectionAddress(), ServerData.Type.OTHER);
			ConnectScreen.startConnecting(client.screen, client, ServerAddress.parseString(getConnectionAddress()), serverInfo, false, null);
		});

		ClientGameTestImpl.waitForWorldLoad(context);

		TestClientWorldContext clientWorld = new TestClientWorldContextImpl(context);
		return new TestServerConnectionImpl(context, clientWorld);
	}

	private String getConnectionAddress() {
		return "localhost:" + server.getPort();
	}

	@Override
	public void close() {
		ThreadingImpl.checkOnGametestThread("close");

		if (!ThreadingImpl.isServerRunning || !server.getRunningThread().isAlive()) {
			throw new AssertionError("Stopped the dedicated server before closing the dedicated server context");
		}

		server.halt(false);
		context.waitFor(client -> !ThreadingImpl.isServerRunning && !server.getRunningThread().isAlive());
	}
}
