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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.MixinEnvironment;

import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.ReconfiguringScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.InputUtil;

import net.fabricmc.fabric.api.client.gametest.v1.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.impl.client.gametest.TestDedicatedServer;
import net.fabricmc.fabric.impl.client.gametest.ThreadingImpl;
import net.fabricmc.fabric.test.client.gametest.mixin.TitleScreenAccessor;
import net.fabricmc.loader.api.FabricLoader;

public class ClientGameTestTest implements FabricClientGameTest {
	public void runTest(ClientGameTestContext context) {
		{
			waitForTitleScreenFade(context);
			context.takeScreenshot("title_screen", 0);
			context.clickScreenButton("menu.singleplayer");
		}

		if (!isDirEmpty(FabricLoader.getInstance().getGameDir().resolve("saves"))) {
			context.waitForScreen(SelectWorldScreen.class);
			context.takeScreenshot("select_world_screen");
			context.clickScreenButton("selectWorld.create");
		}

		{
			context.waitForScreen(CreateWorldScreen.class);
			context.clickScreenButton("selectWorld.gameMode");
			context.clickScreenButton("selectWorld.gameMode");
			context.takeScreenshot("create_world_screen");
			context.clickScreenButton("selectWorld.create");
		}

		{
			// API test mods use experimental features
			context.waitForScreen(ConfirmScreen.class);
			context.clickScreenButton("gui.yes");
		}

		{
			enableDebugHud(context);
			waitForWorldTicks(context, 200);
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

		{
			context.setScreen(() -> new GameMenuScreen(true));
			context.takeScreenshot("game_menu");
			context.clickScreenButton("menu.returnToMenu");
			context.waitForScreen(TitleScreen.class);
			waitForServerStop(context);
		}

		try (var server = new TestDedicatedServer()) {
			connectToServer(context, server);
			waitForWorldTicks(context, 5);

			final GameProfile profile = context.computeOnClient(MinecraftClient::getGameProfile);
			server.runCommand("op " + profile.getName());
			server.runCommand("gamemode creative " + profile.getName());

			waitForWorldTicks(context, 20);
			context.takeScreenshot("server_in_game", 0);

			{ // Test that we can enter and exit configuration
				server.runCommand("debugconfig config " + profile.getName());
				context.waitForScreen(ReconfiguringScreen.class);
				context.takeScreenshot("server_config");
				server.runCommand("debugconfig unconfig " + profile.getId());
				waitForWorldTicks(context, 1);
			}

			context.setScreen(() -> new GameMenuScreen(true));
			context.takeScreenshot("server_game_menu");
			context.clickScreenButton("menu.disconnect");

			context.waitForScreen(MultiplayerScreen.class);
			context.clickScreenButton("gui.back");
		}

		{
			context.waitForScreen(TitleScreen.class);
		}
	}

	private static boolean isDirEmpty(Path path) {
		try (DirectoryStream<Path> directory = Files.newDirectoryStream(path)) {
			return !directory.iterator().hasNext();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
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

	// TODO: replace with world builder
	private static void waitForWorldTicks(ClientGameTestContext context, long ticks) {
		// Wait for the world to be loaded and get the start ticks
		context.waitFor(client -> client.world != null && !(client.currentScreen instanceof LevelLoadingScreen), 30 * SharedConstants.TICKS_PER_MINUTE);
		final long startTicks = context.computeOnClient(client -> client.world.getTime());
		context.waitFor(client -> Objects.requireNonNull(client.world).getTime() > startTicks + ticks, 10 * SharedConstants.TICKS_PER_MINUTE);
	}

	// TODO: replace with function on TestDedicatedServer
	private static void connectToServer(ClientGameTestContext context, TestDedicatedServer server) {
		context.runOnClient(client -> {
			final var serverInfo = new ServerInfo("localhost", server.getConnectionAddress(), ServerInfo.ServerType.OTHER);
			ConnectScreen.connect(client.currentScreen, client, ServerAddress.parse(server.getConnectionAddress()), serverInfo, false, null);
		});
	}

	// TODO: move into close methods of TestDedicatedServer and TestWorld
	private static void waitForServerStop(ClientGameTestContext context) {
		context.waitFor(client -> !ThreadingImpl.isServerRunning, SharedConstants.TICKS_PER_MINUTE);
	}
}
