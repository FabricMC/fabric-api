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
import net.minecraft.client.gui.GuiGraphics;
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
		HudElementRegistry.attachElementBefore(VanillaHudElements.MISC_OVERLAYS, Identifier.fromNamespaceAndPath(MOD_ID, BEFORE_MISC_OVERLAY), HudTests::renderBeforeMiscOverlay);
		HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, Identifier.fromNamespaceAndPath(MOD_ID, AFTER_MISC_OVERLAY), HudTests::renderAfterMiscOverlay);
		HudElementRegistry.attachElementAfter(VanillaHudElements.INFO_BAR, Identifier.fromNamespaceAndPath(MOD_ID, AFTER_HOTBAR_AND_BARS), HudTests::renderAfterExperienceLevel);
		HudElementRegistry.attachElementBefore(VanillaHudElements.DEMO_TIMER, Identifier.fromNamespaceAndPath(MOD_ID, BEFORE_DEMO_TIMER), HudTests::renderBeforeDemoTimer);
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath(MOD_ID, BEFORE_CHAT), HudTests::renderBeforeChat);
		HudElementRegistry.attachElementAfter(VanillaHudElements.SUBTITLES, Identifier.fromNamespaceAndPath(MOD_ID, AFTER_SUBTITLES), HudTests::renderAfterSubtitles);

		// https://github.com/FabricMC/fabric/issues/4933#issuecomment-3552574307
		HudElementRegistry.replaceElement(
				VanillaHudElements.SUBTITLES, original -> (graphics, tracker) -> {
					graphics.pose().pushMatrix();
					graphics.pose().scale(0.25f);
					original.render(graphics, tracker);
					graphics.pose().popMatrix();
				}
		);
	}

	private static void renderBeforeMiscOverlay(GuiGraphics context, DeltaTracker tickCounter) {
		if (!shouldRender) return;
		// Render a blue rectangle at the top right of the screen, and it should be blocked by misc overlays such as vignette, spyglass, and powder snow
		context.fill(context.guiWidth() - 200, 0, context.guiWidth(), 30, CommonColors.BLUE);
		context.drawString(Minecraft.getInstance().font, "1. Blue rectangle blocked by overlays", context.guiWidth() - 196, 10, CommonColors.WHITE);
		context.drawString(Minecraft.getInstance().font, "such as powder snow", context.guiWidth() - 111, 20, CommonColors.WHITE);
	}

	private static void renderAfterMiscOverlay(GuiGraphics context, DeltaTracker tickCounter) {
		if (!shouldRender) return;
		// Render a red square in the center of the screen underneath the crosshair
		context.fill(context.guiWidth() / 2 - 10, context.guiHeight() / 2 - 10, context.guiWidth() / 2 + 10, context.guiHeight() / 2 + 10, CommonColors.RED);
		context.drawCenteredString(Minecraft.getInstance().font, "2. Red square underneath crosshair", context.guiWidth() / 2, context.guiHeight() / 2 + 10, CommonColors.WHITE);
	}

	private static void renderAfterExperienceLevel(GuiGraphics context, DeltaTracker tickCounter) {
		if (!shouldRender) return;
		// Render a green rectangle at the bottom of the screen, and it should block the hotbar and status bars
		context.fill(context.guiWidth() / 2 - 50, context.guiHeight() - 50, context.guiWidth() / 2 + 50, context.guiHeight() - 10, CommonColors.GREEN);
		context.drawCenteredString(Minecraft.getInstance().font, "3. This green rectangle should block the hotbar and status bars.", context.guiWidth() / 2, context.guiHeight() - 40, CommonColors.WHITE);
	}

	private static void renderBeforeDemoTimer(GuiGraphics context, DeltaTracker tickCounter) {
		if (!shouldRender) return;
		// Render a yellow rectangle at the right of the screen, and it should be above the sleep overlay but below the scoreboard
		context.fill(context.guiWidth() - 240, context.guiHeight() / 2 - 10, context.guiWidth(), context.guiHeight() / 2 + 10, CommonColors.YELLOW);
		context.drawString(Minecraft.getInstance().font, "4. This yellow rectangle should be above", context.guiWidth() - 236, context.guiHeight() / 2 - 10, CommonColors.WHITE);
		context.drawString(Minecraft.getInstance().font, "the sleep overlay but below the scoreboard.", context.guiWidth() - 236, context.guiHeight() / 2, CommonColors.WHITE);
	}

	private static void renderBeforeChat(GuiGraphics context, DeltaTracker tickCounter) {
		if (!shouldRender) return;
		// Render a blue rectangle at the bottom left of the screen, and it should be blocked by the chat
		context.fill(0, context.guiHeight() - 40, 300, context.guiHeight() - 50, CommonColors.BLUE);
		context.drawString(Minecraft.getInstance().font, "5. This blue rectangle should be blocked by the chat.", 0, context.guiHeight() - 50, CommonColors.WHITE);
	}

	private static void renderAfterSubtitles(GuiGraphics context, DeltaTracker tickCounter) {
		if (!shouldRender) return;
		// Render a yellow rectangle at the top of the screen, and it should block the player list
		context.fill(context.guiWidth() / 2 - 150, 0, context.guiWidth() / 2 + 150, 15, CommonColors.YELLOW);
		context.drawCenteredString(Minecraft.getInstance().font, "6. This yellow rectangle should block the player list.", context.guiWidth() / 2, 0, CommonColors.WHITE);
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
			singleplayer.getClientWorld().waitForChunksRender();
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
