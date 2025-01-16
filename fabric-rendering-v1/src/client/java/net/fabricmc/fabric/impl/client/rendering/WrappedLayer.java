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

package net.fabricmc.fabric.impl.client.rendering;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;

/**
 * A simple layer that wraps a {@link LayeredDrawer.Layer} that can be added to {@link net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper LayeredDrawerWrapper}.
 *
 * @param id    the identifier of the layer
 * @param layer the layer to wrap
 */
public record WrappedLayer(Identifier id, LayeredDrawer.Layer layer) implements IdentifiedLayer {
	@Override
	public void render(DrawContext context, RenderTickCounter tickCounter) {
		layer.render(context, tickCounter);
	}
}
