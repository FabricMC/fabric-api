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

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * @deprecated Use {@link HudElementRegistry} instead.
 */
@Deprecated
public interface HudRenderCallback {
	Event<HudRenderCallback> EVENT = EventFactory.createArrayBacked(HudRenderCallback.class, (listeners) -> (context, tickCounter) -> {
		for (HudRenderCallback event : listeners) {
			event.onHudRender(context, tickCounter);
		}
	});

	/**
	 * Called after rendering the whole hud, which is displayed in game, in a world.
	 *
	 * @param drawContext the {@link DrawContext} instance
	 * @param tickCounter the {@link RenderTickCounter} instance
	 */
	void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter);
}
