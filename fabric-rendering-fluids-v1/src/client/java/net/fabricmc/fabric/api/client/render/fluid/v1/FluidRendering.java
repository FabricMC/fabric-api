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

package net.fabricmc.fabric.api.client.render.fluid.v1;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderingImpl;

/**
 * A class containing some utilities for rendering fluids.
 */
public final class FluidRendering {
	private FluidRendering() {
	}

	/**
	 * Renders a fluid using the given handler, default renderer, and context. Internally, this just invokes
	 * {@link FluidRenderHandler#renderFluid}, but the passed default renderer is invoked instead of the vanilla
	 * renderer whenever the handler requests default geometry to be rendered.
	 *
	 * @param handler the render handler to invoke {@link FluidRenderHandler#renderFluid} on
	 * @param world the world
	 * @param pos the pos
	 * @param vertexConsumer the vertex consumer
	 * @param blockState the block state
	 * @param fluidState the fluid state
	 * @param defaultRenderer the renderer to use whenever the handler requests default geometry
	 */
	public static void render(FluidRenderHandler handler, BlockAndTintGetter world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, DefaultRenderer defaultRenderer) {
		FluidRenderingImpl.render(handler, world, pos, vertexConsumer, blockState, fluidState, defaultRenderer);
	}

	public interface DefaultRenderer {
		/**
		 * Render the default geometry when it is requested by {@link FluidRenderHandler#renderFluid}. The default
		 * implementation invokes the vanilla renderer. Calling {@link LiquidBlockRenderer#tesselate} directly is not supported
		 * but using {@code DefaultRenderer.super.render} is supported. Note that the parameter values passed to this
		 * call are provided by the render handler, meaning they are not necessarily the same as those provided to the
		 * initial rendering call. As per the documentation of {@link FluidRenderHandler#renderFluid}, a new handler
		 * should not be retrieved and only the passed one should be used.
		 *
		 * @param handler the handler that {@link FluidRenderHandler#renderFluid} was invoked on
		 * @param world the world
		 * @param pos the pos
		 * @param vertexConsumer the vertex consumer
		 * @param blockState the block state
		 * @param fluidState the fluid state
		 */
		default void render(FluidRenderHandler handler, BlockAndTintGetter world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
			FluidRenderingImpl.renderVanillaDefault(handler, world, pos, vertexConsumer, blockState, fluidState);
		}
	}
}
