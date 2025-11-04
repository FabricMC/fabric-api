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

package net.fabricmc.fabric.impl.client.gametest.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.contents.TranslatableContents;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

public final class ClientGameTestImpl {
	public static final Logger LOGGER = LoggerFactory.getLogger("fabric-client-gametest-api-v1");

	private ClientGameTestImpl() {
	}

	public static void waitForWorldLoad(ClientGameTestContext context) {
		for (int i = 0; i < SharedConstants.TICKS_PER_MINUTE; i++) {
			if (context.computeOnClient(client -> isExperimentalWarningScreen(client.screen))) {
				context.clickScreenButton("gui.yes");
			}

			if (context.computeOnClient(client -> client.screen instanceof BackupConfirmScreen)) {
				context.clickScreenButton("selectWorld.backupJoinSkipButton");
			}

			if (context.computeOnClient(ClientGameTestImpl::isWorldLoadingFinished)) {
				return;
			}

			context.waitTick();
		}

		if (!context.computeOnClient(ClientGameTestImpl::isWorldLoadingFinished)) {
			throw new AssertionError("Timeout loading world");
		}
	}

	private static boolean isExperimentalWarningScreen(Screen screen) {
		if (!(screen instanceof ConfirmScreen)) {
			return false;
		}

		if (!(screen.getTitle().getContents() instanceof TranslatableContents translatableContents)) {
			return false;
		}

		return "selectWorld.warning.experimental.title".equals(translatableContents.getKey());
	}

	private static boolean isWorldLoadingFinished(Minecraft client) {
		LOGGER.info("World loading finished: {} screen: {}", client.level, client.screen);
		return client.level != null && !(client.screen instanceof LevelLoadingScreen);
	}
}
