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

package net.fabricmc.fabric.api.client.rendering.v1.world;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;

import net.fabricmc.fabric.impl.client.rendering.world.WorldRendererHooks;

@ApiStatus.Experimental
public interface WorldEntitySubmitContext extends WorldRenderContext {
	/**
	 * Returns the {@code WorldEntitySubmitContext} for the given {@code WorldRenderer} instance, for use in cases where you
	 * have access to the world renderer but not the world render context. World render events always pass the world
	 * render context as a parameter, so always prefer to use that over this method.
	 *
	 * @param worldRenderer The world renderer
	 * @return The world render context for the world renderer
	 * @throws IllegalStateException If not currently rendering the world
	 */
	static WorldEntitySubmitContext getInstance(WorldRenderer worldRenderer) {
		Preconditions.checkNotNull(worldRenderer, "worldRenderer");
		return ((WorldRendererHooks) worldRenderer).fabric$getWorldRenderContext();
	}

	OrderedRenderCommandQueue commandQueue();
}
