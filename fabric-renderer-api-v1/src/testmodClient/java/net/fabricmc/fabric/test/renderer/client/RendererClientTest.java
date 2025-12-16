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

import java.util.Map;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.test.renderer.ItemWithBlock;
import net.fabricmc.fabric.test.renderer.Registration;
import net.fabricmc.fabric.test.renderer.RendererTest;

public final class RendererClientTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		UnbakedModelDeserializer.register(RendererTest.id("builtin_mesh"), new BuiltInMeshUnbakedModelDeserializer());

		CustomUnbakedBlockStateModel.register(RendererTest.id("biome_dependent"), BiomeDependentBlockStateModel.Unbaked.CODEC);
		CustomUnbakedBlockStateModel.register(RendererTest.id("frame"), FrameBlockStateModel.Unbaked.CODEC);
		CustomUnbakedBlockStateModel.register(RendererTest.id("pillar"), PillarBlockStateModel.Unbaked.CODEC);

		ModelLoadingPlugin.register(pluginContext -> {
			pluginContext.modifyItemModelAfterBake().register((model, context) -> {
				if (BuiltInRegistries.ITEM.getValue(context.itemId()) instanceof ItemWithBlock) {
					// Defer the model lookup
					return (
							itemStackRenderState,
							itemStack,
							itemModelResolver,
							itemDisplayContext,
							clientLevel,
							itemOwner,
							i
					) -> {
						// fixme: why is the injected interface not applying?
						ItemModel wrapped = ((FabricBakedModelManager) Minecraft.getInstance()
								.getModelManager())
								.getModel(ItemWithBlockModel.MODEL_KEYS.get(context.itemId()));
						Objects.requireNonNull(wrapped);
						wrapped.update(
								itemStackRenderState,
								itemStack,
								itemModelResolver,
								itemDisplayContext,
								clientLevel,
								itemOwner,
								i
						);
					};
				}

				return model;
			});

			for (Map.Entry<Identifier, ItemWithBlock> entry : ItemWithBlock.LOOKUP.entrySet()) {
				ItemWithBlock item = entry.getValue();
				ExtraModelKey<ItemModel> extraModelKey = ExtraModelKey.create(() -> entry.getKey()
						.toString());
				ItemWithBlockModel.MODEL_KEYS.put(entry.getKey(), extraModelKey);
				pluginContext.addModel(
						extraModelKey,
						new ItemWithBlockModel.UnbakedExtra(
								entry.getKey(),
								item.getItem(),
								item.getBlock()
						)
				);
			}
		});

		// We don't specify a material for the frame mesh,
		// so it will use the default material, i.e. the one from RenderLayers.
		BlockRenderLayerMap.putBlock(Registration.FRAME_BLOCK, ChunkSectionLayer.CUTOUT);
	}
}
