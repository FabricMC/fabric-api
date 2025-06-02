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

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

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
		HudElementRegistry.attachElementBefore(VanillaHudElements.MISC_OVERLAYS, Identifier.of(MOD_ID, BEFORE_MISC_OVERLAY), HudTests::renderBeforeMiscOverlay);
		HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, Identifier.of(MOD_ID, AFTER_MISC_OVERLAY), HudTests::renderAfterMiscOverlay);
		HudElementRegistry.attachElementAfter(VanillaHudElements.INFO_BAR, Identifier.of(MOD_ID, AFTER_HOTBAR_AND_BARS), HudTests::renderAfterExperienceLevel);
		HudElementRegistry.attachElementBefore(VanillaHudElements.DEMO_TIMER, Identifier.of(MOD_ID, BEFORE_DEMO_TIMER), HudTests::renderBeforeDemoTimer);
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.of(MOD_ID, BEFORE_CHAT), HudTests::renderBeforeChat);
		HudElementRegistry.attachElementAfter(VanillaHudElements.SUBTITLES, Identifier.of(MOD_ID, AFTER_SUBTITLES), HudTests::renderAfterSubtitles);
	}

	private static void renderBeforeMiscOverlay(DrawContext context, RenderTickCounter tickCounter) {
		if (!shouldRender) return;
		// Render a blue rectangle at the top right of the screen, and it should be blocked by misc overlays such as vignette, spyglass, and powder snow
		context.fill(context.getScaledWindowWidth() - 200, 0, context.getScaledWindowWidth(), 30, Colors.BLUE);
		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "1. Blue rectangle blocked by overlays", context.getScaledWindowWidth() - 196, 10, Colors.WHITE);
		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "such as powder snow", context.getScaledWindowWidth() - 111, 20, Colors.WHITE);
	}

	private static void renderAfterMiscOverlay(DrawContext context, RenderTickCounter tickCounter) {
		if (!shouldRender) return;
		// Render a red square in the center of the screen underneath the crosshair
		context.fill(context.getScaledWindowWidth() / 2 - 10, context.getScaledWindowHeight() / 2 - 10, context.getScaledWindowWidth() / 2 + 10, context.getScaledWindowHeight() / 2 + 10, Colors.RED);
		context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, "2. Red square underneath crosshair", context.getScaledWindowWidth() / 2, context.getScaledWindowHeight() / 2 + 10, Colors.WHITE);
	}

	private static void renderAfterExperienceLevel(DrawContext context, RenderTickCounter tickCounter) {
		if (!shouldRender) return;
		// Render a green rectangle at the bottom of the screen, and it should block the hotbar and status bars
		context.fill(context.getScaledWindowWidth() / 2 - 50, context.getScaledWindowHeight() - 50, context.getScaledWindowWidth() / 2 + 50, context.getScaledWindowHeight() - 10, Colors.GREEN);
		context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, "3. This green rectangle should block the hotbar and status bars.", context.getScaledWindowWidth() / 2, context.getScaledWindowHeight() - 40, Colors.WHITE);
	}

	private static void renderBeforeDemoTimer(DrawContext context, RenderTickCounter tickCounter) {
		if (!shouldRender) return;
		// Render a yellow rectangle at the right of the screen, and it should be above the sleep overlay but below the scoreboard
		context.fill(context.getScaledWindowWidth() - 240, context.getScaledWindowHeight() / 2 - 10, context.getScaledWindowWidth(), context.getScaledWindowHeight() / 2 + 10, Colors.YELLOW);
		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "4. This yellow rectangle should be above", context.getScaledWindowWidth() - 236, context.getScaledWindowHeight() / 2 - 10, Colors.WHITE);
		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "the sleep overlay but below the scoreboard.", context.getScaledWindowWidth() - 236, context.getScaledWindowHeight() / 2, Colors.WHITE);
	}

	private static void renderBeforeChat(DrawContext context, RenderTickCounter tickCounter) {
		if (!shouldRender) return;
		// Render a blue rectangle at the bottom left of the screen, and it should be blocked by the chat
		context.fill(0, context.getScaledWindowHeight() - 40, 300, context.getScaledWindowHeight() - 50, Colors.BLUE);
		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "5. This blue rectangle should be blocked by the chat.", 0, context.getScaledWindowHeight() - 50, Colors.WHITE);
	}

	private static void renderAfterSubtitles(DrawContext context, RenderTickCounter tickCounter) {
		if (!shouldRender) return;
		// Render a yellow rectangle at the top of the screen, and it should block the player list
		context.fill(context.getScaledWindowWidth() / 2 - 150, 0, context.getScaledWindowWidth() / 2 + 150, 15, Colors.YELLOW);
		context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, "6. This yellow rectangle should block the player list.", context.getScaledWindowWidth() / 2, 0, Colors.WHITE);
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		// Set up required test environment
		context.getInput().resizeWindow(2048, 1024); // Multiple of 256 to not squish the pixels of 256x overlays.
		context.runOnClient(client -> {
			client.options.hudHidden = false;
			client.options.getGuiScale().setValue(2);
		});
		shouldRender = true;

		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			// Set up the test world
			singleplayer.getServer().runCommand("/tp @a 0 -60 0");
			singleplayer.getServer().runCommand("/scoreboard objectives add hud_layer_test dummy");
			singleplayer.getServer().runCommand("/scoreboard objectives setdisplay list hud_layer_test"); // Hack to show player list
			singleplayer.getServer().runCommand("/scoreboard objectives setdisplay sidebar hud_layer_test"); // Hack to show sidebar
			singleplayer.getServer().runOnServer(server -> server.getOverworld().setBlockState(new BlockPos(0, -59, 0), Blocks.POWDER_SNOW.getDefaultState()));

			// Wait for stuff to load
			singleplayer.getClientWorld().waitForChunksRender();
			singleplayer.getServer().runOnServer(server -> server.getPlayerManager().broadcast(Text.of("hud_layer_" + BEFORE_CHAT), false)); // Chat messages disappear in 200 ticks so we send one 150 ticks in advance to test the before chat layer
			context.waitTicks(150); // The powder snow frosty vignette takes 140 ticks to fully appear, so we additionally wait for a total of 150 ticks

			// Take and assert screenshots
			context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("hud_layer_" + BEFORE_MISC_OVERLAY).withRegion(1648, 0, 400, 60).save());
			context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("hud_layer_" + AFTER_MISC_OVERLAY).withRegion(838, 494, 372, 56).save());
			context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("hud_layer_" + AFTER_HOTBAR_AND_BARS).withRegion(924, 924, 200, 80).save());

			// The sleep overlay takes 100 ticks to fully appear, so we start sleeping and wait for 100 ticks
			context.runOnClient(client -> client.player.setSleepingPosition(new BlockPos(0, -59, 0)));
			context.waitTicks(100);

			context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("hud_layer_" + BEFORE_DEMO_TIMER).withRegion(1568, 492, 480, 40).save());
			context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("hud_layer_" + BEFORE_CHAT).withRegion(0, 924, 600, 20).save());

			context.runOnClient(client -> client.player.clearSleepingPosition());
			context.waitTick();
			context.getInput().holdKey(InputUtil.GLFW_KEY_TAB); // Show player list
			context.waitTick();
			context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("hud_layer_" + AFTER_SUBTITLES).withRegion(724, 0, 600, 30).save());
		}

		shouldRender = false;
	}
}
