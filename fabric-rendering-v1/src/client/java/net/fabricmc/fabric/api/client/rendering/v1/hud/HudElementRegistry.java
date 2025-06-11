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

import java.util.Objects;
import java.util.function.Function;

import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.client.rendering.hud.HudElementRegistryImpl;

/**
 * A registry of identified hud layers with methods to add layers in specific positions.
 *
 * <p>Operations relative to a vanilla element will inherit that element's render condition.
 * The render condition for all vanilla layers except {@link VanillaHudElements#SLEEP} is {@link net.minecraft.client.option.GameOptions#hudHidden}.
 * Only {@link #addFirst(Identifier, HudElement)} and {@link #addLast(Identifier, HudElement)} will not inherit any render condition.
 * There is currently no mechanism to change the render condition of a vanilla element.
 * For vanilla layers, see {@link VanillaHudElements}.
 *
 * <p>Common places to add layers (as of 1.21.6):
 * <table>
 *     <tr>
 *         <th>Injection Point</th>
 *         <th>Use Case</th>
 *     </tr>
 *     <tr>
 *         <td>Before {@link VanillaHudElements#MISC_OVERLAYS MISC_OVERLAYS}</td>
 *         <td>Render before everything</td>
 *     </tr>
 *     <tr>
 *         <td>After {@link VanillaHudElements#MISC_OVERLAYS MISC_OVERLAYS}</td>
 *         <td>Render after misc overlays (vignette, spyglass, and powder snow) and before the crosshair</td>
 *     </tr>
 *     <tr>
 *         <td>After {@link VanillaHudElements#BOSS_BAR BOSS_BAR}</td>
 *         <td>Render after most main hud layers like hotbar, spectator hud, status bars, experience bar, status effects overlays, and boss bar and before the sleep overlay</td>
 *     </tr>
 *     <tr>
 *         <td>Before {@link VanillaHudElements#DEMO_TIMER DEMO_TIMER}</td>
 *         <td>Render after sleep overlay and before the demo timer, debug HUD, scoreboard, overlay message (action bar), and title and subtitle</td>
 *     </tr>
 *     <tr>
 *         <td>Before {@link VanillaHudElements#CHAT CHAT}</td>
 *         <td>Render after the debug HUD, scoreboard, overlay message (action bar), and title and subtitle and before {@link net.minecraft.client.gui.hud.ChatHud ChatHud}, player list, and sound subtitles</td>
 *     </tr>
 *     <tr>
 *         <td>After {@link VanillaHudElements#SUBTITLES SUBTITLES}</td>
 *         <td>Render after everything</td>
 *     </tr>
 * </table>
 */
public interface HudElementRegistry {
	/**
	 * Adds an element to the front.
	 *
	 * @param element the element to add
	 */
	static void addFirst(Identifier id, HudElement element) {
		Objects.requireNonNull(id, "identifier");
		Objects.requireNonNull(element, "hudElement");
		HudElementRegistryImpl.addFirst(id, element);
	}

	/**
	 * Adds an element to the end.
	 *
	 * @param element the element to add
	 */
	static void addLast(Identifier id, HudElement element) {
		Objects.requireNonNull(id, "identifier");
		Objects.requireNonNull(element, "hudElement");
		HudElementRegistryImpl.addLast(id, element);
	}

	/**
	 * Attaches an element before the element with the specified identifier.
	 *
	 * <p>The render condition of the vanilla element being attached to, if any, also applies to the new element.
	 *
	 * @param beforeThis the identifier of the element to add the new element before
	 * @param identifier the identifier of the new element
	 * @param element    the element to add
	 */
	static void attachElementBefore(Identifier beforeThis, Identifier identifier, HudElement element) {
		Objects.requireNonNull(beforeThis, "beforeThis");
		Objects.requireNonNull(identifier, "identifier");
		Objects.requireNonNull(element, "hudElement");
		HudElementRegistryImpl.attachElementBefore(beforeThis, identifier, element);
	}

	/**
	 * Attaches an element after the element with the specified identifier.
	 *
	 * <p>The render condition of the vanilla element being attached to, if any, also applies to the new element.
	 *
	 * @param afterThis  the identifier of the element to add the new element after
	 * @param identifier the identifier of the new element
	 * @param element    the element to add
	 */
	static void attachElementAfter(Identifier afterThis, Identifier identifier, HudElement element) {
		Objects.requireNonNull(afterThis, "afterThis");
		Objects.requireNonNull(identifier, "identifier");
		Objects.requireNonNull(element, "hudElement");
		HudElementRegistryImpl.attachElementAfter(afterThis, identifier, element);
	}

	/**
	 * Removes an element with the specified identifier.
	 *
	 * @param identifier the identifier of the element to remove
	 */
	static void removeElement(Identifier identifier) {
		Objects.requireNonNull(identifier, "identifier");
		HudElementRegistryImpl.removeElement(identifier);
	}

	/**
	 * Replaces an element with the specified identifier, the element retains its original identifier.
	 *
	 * <p>The render condition of the vanilla element being replaced, if any, also applies to the new element.
	 *
	 * @param identifier the identifier of the element to replace
	 * @param replacer   a function that takes the old element and returns the new element
	 */
	static void replaceElement(Identifier identifier, Function<HudElement, HudElement> replacer) {
		Objects.requireNonNull(identifier, "identifier");
		Objects.requireNonNull(replacer, "replacer");
		HudElementRegistryImpl.replaceElement(identifier, replacer);
	}
}
