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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.rendering.v1.ChunkSectionLayerMap;
import net.fabricmc.fabric.impl.client.renderer.RendererManager;
import net.fabricmc.fabric.test.renderer.Registration;
import net.fabricmc.fabric.test.renderer.RendererTest;

public final class RendererClientTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		UnbakedModelDeserializer.register(RendererTest.id("builtin_mesh"), new BuiltInMeshUnbakedModelDeserializer());
		UnbakedModelDeserializer.register(RendererTest.id("overlayed"), new OverlayedUnbakedModelDeserializer());

		CustomUnbakedBlockStateModel.register(RendererTest.id("biome_dependent"), BiomeDependentBlockStateModel.Unbaked.CODEC);
		CustomUnbakedBlockStateModel.register(RendererTest.id("frame"), FrameBlockStateModel.Unbaked.CODEC);
		CustomUnbakedBlockStateModel.register(RendererTest.id("pillar"), PillarBlockStateModel.Unbaked.CODEC);

		// We don't specify a material for the frame mesh,
		// so it will use the default material, i.e. the one from ChunkSectionLayers.
		ChunkSectionLayerMap.putBlock(Registration.FRAME_BLOCK, ChunkSectionLayer.CUTOUT);

		try {
			// if it crashes, that means the ordering is #*@!ed up.
			Renderer.get(); // Ensure Renderer can be initialized as early as mod init

			// Print the ordering
			StringBuilder stringBuilder = new StringBuilder("Renderer ordering: ");

			for (int i = 0; i < RendererManager.nodes.size(); i++) {
				RendererManager.RendererProviderNode node = RendererManager.nodes.get(i);

				if (i > 0) {
					stringBuilder.append(", ");
				}

				stringBuilder.append(node.id);
			}

			LoggerFactory.getLogger(RendererClientTest.class).info(stringBuilder.toString());

			List<String> overrides = new ArrayList<>();

			for (RendererManager.RendererProviderNode node : RendererManager.nodes) {
				overrides.add(node.id);
			}

			if (!overrides.equals(List.of("fabric-renderer-indigo", "g", "i", "j", "h"))) {
				throw new IllegalStateException("RendererProvider overrides were not ordered correctly");
			}
		} catch (Exception e) {
			throw new RuntimeException("Renderer failed to initialize", e);
		}
	}
}
