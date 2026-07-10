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
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import org.spongepowered.asm.mixin.MixinEnvironment;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerReconfigScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
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
			testScreenSize(context, MainTarget.DEFAULT_WIDTH, MainTarget.DEFAULT_HEIGHT);
			context.getInput().resizeWindow(1000, 500);
			context.waitTick();
			testScreenSize(context, 1000, 500);
			context.getInput().resizeWindow(MainTarget.DEFAULT_WIDTH, MainTarget.DEFAULT_HEIGHT);
		}

		TestWorldSave spWorldSave;
		try (TestSingleplayerContext singleplayer = context.worldBuilder()
				.adjustSettings(creator -> creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			spWorldSave = singleplayer.getWorldSave();

			{
				setDebugOverlay(context, true);
				singleplayer.getConnection().waitForChunksRender();
				context.takeScreenshot("in_game_overworld");
			}

			{
				BlockPos chestPos = context.computeOnClient(client -> BlockPos.containing(client.player.position()).east().east().above().above());
				singleplayer.getServer().runCommand("setblock %d %d %d minecraft:chest".formatted(chestPos.getX(), chestPos.getY(), chestPos.getZ()));
				context.waitFor(client -> client.level.getBlockState(chestPos).is(Blocks.CHEST));
				context.getInput().lookAt(chestPos);
				context.waitTick();
				context.getInput().pressKey(options -> options.keyUse);
				context.waitForScreen(ContainerScreen.class);
				context.setScreen(() -> null);
			}

			{
				context.getInput().pressKey(options -> options.keyChat);
				context.getInput().typeChars("Hello, World!");
				context.getInput().holdKeyFor(InputConstants.KEY_RETURN, 0); // press without delay, enter not a keybind
				// wait for round trip of chat message to server and back to client
				singleplayer.getConnection().waitForServerboundPackets();
				singleplayer.getConnection().waitForClientboundPackets();
				context.takeScreenshot("chat_message_sent");
			}

			MixinEnvironment.getCurrentEnvironment().audit();

			{
				// See if the player render events are working.
				setCameraType(context, CameraType.THIRD_PERSON_BACK);
				context.takeScreenshot("in_game_overworld_third_person");
				setCameraType(context, CameraType.FIRST_PERSON);
			}

			{
				context.getInput().pressKey(options -> options.keyInventory);
				context.waitTick(); // wait for the client to process the keybind
				context.takeScreenshot("in_game_inventory");
				context.setScreen(() -> null);
			}

			{
				Cow serverCow = singleplayer.getServer().computeOnServer(_ -> {
					ServerLevel level = singleplayer.getConnection().getServerLevel();
					Cow cow = new Cow(EntityTypes.COW, level);
					cow.snapTo(singleplayer.getConnection().getServerPlayer().position().add(2, 0, 0));
					cow.setNoAi(true);
					level.addFreshEntity(cow);
					return cow;
				});
				singleplayer.getConnection().waitForClientboundEntityUpdates(EntityTypes.COW);

				Cow clientCow = context.computeOnClient(_ -> {
					Entity entity = singleplayer.getConnection().getClientLevel().getEntity(serverCow.getUUID());

					if (!(entity instanceof Cow cow)) {
						throw new AssertionError("Expected cow to exist on client");
					}

					return cow;
				});

				singleplayer.getServer().runOnServer(_ -> serverCow.snapTo(clientCow.position().add(2, 0, 0)));
				singleplayer.getConnection().waitForClientboundEntityUpdates(EntityTypes.COW);
				context.waitTicks(InterpolationHandler.DEFAULT_INTERPOLATION_STEPS); // allow the cow to interpolate to the right position
				context.runOnClient(_ -> {
					if (clientCow.position().distanceToSqr(serverCow.position()) >= 1e-7) {
						throw new AssertionError("Expected cow to move to server position");
					}
				});

				singleplayer.getServer().runOnServer(_ -> serverCow.remove(Entity.RemovalReason.DISCARDED));
				singleplayer.getConnection().waitForClientboundEntityUpdates(EntityTypes.COW);
				context.runOnClient(_ -> {
					if (!clientCow.isRemoved()) {
						throw new AssertionError("Expected cow to be removed from client");
					}
				});
			}
		}

		try (TestSingleplayerContext singleplayer = spWorldSave.open()) {
			singleplayer.getConnection().waitForChunksRender();
			context.takeScreenshot("in_game_overworld_2");
		}

		try (TestDedicatedServerContext server = context.worldBuilder().createServer()) {
			try (TestDedicatedServerConnection connection = server.connect()) {
				connection.waitForChunksRender();
				context.takeScreenshot("server_in_game");

				{ // Test that we can enter and exit configuration
					final GameProfile profile = context.computeOnClient(Minecraft::getGameProfile);
					server.runCommand("debugconfig config " + profile.name());
					context.waitForScreen(ServerReconfigScreen.class);
					context.takeScreenshot("server_config");
					server.runCommand("debugconfig unconfig " + profile.id());
					// TODO: better way to wait for reconfiguration to end
					context.waitTicks(100);
				}
			}
		}

		setDebugOverlay(context, false);
	}

	private static void waitForTitleScreenFade(ClientGameTestContext context) {
		context.waitFor(client -> {
			return (client.gui.screen() instanceof TitleScreenAccessor titleScreen) && !titleScreen.isFading();
		});
	}

	private static void setDebugOverlay(ClientGameTestContext context, boolean f3Enabled) {
		context.runOnClient(client -> client.debugEntries.setOverlayVisible(f3Enabled));
	}

	private static void setCameraType(ClientGameTestContext context, CameraType cameraType) {
		context.runOnClient(client -> client.options.setCameraType(cameraType));
	}

	private static void testScreenSize(ClientGameTestContext context, int expectedWidth, int expectedHeight) {
		context.runOnClient(client -> {
			if (client.getWindow().getScreenWidth() != expectedWidth || client.getWindow().getScreenHeight() != expectedHeight) {
				throw new AssertionError("Expected window size to be (%d, %d) but was (%d, %d)".formatted(expectedWidth, expectedHeight, client.getWindow().getScreenWidth(), client.getWindow().getScreenHeight()));
			}

			if (client.getWindow().getWidth() != expectedWidth || client.getWindow().getHeight() != expectedHeight) {
				throw new AssertionError("Expected framebuffer size to be (%d, %d) but was (%d, %d)".formatted(expectedWidth, expectedHeight, client.getWindow().getWidth(), client.getWindow().getHeight()));
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
