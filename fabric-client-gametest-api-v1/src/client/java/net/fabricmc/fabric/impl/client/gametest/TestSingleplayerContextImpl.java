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

import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.server.MinecraftServer;

import net.fabricmc.fabric.api.client.gametest.v1.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.TestClientWorldContext;
import net.fabricmc.fabric.api.client.gametest.v1.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.TestWorldSave;

public class TestSingleplayerContextImpl implements TestSingleplayerContext {
	private final ClientGameTestContext context;
	private final TestWorldSave worldSave;
	private final TestClientWorldContext clientWorld;
	private final TestServerContext server;

	public TestSingleplayerContextImpl(ClientGameTestContext context, TestWorldSave worldSave, MinecraftServer server) {
		this.context = context;
		this.worldSave = worldSave;
		this.clientWorld = new TestClientWorldContextImpl(context);
		this.server = new TestServerContextImpl(server);
	}

	@Override
	public TestWorldSave getWorldSave() {
		return worldSave;
	}

	@Override
	public TestClientWorldContext getClientWorld() {
		return clientWorld;
	}

	@Override
	public TestServerContext getServer() {
		return server;
	}

	@Override
	public void close() {
		ThreadingImpl.checkOnGametestThread("close");

		context.runOnClient(client -> {
			if (client.world == null) {
				throw new IllegalStateException("Exited the world before closing singleplayer context");
			}
		});

		context.setScreen(() -> new GameMenuScreen(true));
		context.clickScreenButton("menu.returnToMenu");
		context.waitForScreen(TitleScreen.class);
		context.waitFor(client -> !ThreadingImpl.isServerRunning && client.world == null, SharedConstants.TICKS_PER_MINUTE);
	}
}
