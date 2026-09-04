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

package net.fabricmc.fabric.test.registry.sync.client;

import java.util.Objects;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.fabricmc.fabric.impl.client.registry.sync.validate.DetailedBackupConfirmScreen;
import net.fabricmc.fabric.impl.client.registry.sync.validate.DetailsScreen;

public class RegistryEntryWorldValidationClientTest implements FabricClientGameTest {
	public void runTest(ClientGameTestContext context) {
		TestWorldSave spWorldSave;
		try (TestSingleplayerContext singleplayer = context.worldBuilder()
				.adjustSettings(creator -> creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			spWorldSave = singleplayer.getWorldSave();
			singleplayer.getConnection().waitForChunksRender();
			singleplayer.getServer().runCommand("override_registry_entries");
			context.waitTicks(5);
		}

		context.waitTicks(20);

		context.runOnClient(client -> {
			Screen screen = Objects.requireNonNull(client.gui.screen());
			client.createWorldOpenFlows().openWorld(spWorldSave.getSaveDirectory().getFileName().toString(), () -> {
				client.setScreenAndShow(screen);
			});
		});

		context.waitForScreen(DetailedBackupConfirmScreen.class);
		context.takeScreenshot("registry_entry_validation_screen");
		context.clickScreenButton("selectWorld.experimental.details");

		context.waitForScreen(DetailsScreen.class);
		context.takeScreenshot("registry_entry_validation_details_screen");
		context.clickScreenButton("gui.back");

		context.clickScreenButton("gui.cancel");
	}
}
