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

import java.util.function.Function;

import com.google.common.base.Preconditions;

import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.client.rendering.hud.HudElementRegistryImpl;

/**
 * A registry of identified hud elements with methods to add elements in specific positions.
 *
 * <p>Operations relative to a vanilla element will inherit that element's render condition.
 * The render condition for all vanilla elements except {@link IdentifiedElement#SLEEP} is {@link net.minecraft.client.option.GameOptions#hudHidden}.
 * Only {@link #addFirst(IdentifiedElement)} and {@link #addLast(IdentifiedElement)} will not inherit any render condition.
 * There is currently no mechanism to change the render condition of a vanilla element.
 * For vanilla elements, see {@link IdentifiedElement}.
 *
 * <p>Common places to add elements (as of 1.21.6):
 * <table>
 *     <tr>
 *         <th>Injection Point</th>
 *         <th>Use Case</th>
 *     </tr>
 *     <tr>
 *         <td>Before {@link IdentifiedElement#MISC_OVERLAYS MISC_OVERLAYS}</td>
 *         <td>Render before everything</td>
 *     </tr>
 *     <tr>
 *         <td>After {@link IdentifiedElement#MISC_OVERLAYS MISC_OVERLAYS}</td>
 *         <td>Render after misc overlays (vignette, spyglass, and powder snow) and before the crosshair</td>
 *     </tr>
 *     <tr>
 *         <td>After {@link IdentifiedElement#HOTBAR_AND_BARS HOTBAR_AND_BARS}</td>
 *         <td>Render after most main hud elements like hotbar, spectator hud, status bars, experience bar, status effects overlays, and boss bar and before the sleep overlay</td>
 *     </tr>
 *     <tr>
 *         <td>Before {@link IdentifiedElement#DEMO_TIMER DEMO_TIMER}</td>
 *         <td>Render after sleep overlay and before the demo timer, debug HUD, scoreboard, overlay message (action bar), and title and subtitle</td>
 *     </tr>
 *     <tr>
 *         <td>Before {@link IdentifiedElement#CHAT CHAT}</td>
 *         <td>Render after the debug HUD, scoreboard, overlay message (action bar), and title and subtitle and before {@link net.minecraft.client.gui.hud.ChatHud ChatHud}, player list, and sound subtitles</td>
 *     </tr>
 *     <tr>
 *         <td>After {@link IdentifiedElement#SUBTITLES SUBTITLES}</td>
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
	static void addFirst(IdentifiedElement element) {
		Preconditions.checkNotNull(element, "hudElement");
		HudElementRegistryImpl.addFirst(element);
	}

	/**
	 * Adds an element to the end.
	 *
	 * @param element the element to add
	 */
	static void addLast(IdentifiedElement element) {
		Preconditions.checkNotNull(element, "hudElement");
		HudElementRegistryImpl.addLast(element);
	}

	/**
	 * Attaches an element before the element with the specified identifier.
	 *
	 * <p>The render condition of the vanilla element being attached to, if any, also applies to the new element.
	 *
	 * @param beforeThis the identifier of the element to add the new element before
	 * @param element    the element to add
	 */
	static void attachElementBefore(Identifier beforeThis, IdentifiedElement element) {
		Preconditions.checkNotNull(beforeThis, "beforeThis");
		Preconditions.checkNotNull(element, "hudElement");
		HudElementRegistryImpl.attachElementBefore(beforeThis, element);
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
		Preconditions.checkNotNull(beforeThis, "beforeThis");
		Preconditions.checkNotNull(identifier, "identifier");
		Preconditions.checkNotNull(element, "hudElement");
		HudElementRegistryImpl.attachElementBefore(beforeThis, IdentifiedElement.of(identifier, element));
	}

	/**
	 * Attaches an element after the element with the specified identifier.
	 *
	 * <p>The render condition of the vanilla element being attached to, if any, also applies to the new element.
	 *
	 * @param afterThis the identifier of the element to add the new element after
	 * @param element   the element to add
	 */
	static void attachElementAfter(Identifier afterThis, IdentifiedElement element) {
		Preconditions.checkNotNull(afterThis, "afterThis");
		Preconditions.checkNotNull(element, "hudElement");
		HudElementRegistryImpl.attachElementAfter(afterThis, element);
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
		Preconditions.checkNotNull(afterThis, "afterThis");
		Preconditions.checkNotNull(identifier, "identifier");
		Preconditions.checkNotNull(element, "hudElement");
		HudElementRegistryImpl.attachElementAfter(afterThis, IdentifiedElement.of(identifier, element));
	}

	/**
	 * Removes an element with the specified identifier.
	 *
	 * @param identifier the identifier of the element to remove
	 */
	static void removeElement(Identifier identifier) {
		Preconditions.checkNotNull(identifier, "identifier");
		HudElementRegistryImpl.removeElement(identifier);
	}

	/**
	 * Replaces an element with the specified identifier.
	 *
	 * <p>The render condition of the vanilla element being replaced, if any, also applies to the new element.
	 *
	 * @param identifier the identifier of the element to replace
	 * @param replacer   a function that takes the old element and returns the new element
	 */
	static void replaceElement(Identifier identifier, Function<IdentifiedElement, IdentifiedElement> replacer) {
		Preconditions.checkNotNull(identifier, "identifier");
		Preconditions.checkNotNull(replacer, "replacer");
		HudElementRegistryImpl.replaceElement(identifier, replacer);
	}
}
