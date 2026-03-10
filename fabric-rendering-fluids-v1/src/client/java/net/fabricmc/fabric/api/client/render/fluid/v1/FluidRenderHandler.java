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

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderingImpl;

/**
 * Interface for handling the rendering of a FluidState.
 */
public interface FluidRenderHandler {
	/**
	 * Tessellate your fluid. By default, this method will call the default
	 * fluid renderer. Call {@code FluidRenderHandler.super.renderFluid} if
	 * you want to render over the default fluid renderer. This is the
	 * intended way to render default geometry; calling
	 * {@link FluidRenderer#tesselate} is not supported. When rendering default
	 * geometry, the current handler will be used instead of looking up
	 * a new one for the passed fluid state.
	 *
	 * @param pos The position in the level, of the fluid to render.
	 * @param level The level the fluid is in
	 * @param output The {@link FluidRenderer.Output} used to get the {@link VertexConsumer} to render to.
	 * @param blockState The block state being rendered.
	 * @param fluidState The fluid state being rendered.
	 */
	default void renderFluid(FluidRenderer fluidRenderer, BlockPos pos, BlockAndTintGetter level, FluidRenderer.Output output, BlockState blockState, FluidState fluidState) {
		FluidRenderingImpl.renderDefault(fluidRenderer, this, level, pos, output, blockState, fluidState);
	}
}
