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

import java.util.function.BooleanSupplier;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;

/**
 * A layer that wraps another layered drawer that can be added to {@link net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper LayeredDrawerWrapper}.
 *
 * <p>This implementation is currently not used, as vanilla sub drawers are flattened, but exists for completeness and use if needed.
 *
 * @param id           the identifier of the layer
 * @param delegate     the layered drawer to wrap
 * @param shouldRender a boolean supplier that determines if the layer should render
 */
public record SubLayer(Identifier id, LayeredDrawer delegate, BooleanSupplier shouldRender) implements IdentifiedLayer {
	@Override
	public void render(DrawContext context, RenderTickCounter tickCounter) {
		if (shouldRender.getAsBoolean()) {
			delegate.render(context, tickCounter);
		}
	}
}
