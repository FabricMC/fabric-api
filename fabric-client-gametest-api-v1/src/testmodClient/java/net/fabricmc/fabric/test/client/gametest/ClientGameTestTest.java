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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.MixinEnvironment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.WindowFramebuffer;
import net.minecraft.client.gui.screen.ReconfiguringScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.InputUtil;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonOptions;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.fabricmc.fabric.test.client.gametest.mixin.TitleScreenAccessor;

public class ClientGameTestTest implements FabricClientGameTest {
	public void runTest(ClientGameTestContext context) {
		{
			waitForTitleScreenFade(context);
			context.takeScreenshot("title_screen");
			context.assertScreenshotContains("sound_button");
			context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("sound_button")
					.withGrayscale()
					.withRegion(430, 312, 144, 40));
			assertThrows(() -> context.assertScreenshotContains("doesnt_exist"));
		}

		{
			testScreenSize(context, WindowFramebuffer.DEFAULT_WIDTH, WindowFramebuffer.DEFAULT_HEIGHT);
			context.getInput().resizeWindow(1000, 500);
			context.waitTick();
			testScreenSize(context, 1000, 500);
			context.getInput().resizeWindow(WindowFramebuffer.DEFAULT_WIDTH, WindowFramebuffer.DEFAULT_HEIGHT);
		}

		TestWorldSave spWorldSave;
		try (TestSingleplayerContext singleplayer = context.worldBuilder()
				.adjustSettings(creator -> creator.setGameMode(WorldCreator.Mode.CREATIVE)).create()) {
			spWorldSave = singleplayer.getWorldSave();

			{
				enableDebugHud(context);
				singleplayer.getClientWorld().waitForChunksRender();
				context.takeScreenshot("in_game_overworld");
			}

			{
				context.getInput().pressKey(options -> options.chatKey);
				context.getInput().typeChars("Hello, World!");
				context.getInput().holdKeyFor(InputUtil.GLFW_KEY_ENTER, 0); // press without delay, enter not a keybind
				context.waitTick(); // wait for the server to receive the chat message
				context.takeScreenshot("chat_message_sent");
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
				context.waitTick(); // wait for the server to receive the request
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
				context.takeScreenshot("server_in_game");

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

	private static void testScreenSize(ClientGameTestContext context, int expectedWidth, int expectedHeight) {
		context.runOnClient(client -> {
			if (client.getWindow().getWidth() != expectedWidth || client.getWindow().getHeight() != expectedHeight) {
				throw new AssertionError("Expected window size to be (%d, %d) but was (%d, %d)".formatted(expectedWidth, expectedHeight, client.getWindow().getWidth(), client.getWindow().getHeight()));
			}

			if (client.getWindow().getFramebufferWidth() != expectedWidth || client.getWindow().getFramebufferHeight() != expectedHeight) {
				throw new AssertionError("Expected framebuffer size to be (%d, %d) but was (%d, %d)".formatted(expectedWidth, expectedHeight, client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight()));
			}
		});

		Path screenshotPath = context.takeScreenshot("screenshot_size_test");

		try (NativeImage screenshot = NativeImage.read(Files.newInputStream(screenshotPath))) {
			if (screenshot.getWidth() != expectedWidth || screenshot.getHeight() != expectedHeight) {
				throw new AssertionError("Expected screenshot size to be (%d, %d) but was (%d, %d)".formatted(expectedWidth, expectedHeight, screenshot.getWidth(), screenshot.getHeight()));
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static void assertThrows(Runnable runnable) {
		try {
			runnable.run();
		} catch (Throwable t) {
			return;
		}

		throw new AssertionError("Expected exception to be thrown");
	}
}
