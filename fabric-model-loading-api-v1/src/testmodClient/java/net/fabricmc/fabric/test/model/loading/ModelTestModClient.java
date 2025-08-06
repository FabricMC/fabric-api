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

package net.fabricmc.fabric.test.model.loading;

import java.util.function.Supplier;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.HorizontalConnectingBlock;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.DelegatingUnbakedModel;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

public class ModelTestModClient implements ClientModInitializer {
	public static final String ID = "fabric-model-loading-api-v1-testmod";

	public static final Identifier HALF_RED_SAND_MODEL_ID = id("half_red_sand");
	public static final Identifier GOLD_BLOCK_MODEL_ID = Identifier.ofVanilla("block/gold_block");
	public static final Identifier BROWN_GLAZED_TERRACOTTA_MODEL_ID = Identifier.ofVanilla("block/brown_glazed_terracotta");

	static class DownQuadRemovingModel extends ForwardingBakedModel {
		DownQuadRemovingModel(BakedModel model) {
			super(model);
		}

		@Override
		public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
			context.pushTransform(q -> q.cullFace() != Direction.DOWN);
			super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
			context.popTransform();
		}
	}

	@Override
	public void onInitializeClient() {
		ModelLoadingPlugin.register(pluginContext -> {
			pluginContext.addModels(HALF_RED_SAND_MODEL_ID);
			// remove bottom face of gold blocks
			pluginContext.modifyModelAfterBake().register(ModelModifier.WRAP_PHASE, (model, context) -> {
				Identifier id = context.resourceId();

				if (id != null && id.equals(GOLD_BLOCK_MODEL_ID)) {
					return new DownQuadRemovingModel(model);
				}

				return model;
			});
			// make fences with west: true and everything else false appear to be a missing model visually
			ModelIdentifier fenceId = BlockModels.getModelId(Blocks.OAK_FENCE.getDefaultState().with(HorizontalConnectingBlock.WEST, true));
			pluginContext.modifyModelOnLoad().register(ModelModifier.OVERRIDE_PHASE, (model, context) -> {
				ModelIdentifier id = context.topLevelId();

				if (id != null && id.equals(fenceId)) {
					return context.getOrLoadModel(ModelLoader.MISSING_ID);
				}

				return model;
			});
			// make brown glazed terracotta appear to be a missing model visually, but without affecting the item, by using pre-bake
			// using load here would make the item also appear missing
			pluginContext.modifyModelBeforeBake().register(ModelModifier.OVERRIDE_PHASE, (model, context) -> {
				Identifier id = context.resourceId();

				if (id != null && id.equals(BROWN_GLAZED_TERRACOTTA_MODEL_ID)) {
					return context.baker().getOrLoadModel(ModelLoader.MISSING_ID);
				}

				return model;
			});

			// Make wheat stages 1->6 use the same model as stage 0. This can be done with resource packs, this is just a test.
			pluginContext.registerBlockStateResolver(Blocks.WHEAT, context -> {
				BlockState state = context.block().getDefaultState();

				// All the block state models are top-level...
				// Use a delegating unbaked model to make sure the identical models only get baked a single time.
				Identifier wheatStage0Id = Identifier.ofVanilla("block/wheat_stage0");

				UnbakedModel stage0Model = new DelegatingUnbakedModel(wheatStage0Id);

				for (int age = 0; age <= 6; age++) {
					context.setModel(state.with(CropBlock.AGE, age), stage0Model);
				}

				context.setModel(state.with(CropBlock.AGE, 7), context.getOrLoadModel(Identifier.ofVanilla("block/wheat_stage7")));
			});
		});

		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(SpecificModelReloadListener.INSTANCE);

		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityRenderer instanceof PlayerEntityRenderer playerRenderer) {
				registrationHelper.register(new BakedModelFeatureRenderer<>(playerRenderer, SpecificModelReloadListener.INSTANCE::getSpecificModel));
			}
		});
	}

	public static Identifier id(String path) {
		return Identifier.of(ID, path);
	}
}
