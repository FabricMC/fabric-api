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

import java.util.function.Function;

import org.jetbrains.annotations.Contract;

import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.util.Identifier;

/**
 * A layered drawer that has an identifier attached to each layer and methods to add layers in specific positions.
 *
 * <p>Operations relative to a layer will generally inherit that layer's render condition.
 * The render condition for all vanilla layers except {@link IdentifiedLayer#SLEEP} is {@link net.minecraft.client.option.GameOptions#hudHidden}.
 * Only {@link #addLayer(IdentifiedLayer)} will not inherit any render condition.
 * There is currently no mechanism to change the render condition of a layer.
 * For vanilla layers, see {@link IdentifiedLayer}.
 *
 * <p>Common places to add layers (as of 1.21.4):
 * <table>
 *     <tr>
 *         <th>Injection Point</th>
 *         <th>Use Case</th>
 *     </tr>
 *     <tr>
 *         <td>Before {@link IdentifiedLayer#MISC_OVERLAYS MISC_OVERLAYS}</td>
 *         <td>Render before everything</td>
 *     </tr>
 *     <tr>
 *         <td>After {@link IdentifiedLayer#MISC_OVERLAYS MISC_OVERLAYS}</td>
 *         <td>Render after misc overlays (vignette, spyglass, and powder snow) and before the crosshair</td>
 *     </tr>
 *     <tr>
 *         <td>After {@link IdentifiedLayer#EXPERIENCE_LEVEL EXPERIENCE_LEVEL}</td>
 *         <td>Render after most main hud elements like hotbar, spectator hud, status bars, experience bar, status effects overlays, and boss bar and before the sleep overlay</td>
 *     </tr>
 *     <tr>
 *         <td>Before {@link IdentifiedLayer#DEMO_TIMER DEMO_TIMER}</td>
 *         <td>Render after sleep overlay and before the demo timer, debug HUD, scoreboard, overlay message (action bar), and title and subtitle</td>
 *     </tr>
 *     <tr>
 *         <td>Before {@link IdentifiedLayer#CHAT CHAT}</td>
 *         <td>Render after the debug HUD, scoreboard, overlay message (action bar), and title and subtitle and before {@link net.minecraft.client.gui.hud.ChatHud ChatHud}, player list, and sound subtitles</td>
 *     </tr>
 *     <tr>
 *         <td>After {@link IdentifiedLayer#SUBTITLES SUBTITLES}</td>
 *         <td>Render after everything</td>
 *     </tr>
 * </table>
 *
 * @see HudLayerRegistrationCallback
 */
public interface LayeredDrawerWrapper {
	/**
	 * Adds a layer to the end of the layered drawer.
	 *
	 * @param layer the layer to add
	 * @return this layered drawer
	 */
	@Contract("_ -> this")
	LayeredDrawerWrapper addLayer(IdentifiedLayer layer);

	/**
	 * Attaches a layer before the layer with the specified identifier.
	 *
	 * <p>The render condition of the layer being attached to, if any, also applies to the new layer.
	 *
	 * @param beforeThis the identifier of the layer to add the new layer before
	 * @param layer      the layer to add
	 * @return this layered drawer
	 */
	@Contract("_, _ -> this")
	LayeredDrawerWrapper attachLayerBefore(Identifier beforeThis, IdentifiedLayer layer);

	/**
	 * Attaches a layer before the layer with the specified identifier.
	 *
	 * <p>The render condition of the layer being attached to, if any, also applies to the new layer.
	 *
	 * @param beforeThis the identifier of the layer to add the new layer before
	 * @param identifier the identifier of the new layer
	 * @param layer      the layer to add
	 * @return this layered drawer
	 */
	@Contract("_, _, _ -> this")
	default LayeredDrawerWrapper attachLayerBefore(Identifier beforeThis, Identifier identifier, LayeredDrawer.Layer layer) {
		return attachLayerBefore(beforeThis, IdentifiedLayer.of(identifier, layer));
	}

	/**
	 * Attaches a layer after the layer with the specified identifier.
	 *
	 * <p>The render condition of the layer being attached to, if any, also applies to the new layer.
	 *
	 * @param afterThis the identifier of the layer to add the new layer after
	 * @param layer     the layer to add
	 * @return this layered drawer
	 */
	@Contract("_, _ -> this")
	LayeredDrawerWrapper attachLayerAfter(Identifier afterThis, IdentifiedLayer layer);

	/**
	 * Attaches a layer after the layer with the specified identifier.
	 *
	 * <p>The render condition of the layer being attached to, if any, also applies to the new layer.
	 *
	 * @param afterThis  the identifier of the layer to add the new layer after
	 * @param identifier the identifier of the new layer
	 * @param layer      the layer to add
	 * @return this layered drawer
	 */
	@Contract("_, _, _ -> this")
	default LayeredDrawerWrapper attachLayerAfter(Identifier afterThis, Identifier identifier, LayeredDrawer.Layer layer) {
		return attachLayerAfter(afterThis, IdentifiedLayer.of(identifier, layer));
	}

	/**
	 * Removes a layer with the specified identifier.
	 *
	 * @param identifier the identifier of the layer to remove
	 * @return this layered drawer
	 */
	@Contract("_ -> this")
	LayeredDrawerWrapper removeLayer(Identifier identifier);

	/**
	 * Replaces a layer with the specified identifier.
	 *
	 * <p>The render condition of the layer being replaced, if any, also applies to the new layer.
	 *
	 * @param identifier the identifier of the layer to replace
	 * @param replacer   a function that takes the old layer and returns the new layer
	 * @return this layered drawer
	 */
	@Contract("_, _ -> this")
	LayeredDrawerWrapper replaceLayer(Identifier identifier, Function<IdentifiedLayer, IdentifiedLayer> replacer);
}
