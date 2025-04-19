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

import net.minecraft.client.render.RenderLayer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.fabricmc.fabric.test.renderer.Registration;
import net.fabricmc.fabric.test.renderer.RendererTest;

public final class RendererClientTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		UnbakedModelDeserializer.register(RendererTest.id("builtin_mesh"), new BuiltInMeshUnbakedModelDeserializer());

		CustomUnbakedBlockStateModel.register(RendererTest.id("biome_dependent"), BiomeDependentBlockStateModel.Unbaked.CODEC);
		CustomUnbakedBlockStateModel.register(RendererTest.id("frame"), FrameBlockStateModel.Unbaked.CODEC);
		CustomUnbakedBlockStateModel.register(RendererTest.id("pillar"), PillarBlockStateModel.Unbaked.CODEC);

		// We don't specify a material for the frame mesh,
		// so it will use the default material, i.e. the one from BlockRenderLayerMap.
		BlockRenderLayerMap.INSTANCE.putBlock(Registration.FRAME_BLOCK, RenderLayer.getCutoutMipped());
	}
}
