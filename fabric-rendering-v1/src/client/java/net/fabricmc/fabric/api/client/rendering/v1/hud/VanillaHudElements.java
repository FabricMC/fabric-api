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

import net.minecraft.util.Identifier;

/**
 * Identifiers for vanilla hud elements for use in {@link HudElementRegistry}.
 *
 * <p>The identifiers in this interface are the vanilla hud elements in the order they are drawn in.
 * The first element is drawn first, which means it is at the bottom.
 * All vanilla elements except {@link #SLEEP} are in sub drawers and have a render condition attached ({@link net.minecraft.client.option.GameOptions#hudHidden}).
 * Operations relative to any element will generally inherit that element's render condition.
 * There is currently no mechanism to change the render condition of an element.
 *
 * <p>For common use cases and more details on how this API deals with render condition, see {@link HudElementRegistry}.
 */
public interface VanillaHudElements {
	/**
	 * The identifier for the vanilla miscellaneous overlays (such as vignette, spyglass, and powder snow) element.
	 */
	Identifier MISC_OVERLAYS = Identifier.ofVanilla("misc_overlays");
	/**
	 * The identifier for the vanilla crosshair element.
	 */
	Identifier CROSSHAIR = Identifier.ofVanilla("crosshair");
	/**
	 * The identifier for the vanilla hotbar, spectator hud, experience bar, and status bars element.
	 */
	Identifier HOTBAR_AND_BARS = Identifier.ofVanilla("hotbar_and_bars");
	/**
	 * The identifier for the vanilla status effects element.
	 */
	Identifier STATUS_EFFECTS = Identifier.ofVanilla("status_effects");
	/**
	 * The identifier for the vanilla boss bar element.
	 */
	Identifier BOSS_BAR = Identifier.ofVanilla("boss_bar");
	/**
	 * The identifier for the vanilla sleep overlay element.
	 */
	Identifier SLEEP = Identifier.ofVanilla("sleep");
	/**
	 * The identifier for the vanilla demo timer element.
	 */
	Identifier DEMO_TIMER = Identifier.ofVanilla("demo_timer");
	/**
	 * The identifier for the vanilla debug hud element.
	 */
	Identifier DEBUG = Identifier.ofVanilla("debug");
	/**
	 * The identifier for the vanilla scoreboard element.
	 */
	Identifier SCOREBOARD = Identifier.ofVanilla("scoreboard");
	/**
	 * The identifier for the vanilla overlay message element.
	 */
	Identifier OVERLAY_MESSAGE = Identifier.ofVanilla("overlay_message");
	/**
	 * The identifier for the vanilla title and subtitle element.
	 *
	 * <p>Note that this is not the sound subtitles.
	 */
	Identifier TITLE_AND_SUBTITLE = Identifier.ofVanilla("title_and_subtitle");
	/**
	 * The identifier for the vanilla chat element.
	 */
	Identifier CHAT = Identifier.ofVanilla("chat");
	/**
	 * The identifier for the vanilla player list element.
	 */
	Identifier PLAYER_LIST = Identifier.ofVanilla("player_list");
	/**
	 * The identifier for the vanilla sound subtitles element.
	 */
	Identifier SUBTITLES = Identifier.ofVanilla("subtitles");
}
