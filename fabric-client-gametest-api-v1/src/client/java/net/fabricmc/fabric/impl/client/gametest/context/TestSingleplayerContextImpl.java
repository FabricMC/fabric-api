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

import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestClientWorldContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl;

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
			if (client.level == null) {
				throw new IllegalStateException("Exited the world before closing singleplayer context");
			}

			client.level.disconnect(Component.translatable("menu.savingLevel"));
			client.disconnect(new GenericMessageScreen(Component.translatable("menu.savingLevel")), false);
		});

		context.waitFor(client -> !ThreadingImpl.isServerRunning && client.level == null, SharedConstants.TICKS_PER_MINUTE);
		context.setScreen(TitleScreen::new);
	}
}
