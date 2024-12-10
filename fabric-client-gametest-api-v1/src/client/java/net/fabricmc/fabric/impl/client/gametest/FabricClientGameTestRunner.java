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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.gui.screen.TitleScreen;

import net.fabricmc.fabric.api.client.gametest.v1.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.loader.api.FabricLoader;

public class FabricClientGameTestRunner {
	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-client-gametest-api-v1");
	private static final String ENTRYPOINT_KEY = "fabric-client-gametest";

	public static void start() {
		List<FabricClientGameTest> gameTests = FabricLoader.getInstance().getEntrypoints(ENTRYPOINT_KEY, FabricClientGameTest.class);
		ThreadingImpl.runTestThread(() -> {
			ClientGameTestContext context = new ClientGameTestContextImpl();
			boolean failed = false;

			for (FabricClientGameTest gameTest : gameTests) {
				try {
					gameTest.runTest(context);
				} catch (Throwable e) {
					LOGGER.error("Failed test {}", gameTest.getClass().getName(), e);
					failed = true;
				} finally {
					// Open the title screen to reset the state for the next gametest.
					// If the gametest API was used correctly, we should be in the menus somewhere because any test
					// world should have been closed at the end of a try-with-resources statement.
					context.setScreen(new TitleScreen());
				}
			}

			if (failed) {
				throw new AssertionError("There were failing client gametests");
			}

			context.clickScreenButton("menu.quit");
		});
	}
}
