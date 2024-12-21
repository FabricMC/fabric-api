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

package net.fabricmc.fabric.test.client.gametest;

import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.MixinEnvironment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ReconfiguringScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.InputUtil;

import net.fabricmc.fabric.api.client.gametest.v1.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.TestServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.TestWorldSave;
import net.fabricmc.fabric.test.client.gametest.mixin.TitleScreenAccessor;

public class ClientGameTestTest implements FabricClientGameTest {
	public void runTest(ClientGameTestContext context) {
		{
			waitForTitleScreenFade(context);
			context.takeScreenshot("title_screen", 0);
		}

		TestWorldSave spWorldSave;
		try (TestSingleplayerContext singleplayer = context.worldBuilder()
				.adjustSettings(creator -> creator.setGameMode(WorldCreator.Mode.CREATIVE)).create()) {
			spWorldSave = singleplayer.getWorldSave();

			{
				enableDebugHud(context);
				singleplayer.getClientWorld().waitForChunksRender();
				context.takeScreenshot("in_game_overworld", 0);
			}

			{
				context.getInput().pressKey(options -> options.chatKey);
				context.waitTick();
				context.getInput().typeChars("Hello, World!");
				context.getInput().pressKey(InputUtil.GLFW_KEY_ENTER);
				context.takeScreenshot("chat_message_sent", 5);
			}

			MixinEnvironment.getCurrentEnvironment().audit();

			{
				// See if the player render events are working.
				setPerspective(context, Perspective.THIRD_PERSON_BACK);
				context.takeScreenshot("in_game_overworld_third_person");
				setPerspective(context, Perspective.FIRST_PERSON);
			}

			{
				context.getInput().pressKey(options -> options.inventoryKey);
				context.takeScreenshot("in_game_inventory");
				context.setScreen(() -> null);
			}
		}

		try (TestSingleplayerContext singleplayer = spWorldSave.open()) {
			singleplayer.getClientWorld().waitForChunksRender();
			context.takeScreenshot("in_game_overworld_2");
		}

		try (TestDedicatedServerContext server = context.worldBuilder().createServer()) {
			try (TestServerConnection connection = server.connect()) {
				connection.getClientWorld().waitForChunksRender();
				context.takeScreenshot("server_in_game", 0);

				{ // Test that we can enter and exit configuration
					final GameProfile profile = context.computeOnClient(MinecraftClient::getGameProfile);
					server.runCommand("debugconfig config " + profile.getName());
					context.waitForScreen(ReconfiguringScreen.class);
					context.takeScreenshot("server_config");
					server.runCommand("debugconfig unconfig " + profile.getId());
					// TODO: better way to wait for reconfiguration to end
					context.waitTicks(100);
				}
			}
		}
	}

	private static void waitForTitleScreenFade(ClientGameTestContext context) {
		context.waitFor(client -> {
			return !(client.currentScreen instanceof TitleScreenAccessor titleScreen) || !titleScreen.getDoBackgroundFade();
		});
	}

	private static void enableDebugHud(ClientGameTestContext context) {
		context.runOnClient(client -> client.inGameHud.getDebugHud().toggleDebugHud());
	}

	private static void setPerspective(ClientGameTestContext context, Perspective perspective) {
		context.runOnClient(client -> client.options.setPerspective(perspective));
	}
}
