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

import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.client.rendering.WrappedLayer;

/**
 * A hud layer that has an identifier attached for use in {@link LayeredDrawerWrapper}.
 *
 * <p>The identifiers in this interface are the vanilla hud layers in the order they are drawn in.
 * The first layer is drawn first, which means it is at the bottom.
 * All vanilla layers except {@link #SLEEP} are in sub drawers and have a render condition attached ({@link net.minecraft.client.option.GameOptions#hudHidden}).
 * Operations relative to any layer will generally inherit that layer's render condition.
 * There is currently no mechanism to change the render condition of a layer.
 *
 * <p>For common use cases and more details on how this API deals with render condition, see {@link LayeredDrawerWrapper}.
 */
public interface IdentifiedLayer extends LayeredDrawer.Layer {
	/**
	 * The identifier for the vanilla miscellaneous overlays (such as vignette, spyglass, and powder snow) layer.
	 */
	Identifier MISC_OVERLAYS = Identifier.ofVanilla("misc_overlays");
	/**
	 * The identifier for the vanilla crosshair layer.
	 */
	Identifier CROSSHAIR = Identifier.ofVanilla("crosshair");
	/**
	 * The identifier for the vanilla hotbar, spectator hud, experience bar, and status bars layer.
	 */
	Identifier HOTBAR_AND_BARS = Identifier.ofVanilla("hotbar_and_bars");
	/**
	 * The identifier for the vanilla experience level layer.
	 */
	Identifier EXPERIENCE_LEVEL = Identifier.ofVanilla("experience_level");
	/**
	 * The identifier for the vanilla status effects layer.
	 */
	Identifier STATUS_EFFECTS = Identifier.ofVanilla("status_effects");
	/**
	 * The identifier for the vanilla boss bar layer.
	 */
	Identifier BOSS_BAR = Identifier.ofVanilla("boss_bar");
	/**
	 * The identifier for the vanilla sleep overlay layer.
	 */
	Identifier SLEEP = Identifier.ofVanilla("sleep");
	/**
	 * The identifier for the vanilla demo timer layer.
	 */
	Identifier DEMO_TIMER = Identifier.ofVanilla("demo_timer");
	/**
	 * The identifier for the vanilla debug hud layer.
	 */
	Identifier DEBUG = Identifier.ofVanilla("debug");
	/**
	 * The identifier for the vanilla scoreboard layer.
	 */
	Identifier SCOREBOARD = Identifier.ofVanilla("scoreboard");
	/**
	 * The identifier for the vanilla overlay message layer.
	 */
	Identifier OVERLAY_MESSAGE = Identifier.ofVanilla("overlay_message");
	/**
	 * The identifier for the vanilla title and subtitle layer.
	 *
	 * <p>Note that this is not the sound subtitles.
	 */
	Identifier TITLE_AND_SUBTITLE = Identifier.ofVanilla("title_and_subtitle");
	/**
	 * The identifier for the vanilla chat layer.
	 */
	Identifier CHAT = Identifier.ofVanilla("chat");
	/**
	 * The identifier for the vanilla player list layer.
	 */
	Identifier PLAYER_LIST = Identifier.ofVanilla("player_list");
	/**
	 * The identifier for the vanilla sound subtitles layer.
	 */
	Identifier SUBTITLES = Identifier.ofVanilla("subtitles");

	/**
	 * @return the identifier of the layer
	 */
	Identifier id();

	/**
	 * Wraps a hud layer in an identified layer.
	 *
	 * @param id    the identifier to give the layer
	 * @param layer the layer to wrap
	 * @return the identified layer
	 */
	static IdentifiedLayer of(Identifier id, LayeredDrawer.Layer layer) {
		return new WrappedLayer(id, layer);
	}
}
