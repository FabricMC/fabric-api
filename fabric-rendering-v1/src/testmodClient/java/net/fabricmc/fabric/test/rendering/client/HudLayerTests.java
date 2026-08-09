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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;

public class HudLayerTests implements ClientModInitializer {
	private static final String MOD_ID = "fabric";
	private static final String BEFORE_MISC_OVERLAY = "test_before_misc_overlay";
	private static final String AFTER_MISC_OVERLAY = "test_after_misc_overlay";
	private static final String AFTER_EXPERIENCE_LEVEL = "test_after_experience_level";
	private static final String BEFORE_DEMO_TIMER = "test_before_demo_timer";
	private static final String BEFORE_CHAT = "test_before_chat";
	private static final String AFTER_SUBTITLES = "test_after_subtitles";
	private static boolean shouldRender = false;

	@Override
	public void onInitializeClient() {
		HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer
				.attachLayerBefore(IdentifiedLayer.MISC_OVERLAYS, Identifier.of(MOD_ID, BEFORE_MISC_OVERLAY), HudLayerTests::renderBeforeMiscOverlay)
				.attachLayerAfter(IdentifiedLayer.MISC_OVERLAYS, Identifier.of(MOD_ID, AFTER_MISC_OVERLAY), HudLayerTests::renderAfterMiscOverlay)
				.attachLayerAfter(IdentifiedLayer.EXPERIENCE_LEVEL, Identifier.of(MOD_ID, AFTER_EXPERIENCE_LEVEL), HudLayerTests::renderAfterExperienceLevel)
				.attachLayerBefore(IdentifiedLayer.DEMO_TIMER, Identifier.of(MOD_ID, BEFORE_DEMO_TIMER), HudLayerTests::renderBeforeDemoTimer)
				.attachLayerBefore(IdentifiedLayer.CHAT, Identifier.of(MOD_ID, BEFORE_CHAT), HudLayerTests::renderBeforeChat)
				.attachLayerAfter(IdentifiedLayer.SUBTITLES, Identifier.of(MOD_ID, AFTER_SUBTITLES), HudLayerTests::renderAfterSubtitles)
		);
	}

	private static void renderBeforeMiscOverlay(DrawContext context, RenderTickCounter tickCounter) {
		if (!shouldRender) return;
		// Render a blue rectangle at the top right of the screen, and it should be blocked by misc overlays such as vignette, spyglass, and powder snow
		context.fill(context.getScaledWindowWidth() - 200, 0, context.getScaledWindowWidth(), 30, Colors.BLUE);
		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "1. Blue rectangle blocked by overlays", context.getScaledWindowWidth() - 196, 10, Colors.WHITE);
		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "such as powder snow", context.getScaledWindowWidth() - 111, 20, Colors.WHITE);
	}

	private static void renderAfterMiscOverlay(DrawContext context, RenderTickCounter tickCounter) {
		shouldRender = true;

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
}
