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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.BackupPromptScreen;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.text.TranslatableTextContent;

import net.fabricmc.fabric.api.client.gametest.v1.ClientGameTestContext;

public final class ClientGameTestImpl {
	public static final Logger LOGGER = LoggerFactory.getLogger("fabric-client-gametest-api-v1");

	private ClientGameTestImpl() {
	}

	public static void waitForWorldLoad(ClientGameTestContext context) {
		for (int i = 0; i < SharedConstants.TICKS_PER_MINUTE; i++) {
			if (context.computeOnClient(client -> isExperimentalWarningScreen(client.currentScreen))) {
				context.clickScreenButton("gui.yes");
			}

			if (context.computeOnClient(client -> client.currentScreen instanceof BackupPromptScreen)) {
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

		if (!(screen.getTitle().getContent() instanceof TranslatableTextContent translatableContents)) {
			return false;
		}

		return "selectWorld.warning.experimental.title".equals(translatableContents.getKey());
	}

	private static boolean isWorldLoadingFinished(MinecraftClient client) {
		return client.world != null && !(client.currentScreen instanceof LevelLoadingScreen);
	}
}
