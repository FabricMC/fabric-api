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

package net.fabricmc.fabric.test.rendering.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonOptions;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

public class HudTests implements ClientModInitializer, FabricClientGameTest {
	private static final String MOD_ID = "fabric";
	private static final String BEFORE_MISC_OVERLAY = "test_before_misc_overlay";
	private static final String AFTER_MISC_OVERLAY = "test_after_misc_overlay";
	private static final String AFTER_HOTBAR_AND_BARS = "test_after_hotbar_and_bars";
	private static final String BEFORE_DEMO_TIMER = "test_before_demo_timer";
	private static final String BEFORE_CHAT = "test_before_chat";
	private static final String AFTER_SUBTITLES = "test_after_subtitles";
	private static boolean shouldRender = false;

	@Override
	public void onInitializeClient() {
		HudElementRegistry.attachElementBefore(VanillaHudElements.MISC_OVERLAYS, Identifier.fromNamespaceAndPath(MOD_ID, BEFORE_MISC_OVERLAY), HudTests::extractBeforeMiscOverlay);
		HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, Identifier.fromNamespaceAndPath(MOD_ID, AFTER_MISC_OVERLAY), HudTests::extractAfterMiscOverlay);
		HudElementRegistry.attachElementAfter(VanillaHudElements.INFO_BAR, Identifier.fromNamespaceAndPath(MOD_ID, AFTER_HOTBAR_AND_BARS), HudTests::extractAfterExperienceLevel);
		HudElementRegistry.attachElementBefore(VanillaHudElements.DEMO_TIMER, Identifier.fromNamespaceAndPath(MOD_ID, BEFORE_DEMO_TIMER), HudTests::extractBeforeDemoTimer);
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath(MOD_ID, BEFORE_CHAT), HudTests::extractBeforeChat);
		HudElementRegistry.attachElementAfter(VanillaHudElements.SUBTITLES, Identifier.fromNamespaceAndPath(MOD_ID, AFTER_SUBTITLES), HudTests::extractAfterSubtitles);

		// https://github.com/FabricMC/fabric/issues/4933#issuecomment-3552574307
		HudElementRegistry.replaceElement(
				VanillaHudElements.SUBTITLES, original -> (graphics, tracker) -> {
					graphics.pose().pushMatrix();
					graphics.pose().scale(0.25f);
					original.extractRenderState(graphics, tracker);
					graphics.pose().popMatrix();
				}
		);
	}

	private static void extractBeforeMiscOverlay(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		if (!shouldRender) return;
		// Render a blue rectangle at the top right of the screen, and it should be blocked by misc overlays such as vignette, spyglass, and powder snow
		graphics.fill(graphics.guiWidth() - 200, 0, graphics.guiWidth(), 30, CommonColors.BLUE);
		graphics.text(Minecraft.getInstance().font, "1. Blue rectangle blocked by overlays", graphics.guiWidth() - 196, 10, CommonColors.WHITE);
		graphics.text(Minecraft.getInstance().font, "such as powder snow", graphics.guiWidth() - 111, 20, CommonColors.WHITE);
	}

	private static void extractAfterMiscOverlay(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		if (!shouldRender) return;
		// Render a red square in the center of the screen underneath the crosshair
		graphics.fill(graphics.guiWidth() / 2 - 10, graphics.guiHeight() / 2 - 10, graphics.guiWidth() / 2 + 10, graphics.guiHeight() / 2 + 10, CommonColors.RED);
		graphics.centeredText(Minecraft.getInstance().font, "2. Red square underneath crosshair", graphics.guiWidth() / 2, graphics.guiHeight() / 2 + 10, CommonColors.WHITE);
	}

	private static void extractAfterExperienceLevel(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		if (!shouldRender) return;
		// Render a green rectangle at the bottom of the screen, and it should block the hotbar and status bars
		graphics.fill(graphics.guiWidth() / 2 - 50, graphics.guiHeight() - 50, graphics.guiWidth() / 2 + 50, graphics.guiHeight() - 10, CommonColors.GREEN);
		graphics.centeredText(Minecraft.getInstance().font, "3. This green rectangle should block the hotbar and status bars.", graphics.guiWidth() / 2, graphics.guiHeight() - 40, CommonColors.WHITE);
	}

	private static void extractBeforeDemoTimer(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		if (!shouldRender) return;
		// Render a yellow rectangle at the right of the screen, and it should be above the sleep overlay but below the scoreboard
		graphics.fill(graphics.guiWidth() - 240, graphics.guiHeight() / 2 - 10, graphics.guiWidth(), graphics.guiHeight() / 2 + 10, CommonColors.YELLOW);
		graphics.text(Minecraft.getInstance().font, "4. This yellow rectangle should be above", graphics.guiWidth() - 236, graphics.guiHeight() / 2 - 10, CommonColors.WHITE);
		graphics.text(Minecraft.getInstance().font, "the sleep overlay but below the scoreboard.", graphics.guiWidth() - 236, graphics.guiHeight() / 2, CommonColors.WHITE);
	}

	private static void extractBeforeChat(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		if (!shouldRender) return;
		// Render a blue rectangle at the bottom left of the screen, and it should be blocked by the chat
		graphics.fill(0, graphics.guiHeight() - 40, 300, graphics.guiHeight() - 50, CommonColors.BLUE);
		graphics.text(Minecraft.getInstance().font, "5. This blue rectangle should be blocked by the chat.", 0, graphics.guiHeight() - 50, CommonColors.WHITE);
	}

	private static void extractAfterSubtitles(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		if (!shouldRender) return;
		// Render a yellow rectangle at the top of the screen, and it should block the player list
		graphics.fill(graphics.guiWidth() / 2 - 150, 0, graphics.guiWidth() / 2 + 150, 15, CommonColors.YELLOW);
		graphics.centeredText(Minecraft.getInstance().font, "6. This yellow rectangle should block the player list.", graphics.guiWidth() / 2, 0, CommonColors.WHITE);
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		// Set up required test environment
		context.getInput().resizeWindow(2048, 1024); // Multiple of 256 to not squish the pixels of 256x overlays.
		context.runOnClient(client -> {
			client.options.hideGui = false;
			client.options.guiScale().set(2);
		});
		shouldRender = true;

		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			// Set up the test world
			singleplayer.getServer().runCommand("/tp @a 0 -60 0");
			singleplayer.getServer().runCommand("/scoreboard objectives add hud_layer_test dummy");
			singleplayer.getServer().runCommand("/scoreboard objectives setdisplay list hud_layer_test"); // Hack to show player list
			singleplayer.getServer().runCommand("/scoreboard objectives setdisplay sidebar hud_layer_test"); // Hack to show sidebar
			singleplayer.getServer().runOnServer(server -> server.overworld().setBlockAndUpdate(new BlockPos(0, -59, 0), Blocks.POWDER_SNOW.defaultBlockState()));

			// Wait for stuff to load
			singleplayer.getClientLevel().waitForChunksRender();
			singleplayer.getServer().runOnServer(server -> server.getPlayerList().broadcastSystemMessage(Component.nullToEmpty("hud_layer_" + BEFORE_CHAT), false)); // Chat messages disappear in 200 ticks so we send one 150 ticks in advance to test the before chat layer
			context.waitTicks(150); // The powder snow frosty vignette takes 140 ticks to fully appear, so we additionally wait for a total of 150 ticks

			// Take and assert screenshots
			context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("hud_layer_" + BEFORE_MISC_OVERLAY).withRegion(1648, 0, 400, 60).save());
			context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("hud_layer_" + AFTER_MISC_OVERLAY).withRegion(838, 494, 372, 56).save());
			context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("hud_layer_" + AFTER_HOTBAR_AND_BARS).withRegion(924, 924, 200, 80).save());

			// The sleep overlay takes 100 ticks to fully appear, so we start sleeping and wait for 100 ticks
			context.runOnClient(client -> client.player.setSleepingPos(new BlockPos(0, -59, 0)));
			context.waitTicks(100);

			context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("hud_layer_" + BEFORE_DEMO_TIMER).withRegion(1568, 492, 480, 40).save());
			context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("hud_layer_" + BEFORE_CHAT).withRegion(0, 924, 600, 20).save());

			context.runOnClient(client -> client.player.clearSleepingPos());
			context.waitTick();
			context.getInput().holdKey(InputConstants.KEY_TAB); // Show player list
			context.waitTick();
			context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("hud_layer_" + AFTER_SUBTITLES).withRegion(724, 0, 600, 30).save());
		}

		shouldRender = false;
	}
}
