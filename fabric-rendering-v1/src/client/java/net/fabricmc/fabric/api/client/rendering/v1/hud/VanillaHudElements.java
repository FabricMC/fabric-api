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
 * A hud element that has an identifier attached for use in {@link HudElementRegistry}.
 *
 * <p>The identifiers in this interface are the vanilla hud layers in the order they are drawn in.
 * The first element is drawn first, which means it is at the bottom.
 * All vanilla layers except {@link #SLEEP} are in sub drawers and have a render condition attached ({@link net.minecraft.client.option.GameOptions#hudHidden}).
 * Operations relative to any element will generally inherit that element's render condition.
 * There is currently no mechanism to change the render condition of an element.
 *
 * <p>For common use cases and more details on how this API deals with render condition, see {@link HudElementRegistry}.
 */
public final class VanillaHudElements {
	/**
	 * The identifier for the vanilla miscellaneous overlays (such as vignette, spyglass, and powder snow) element.
	 */
	public static final Identifier MISC_OVERLAYS = Identifier.ofVanilla("misc_overlays");
	/**
	 * The identifier for the vanilla crosshair element.
	 */
	public static final Identifier CROSSHAIR = Identifier.ofVanilla("crosshair");
	/**
	 * The identifier for the vanilla spectator menu.
	 */
	public static final Identifier SPECTATOR_MENU = Identifier.ofVanilla("spectator_menu");
	/**
	 * The identifier for the vanilla hotbar.
	 */
	public static final Identifier HOTBAR = Identifier.ofVanilla("hotbar");
	/**
	 * The identifier for the player armor level bar.
	 */
	public static final Identifier ARMOR_BAR = Identifier.ofVanilla("armor_bar");
	/**
	 * The identifier for the player health bar.
	 */
	public static final Identifier HEALTH_BAR = Identifier.ofVanilla("health_bar");
	/**
	 * The identifier for the player hunger level bar.
	 */
	public static final Identifier FOOD_BAR = Identifier.ofVanilla("food_bar");
	/**
	 * The identifier for the player air level bar.
	 */
	public static final Identifier AIR_BAR = Identifier.ofVanilla("air_bar");
	/**
	 * The identifier for the vanilla mount health.
	 */
	public static final Identifier MOUNT_HEALTH = Identifier.ofVanilla("mount_health");
	/**
	 * The identifier for the info bar, either empty, experience bar, locator, or jump bar.
	 */
	public static final Identifier INFO_BAR = Identifier.ofVanilla("info_bar");
	/**
	 * The identifier for experience level tooltip.
	 */
	public static final Identifier EXPERIENCE_LEVEL = Identifier.ofVanilla("experience_level");
	/**
	 * The identifier for held item tooltip.
	 */
	public static final Identifier HELD_ITEM_TOOLTIP = Identifier.ofVanilla("held_item_tooltip");
	/**
	 * The identifier for the vanilla spectator tooltip.
	 */
	public static final Identifier SPECTATOR_TOOLTIP = Identifier.ofVanilla("spectator_tooltip");
	/**
	 * The identifier for the vanilla status effects element.
	 */
	public static final Identifier STATUS_EFFECTS = Identifier.ofVanilla("status_effects");
	/**
	 * The identifier for the vanilla boss bar element.
	 */
	public static final Identifier BOSS_BAR = Identifier.ofVanilla("boss_bar");
	/**
	 * The identifier for the vanilla sleep overlay element.
	 */
	public static final Identifier SLEEP = Identifier.ofVanilla("sleep");
	/**
	 * The identifier for the vanilla demo timer element.
	 */
	public static final Identifier DEMO_TIMER = Identifier.ofVanilla("demo_timer");
	/**
	 * The identifier for the vanilla debug hud element.
	 */
	public static final Identifier DEBUG = Identifier.ofVanilla("debug");
	/**
	 * The identifier for the vanilla scoreboard element.
	 */
	public static final Identifier SCOREBOARD = Identifier.ofVanilla("scoreboard");
	/**
	 * The identifier for the vanilla overlay message element.
	 */
	public static final Identifier OVERLAY_MESSAGE = Identifier.ofVanilla("overlay_message");
	/**
	 * The identifier for the vanilla title and subtitle element.
	 *
	 * <p>Note that this is not the sound subtitles.
	 */
	public static final Identifier TITLE_AND_SUBTITLE = Identifier.ofVanilla("title_and_subtitle");
	/**
	 * The identifier for the vanilla chat element.
	 */
	public static final Identifier CHAT = Identifier.ofVanilla("chat");
	/**
	 * The identifier for the vanilla player list element.
	 */
	public static final Identifier PLAYER_LIST = Identifier.ofVanilla("player_list");
	/**
	 * The identifier for the vanilla sound subtitles element.
	 */
	public static final Identifier SUBTITLES = Identifier.ofVanilla("subtitles");

	private VanillaHudElements() {
	}
}
