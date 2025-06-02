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

package net.fabricmc.fabric.impl.client.rendering.hud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class HudElementRegistryImpl {
	private static final Set<Identifier> VANILLA_ELEMENT_IDS = Set.of(
			VanillaHudElements.MISC_OVERLAYS,
			VanillaHudElements.CROSSHAIR,
			VanillaHudElements.HOTBAR_AND_BARS,
			VanillaHudElements.STATUS_EFFECTS,
			VanillaHudElements.BOSS_BAR,
			VanillaHudElements.SLEEP,
			VanillaHudElements.DEMO_TIMER,
			VanillaHudElements.DEBUG,
			VanillaHudElements.SCOREBOARD,
			VanillaHudElements.OVERLAY_MESSAGE,
			VanillaHudElements.TITLE_AND_SUBTITLE,
			VanillaHudElements.CHAT,
			VanillaHudElements.PLAYER_LIST,
			VanillaHudElements.SUBTITLES
	);

	private static final List<HudElement> FIRST_ELEMENTS = new ArrayList<>();
	private static final List<HudElement> LAST_ELEMENTS = new ArrayList<>();

	private static final Map<Identifier, Event<HudElement>> BEFORE_EVENTS = new HashMap<>();
	private static final Map<Identifier, Event<HudElement>> AFTER_EVENTS = new HashMap<>();

	public static void addFirst(HudElement element) {
		FIRST_ELEMENTS.add(element);
	}

	public static void addLast(HudElement element) {
		LAST_ELEMENTS.add(element);
	}

	public static Event<HudElement> before(Identifier beforeThis) {
		validateVanillaId(beforeThis);
		return BEFORE_EVENTS.computeIfAbsent(beforeThis, id -> EventFactory.createArrayBacked(HudElement.class, elements -> (context, tickCounter) -> {
			for (HudElement element : elements) {
				element.render(context, tickCounter);
			}
		}));
	}

	public static Event<HudElement> after(Identifier afterThis) {
		validateVanillaId(afterThis);
		return AFTER_EVENTS.computeIfAbsent(afterThis, id -> EventFactory.createArrayBacked(HudElement.class, elements -> (context, tickCounter) -> {
			for (HudElement element : elements) {
				element.render(context, tickCounter);
			}
		}));
	}

	private static void validateVanillaId(Identifier id) {
		if (!VANILLA_ELEMENT_IDS.contains(id)) {
			throw new IllegalArgumentException("Vanilla layer " + id + " does not exist");
		}
	}

	public static void renderFirst(DrawContext context, RenderTickCounter tickCounter) {
		for (HudElement element : FIRST_ELEMENTS) {
			element.render(context, tickCounter);
		}
	}

	public static void renderVanilla(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Identifier id, Operation<Void> renderVanilla) {
		if (BEFORE_EVENTS.containsKey(id)) {
			BEFORE_EVENTS.get(id).invoker().render(context, tickCounter);
		}

		renderVanilla.call(instance, context, tickCounter);

		if (AFTER_EVENTS.containsKey(id)) {
			AFTER_EVENTS.get(id).invoker().render(context, tickCounter);
		}
	}

	public static void renderLast(DrawContext context, RenderTickCounter tickCounter) {
		for (HudElement element : LAST_ELEMENTS) {
			element.render(context, tickCounter);
		}
	}
}
