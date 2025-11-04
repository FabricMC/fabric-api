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

package net.fabricmc.fabric.impl.client.rendering.fluid;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRendering;

public class FluidRenderingImpl {
	private static final ThreadLocal<FluidRendering.DefaultRenderer> CURRENT_DEFAULT_RENDERER = new ThreadLocal<>();
	private static final ThreadLocal<FluidRenderHandlerInfo> CURRENT_INFO = ThreadLocal.withInitial(FluidRenderHandlerInfo::new);
	private static LiquidBlockRenderer vanillaRenderer;

	// Only invoked manually from FluidRendering#render
	public static void render(FluidRenderHandler handler, BlockAndTintGetter world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, FluidRendering.DefaultRenderer defaultRenderer) {
		CURRENT_DEFAULT_RENDERER.set(defaultRenderer);

		try {
			handler.renderFluid(pos, world, vertexConsumer, blockState, fluidState);
		} finally {
			CURRENT_DEFAULT_RENDERER.remove();
		}
	}

	// Only invoked when FluidRenderHandler#renderFluid calls super
	public static void renderDefault(FluidRenderHandler handler, BlockAndTintGetter world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
		FluidRendering.DefaultRenderer renderer = CURRENT_DEFAULT_RENDERER.get();

		if (renderer != null) {
			renderer.render(handler, world, pos, vertexConsumer, blockState, fluidState);
		} else {
			renderVanillaDefault(handler, world, pos, vertexConsumer, blockState, fluidState);
		}
	}

	// Invoked when FluidRenderHandler#renderFluid is called directly without using FluidRendering#render (such as
	// from vanilla FluidRenderer#render via mixin) or from the default implementation of DefaultRenderer#render
	public static void renderVanillaDefault(FluidRenderHandler handler, BlockAndTintGetter world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
		FluidRenderHandlerInfo info = CURRENT_INFO.get();
		info.setup(handler, world, pos, fluidState);

		try {
			vanillaRenderer.tesselate(world, pos, vertexConsumer, blockState, fluidState);
		} finally {
			info.clear();
		}
	}

	public static void setVanillaRenderer(LiquidBlockRenderer vanillaRenderer) {
		FluidRenderingImpl.vanillaRenderer = vanillaRenderer;
	}

	public static FluidRenderHandlerInfo getCurrentInfo() {
		return CURRENT_INFO.get();
	}
}
