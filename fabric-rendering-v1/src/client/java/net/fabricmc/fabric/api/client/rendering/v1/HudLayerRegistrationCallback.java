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

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Callback for when hud layers are registered.
 *
 * <p>To register a layer, register a listener to this event and register your layers in the listener.
 * For common use cases, see {@link LayeredDrawerWrapper}.
 *
 * <p>For example, the following code registers a layer after {@link IdentifiedLayer#MISC_OVERLAYS}:
 * {@snippet :
 * // @link region substring=HudLayerRegistrationCallback target=HudLayerRegistrationCallback
 * // @link region substring=EVENT target="HudLayerRegistrationCallback#EVENT"
 * // @link region substring=layeredDrawer target="LayeredDrawerWrapper"
 * // @link region substring=attachLayerAfter target="LayeredDrawerWrapper#attachLayerAfter"
 * // @link region substring=IdentifiedLayer target=IdentifiedLayer
 * // @link region substring=MISC_OVERLAYS target="IdentifiedLayer#MISC_OVERLAYS"
 * // @link region substring=Identifier target="net.minecraft.util.Identifier"
 * // @link region substring=of target="net.minecraft.util.Identifier#of"
 * // @link region substring=context target="net.minecraft.client.gui.DrawContext"
 * // @link region substring=tickCounter target="net.minecraft.client.render.RenderTickCounter"
 * HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerAfter(IdentifiedLayer.MISC_OVERLAYS, Identifier.of("example", "example_layer_after_misc_overlays"), (context, tickCounter) -> {
 *     // Your rendering code here
 * }));
 * // @end @end @end @end @end @end @end @end @end @end
 * }
 *
 * @see LayeredDrawerWrapper
 */
public interface HudLayerRegistrationCallback {
	Event<HudLayerRegistrationCallback> EVENT = EventFactory.createArrayBacked(HudLayerRegistrationCallback.class, callbacks -> layeredDrawer -> {
		for (HudLayerRegistrationCallback callback : callbacks) {
			callback.register(layeredDrawer);
		}
	});

	/**
	 * Called when registering hud layers.
	 *
	 * @param layeredDrawer the layered drawer to register layers to
	 * @see LayeredDrawerWrapper
	 */
	void register(LayeredDrawerWrapper layeredDrawer);
}
