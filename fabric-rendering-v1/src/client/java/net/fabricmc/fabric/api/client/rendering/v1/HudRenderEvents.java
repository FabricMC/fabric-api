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

package net.fabricmc.fabric.api.client.rendering.v1;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Events for rendering elements on the HUD.
 *
 * <p>These events will not be called if the HUD is hidden with F1.
 */
public final class HudRenderEvents {
	/**
	 * Called at the start of HUD rendering, right before anything is rendered.
	 */
	public static final Event<HudRenderStage> START = createEventForStage();

	/**
	 * Called after misc overlays (vignette, spyglass, and powder snow) have been rendered, and before the crosshair is rendered.
	 */
	public static final Event<HudRenderStage> AFTER_MISC_OVERLAYS = createEventForStage();

	/**
	 * Called after the hotbar, status bars, and experience bar have been rendered, and before the status effects overlays are rendered.
	 */
	public static final Event<HudRenderStage> AFTER_MAIN_HUD = createEventForStage();

	/**
	 * Called after the debug HUD, scoreboard, overlay message (action bar), and title and subtitle have been rendered, and before the {@link net.minecraft.client.gui.hud.ChatHud} is rendered.
	 */
	public static final Event<HudRenderStage> BEFORE_CHAT = createEventForStage();

	/**
	 * Called after the entire HUD is rendered.
	 */
	public static final Event<HudRenderStage> LAST = createEventForStage();

	private HudRenderEvents() { }

	private static Event<HudRenderStage> createEventForStage() {
		return EventFactory.createArrayBacked(HudRenderStage.class, listeners -> (client, context, tickCounter) -> {
			for (HudRenderStage listener : listeners) {
				listener.onHudRender(client, context, tickCounter);
			}
		});
	}

	@FunctionalInterface
	public interface HudRenderStage {
		/**
		 * Called sometime during a specific HUD render stage.
		 *
		 * @param client      The {@link MinecraftClient} instance
		 * @param context     The {@link DrawContext} instance
		 * @param tickCounter The {@link RenderTickCounter} instance
		 */
		void onHudRender(MinecraftClient client, DrawContext context, RenderTickCounter tickCounter);
	}
}
