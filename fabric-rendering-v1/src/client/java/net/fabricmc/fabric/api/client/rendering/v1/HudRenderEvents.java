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
	public static final Event<Start> START = EventFactory.createArrayBacked(Start.class, listeners -> (client, context, tickCounter) -> {
		for (Start listener : listeners) {
			listener.onHudStart(client, context, tickCounter);
		}
	});

	/**
	 * Called after misc overlays (vignette, spyglass, and powder snow) have been rendered, and before the crosshair is rendered.
	 */
	public static final Event<AfterMiscOverlays> AFTER_MISC_OVERLAYS = EventFactory.createArrayBacked(AfterMiscOverlays.class, listeners -> (client, context, tickCounter) -> {
		for (AfterMiscOverlays listener : listeners) {
			listener.afterMiscOverlays(client, context, tickCounter);
		}
	});

	/**
	 * Called after the hotbar, status bars, experience bar, status effects overlays, and boss bar have been rendered, and before the sleep overlay is rendered.
	 */
	public static final Event<AfterMainHud> AFTER_MAIN_HUD = EventFactory.createArrayBacked(AfterMainHud.class, listeners -> (client, context, tickCounter) -> {
		for (AfterMainHud listener : listeners) {
			listener.afterMainHud(client, context, tickCounter);
		}
	});

	/**
	 * Called after the sleep overlay has been rendered, and before the demo timer, debug HUD, scoreboard, overlay message (action bar), and title and subtitle are rendered.
	 */
	public static final Event<AfterSleepOverlay> AFTER_SLEEP_OVERLAY = EventFactory.createArrayBacked(AfterSleepOverlay.class, listeners -> (client, context, tickCounter) -> {
		for (AfterSleepOverlay listener : listeners) {
			listener.afterSleepOverlay(client, context, tickCounter);
		}
	});

	/**
	 * Called after the debug HUD, scoreboard, overlay message (action bar), and title and subtitle have been rendered, and before the {@link net.minecraft.client.gui.hud.ChatHud} is rendered.
	 */
	public static final Event<BeforeChat> BEFORE_CHAT = EventFactory.createArrayBacked(BeforeChat.class, listeners -> (client, context, tickCounter) -> {
		for (BeforeChat listener : listeners) {
			listener.beforeChat(client, context, tickCounter);
		}
	});

	/**
	 * Called after the entire HUD is rendered.
	 */
	public static final Event<Last> LAST = EventFactory.createArrayBacked(Last.class, listeners -> (client, context, tickCounter) -> {
		for (Last listener : listeners) {
			listener.onHudLast(client, context, tickCounter);
		}
	});

	private HudRenderEvents() { }

	@FunctionalInterface
	public interface Start {
		/**
		 * Called at the start of HUD rendering, right before anything is rendered.
		 *
		 * @param client      the {@link MinecraftClient} instance
		 * @param context     the {@link DrawContext} instance
		 * @param tickCounter the {@link RenderTickCounter} instance with access to tick delta
		 */
		void onHudStart(MinecraftClient client, DrawContext context, RenderTickCounter tickCounter);
	}

	@FunctionalInterface
	public interface AfterMiscOverlays {
		/**
		 * Called after misc overlays (vignette, spyglass, and powder snow) have been rendered, and before the crosshair is rendered.
		 *
		 * @param client      the {@link MinecraftClient} instance
		 * @param context     the {@link DrawContext} instance
		 * @param tickCounter the {@link RenderTickCounter} instance with access to tick delta
		 */
		void afterMiscOverlays(MinecraftClient client, DrawContext context, RenderTickCounter tickCounter);
	}

	@FunctionalInterface
	public interface AfterMainHud {
		/**
		 * Called after the hotbar, status bars, experience bar, status effects overlays, and boss bar have been rendered, and before the sleep overlay is rendered.
		 *
		 * @param client      the {@link MinecraftClient} instance
		 * @param context     the {@link DrawContext} instance
		 * @param tickCounter the {@link RenderTickCounter} instance with access to tick delta
		 */
		void afterMainHud(MinecraftClient client, DrawContext context, RenderTickCounter tickCounter);
	}

	@FunctionalInterface
	public interface AfterSleepOverlay {
		/**
		 * Called after the sleep overlay has been rendered, and before the demo timer, debug HUD, scoreboard, overlay message (action bar), and title and subtitle are rendered.
		 *
		 * @param client      the {@link MinecraftClient} instance
		 * @param context     the {@link DrawContext} instance
		 * @param tickCounter the {@link RenderTickCounter} instance with access to tick delta
		 */
		void afterSleepOverlay(MinecraftClient client, DrawContext context, RenderTickCounter tickCounter);
	}

	@FunctionalInterface
	public interface BeforeChat {
		/**
		 * Called after the debug HUD, scoreboard, overlay message (action bar), and title and subtitle have been rendered, and before the {@link net.minecraft.client.gui.hud.ChatHud} is rendered.
		 *
		 * @param client      the {@link MinecraftClient} instance
		 * @param context     the {@link DrawContext} instance
		 * @param tickCounter the {@link RenderTickCounter} instance with access to tick delta
		 */
		void beforeChat(MinecraftClient client, DrawContext context, RenderTickCounter tickCounter);
	}

	@FunctionalInterface
	public interface Last {
		/**
		 * Called after the entire HUD is rendered.
		 *
		 * @param client      the {@link MinecraftClient} instance
		 * @param context     the {@link DrawContext} instance
		 * @param tickCounter the {@link RenderTickCounter} instance with access to tick delta
		 */
		void onHudLast(MinecraftClient client, DrawContext context, RenderTickCounter tickCounter);
	}
}
