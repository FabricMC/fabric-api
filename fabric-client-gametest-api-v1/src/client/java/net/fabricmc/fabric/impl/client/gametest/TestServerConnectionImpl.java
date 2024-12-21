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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;

import net.fabricmc.fabric.api.client.gametest.v1.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.TestClientWorldContext;
import net.fabricmc.fabric.api.client.gametest.v1.TestServerConnection;

public class TestServerConnectionImpl implements TestServerConnection {
	private final ClientGameTestContext context;
	private final TestClientWorldContext clientWorld;

	public TestServerConnectionImpl(ClientGameTestContext context, TestClientWorldContext clientWorld) {
		this.context = context;
		this.clientWorld = clientWorld;
	}

	@Override
	public TestClientWorldContext getClientWorld() {
		return clientWorld;
	}

	@Override
	public void close() {
		ThreadingImpl.checkOnGametestThread("close");

		context.runOnClient(client -> {
			if (client.world == null) {
				throw new AssertionError("Disconnected from server before closing the test server connection");
			}
		});

		context.runOnClient(MinecraftClient::disconnect);
		context.waitFor(client -> client.world == null);
		context.setScreen(TitleScreen::new);
	}
}
