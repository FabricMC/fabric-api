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

package net.fabricmc.fabric.test.base.client;

import static net.fabricmc.fabric.test.base.client.FabricClientTestHelper.clickScreenButton;
import static net.fabricmc.fabric.test.base.client.FabricClientTestHelper.closeScreen;
import static net.fabricmc.fabric.test.base.client.FabricClientTestHelper.computeOnClient;
import static net.fabricmc.fabric.test.base.client.FabricClientTestHelper.connectToServer;
import static net.fabricmc.fabric.test.base.client.FabricClientTestHelper.enableDebugHud;
import static net.fabricmc.fabric.test.base.client.FabricClientTestHelper.openGameMenu;
import static net.fabricmc.fabric.test.base.client.FabricClientTestHelper.openInventory;
import static net.fabricmc.fabric.test.base.client.FabricClientTestHelper.setPerspective;
import static net.fabricmc.fabric.test.base.client.FabricClientTestHelper.takeScreenshot;
import static net.fabricmc.fabric.test.base.client.FabricClientTestHelper.waitForLoadingComplete;
import static net.fabricmc.fabric.test.base.client.FabricClientTestHelper.waitForScreen;
import static net.fabricmc.fabric.test.base.client.FabricClientTestHelper.waitForServerStop;
import static net.fabricmc.fabric.test.base.client.FabricClientTestHelper.waitForTitleScreenFade;
import static net.fabricmc.fabric.test.base.client.FabricClientTestHelper.waitForWorldTicks;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.MixinEnvironment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.ReconfiguringScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.option.Perspective;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class FabricApiAutoTestClient implements ClientModInitializer {
	public static final boolean IS_AUTO_TEST = System.getProperty("fabric.autoTest") != null;

	@Override
	public void onInitializeClient() {
		if (!IS_AUTO_TEST) {
			return;
		}

		ThreadingImpl.runTestThread(this::runTest);
	}

	private void runTest() {
		waitForLoadingComplete();

		final boolean onboardAccessibility = computeOnClient(client -> client.options.onboardAccessibility);

		if (onboardAccessibility) {
			waitForScreen(AccessibilityOnboardingScreen.class);
			takeScreenshot("onboarding_screen");
			clickScreenButton("gui.continue");
		}

		{
			waitForScreen(TitleScreen.class);
			waitForTitleScreenFade();
			takeScreenshot("title_screen", 0);
			clickScreenButton("menu.singleplayer");
		}

		if (!isDirEmpty(FabricLoader.getInstance().getGameDir().resolve("saves"))) {
			waitForScreen(SelectWorldScreen.class);
			takeScreenshot("select_world_screen");
			clickScreenButton("selectWorld.create");
		}

		{
			waitForScreen(CreateWorldScreen.class);
			clickScreenButton("selectWorld.gameMode");
			clickScreenButton("selectWorld.gameMode");
			takeScreenshot("create_world_screen");
			clickScreenButton("selectWorld.create");
		}

		{
			// API test mods use experimental features
			waitForScreen(ConfirmScreen.class);
			clickScreenButton("gui.yes");
		}

		{
			enableDebugHud();
			waitForWorldTicks(200);
			takeScreenshot("in_game_overworld", 0);
		}

		MixinEnvironment.getCurrentEnvironment().audit();

		{
			// See if the player render events are working.
			setPerspective(Perspective.THIRD_PERSON_BACK);
			takeScreenshot("in_game_overworld_third_person");
			setPerspective(Perspective.FIRST_PERSON);
		}

		{
			openInventory();
			takeScreenshot("in_game_inventory");
			closeScreen();
		}

		{
			openGameMenu();
			takeScreenshot("game_menu");
			clickScreenButton("menu.returnToMenu");
			waitForScreen(TitleScreen.class);
			waitForServerStop();
		}

		try (var server = new TestDedicatedServer()) {
			connectToServer(server);
			waitForWorldTicks(5);

			final GameProfile profile = computeOnClient(MinecraftClient::getGameProfile);
			server.runCommand("op " + profile.getName());
			server.runCommand("gamemode creative " + profile.getName());

			waitForWorldTicks(20);
			takeScreenshot("server_in_game", 0);

			{ // Test that we can enter and exit configuration
				server.runCommand("debugconfig config " + profile.getName());
				waitForScreen(ReconfiguringScreen.class);
				takeScreenshot("server_config");
				server.runCommand("debugconfig unconfig " + profile.getId());
				waitForWorldTicks(1);
			}

			openGameMenu();
			takeScreenshot("server_game_menu");
			clickScreenButton("menu.disconnect");

			waitForScreen(MultiplayerScreen.class);
			clickScreenButton("gui.back");
		}

		{
			waitForScreen(TitleScreen.class);
			clickScreenButton("menu.quit");
		}
	}

	private boolean isDirEmpty(Path path) {
		try (DirectoryStream<Path> directory = Files.newDirectoryStream(path)) {
			return !directory.iterator().hasNext();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
