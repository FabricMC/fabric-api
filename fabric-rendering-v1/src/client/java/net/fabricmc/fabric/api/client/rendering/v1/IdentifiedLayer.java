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

public interface IdentifiedLayer extends LayeredDrawer.Layer {
	Identifier MISC_OVERLAYS = Identifier.ofVanilla("misc_overlays");
	Identifier CROSSHAIR = Identifier.ofVanilla("crosshair");
	Identifier MAIN_HUD = Identifier.ofVanilla("main_hud");
	Identifier EXPERIENCE_LEVEL = Identifier.ofVanilla("experience_level");
	Identifier STAUS_EFFECTS = Identifier.ofVanilla("status_effects");
	Identifier BOSSBAR = Identifier.ofVanilla("bossbar");
	Identifier DEMO_TIMER = Identifier.ofVanilla("demo_timer");
	Identifier DEBUG_HUD = Identifier.ofVanilla("debug_hud");
	Identifier SCOREBOARD = Identifier.ofVanilla("scoreboard");
	Identifier OVERLAY_MESSAGE = Identifier.ofVanilla("overlay_message");
	Identifier TITLE_AND_SUBTITLE = Identifier.ofVanilla("title_and_subtitle");
	Identifier CHAT = Identifier.ofVanilla("chat");
	Identifier PLAYER_LIST = Identifier.ofVanilla("player_list");
	Identifier SUBTITLES = Identifier.ofVanilla("subtitles");
	Identifier SLEEP = Identifier.ofVanilla("sleep");

	Identifier id();

	static IdentifiedLayer wrapping(Identifier id, LayeredDrawer.Layer layer) {
		return new WrappedLayer(id, layer);
	}
}
