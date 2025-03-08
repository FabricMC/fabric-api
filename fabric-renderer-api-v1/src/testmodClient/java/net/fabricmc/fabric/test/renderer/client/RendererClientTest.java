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

package net.fabricmc.fabric.test.renderer.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.renderer.v1.material.ShadeMode;
import net.fabricmc.fabric.test.renderer.OctagonalColumnBlock;
import net.fabricmc.fabric.test.renderer.Registration;

public final class RendererClientTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLoadingPlugin.register(pluginContext -> {
			pluginContext.registerBlockStateResolver(Registration.FRAME_BLOCK, ctx -> {
				ctx.setModel(ctx.block().getDefaultState(), new FrameUnbakedBlockStateModel().cached());
			});

			pluginContext.registerBlockStateResolver(Registration.PILLAR_BLOCK, ctx -> {
				ctx.setModel(ctx.block().getDefaultState(), new PillarUnbakedBlockStateModel().cached());
			});

			pluginContext.registerBlockStateResolver(Registration.OCTAGONAL_COLUMN_BLOCK, ctx -> {
				BlockState state = ctx.block().getDefaultState();
				ctx.setModel(state.with(OctagonalColumnBlock.VANILLA_SHADE_MODE, false), new OctagonalColumnUnbakedBlockStateModel(ShadeMode.ENHANCED).cached());
				ctx.setModel(state.with(OctagonalColumnBlock.VANILLA_SHADE_MODE, true), new OctagonalColumnUnbakedBlockStateModel(ShadeMode.VANILLA).cached());
			});

			pluginContext.registerBlockStateResolver(Registration.RIVERSTONE_BLOCK, ctx -> {
				ctx.setModel(ctx.block().getDefaultState(), new RiverstoneUnbakedBlockStateModel().cached());
			});
		});

		// We don't specify a material for the frame mesh,
		// so it will use the default material, i.e. the one from BlockRenderLayerMap.
		BlockRenderLayerMap.INSTANCE.putBlock(Registration.FRAME_BLOCK, RenderLayer.getCutoutMipped());
	}
}
