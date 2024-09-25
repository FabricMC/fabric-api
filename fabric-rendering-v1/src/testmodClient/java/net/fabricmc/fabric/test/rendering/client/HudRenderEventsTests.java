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

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderEvents;

public class HudRenderEventsTests implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HudRenderEvents.START.register((context, tickCounter) -> { });
		// Render a red square in the center of the screen underneath the crosshair
		HudRenderEvents.AFTER_MISC_OVERLAYS.register((context, tickCounter) -> context.fill(context.getScaledWindowWidth() / 2 - 10, context.getScaledWindowHeight() / 2 - 10, context.getScaledWindowWidth() / 2 + 10, context.getScaledWindowHeight() / 2 + 10, 0xFFFF0000));
		// Render a green rectangle at the bottom of the screen, and it should block the hotbar and status bars
		HudRenderEvents.AFTER_MAIN_HUD.register((context, tickCounter) -> context.fill(context.getScaledWindowWidth() / 2 - 10, context.getScaledWindowHeight() / 2 - 10, context.getScaledWindowWidth() - 50, context.getScaledWindowHeight() - 10, 0xFF00FF00));
		// Render a blue rectangle at the bottom left of the screen, and it should be blocked by the chat
		HudRenderEvents.BEFORE_CHAT.register((context, tickCounter) -> context.fill(0, context.getScaledWindowHeight() / 2, 100, context.getScaledWindowHeight() - 10, 0xFF0000FF));
		// Render a yellow rectangle at the top of the screen, and it should block the player list
		HudRenderEvents.LAST.register((context, tickCounter) -> context.fill(0, 0, context.getScaledWindowWidth(), 100, 0xFFFFFF00));
	}
}
