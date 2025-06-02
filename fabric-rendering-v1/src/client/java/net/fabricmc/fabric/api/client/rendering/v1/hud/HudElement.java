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

package net.fabricmc.fabric.api.client.rendering.v1.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/**
 * Represents a mod added {@link net.minecraft.client.gui.hud.InGameHud} element that can be rendered on the screen.
 *
 *
 * <p>HUD elements should be registered using {@link HudElementRegistry}
 */
public interface HudElement {
	/**
	 * Renders the HUD element.
	 *
	 * @param context the {@link DrawContext} used for rendering
	 * @param tickCounter the {@link RenderTickCounter} providing timing information
	 */
	void render(DrawContext context, RenderTickCounter tickCounter);
}
