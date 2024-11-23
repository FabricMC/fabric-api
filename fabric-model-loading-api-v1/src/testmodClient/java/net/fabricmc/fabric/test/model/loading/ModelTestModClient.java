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

import java.util.List;

import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.HorizontalConnectingBlock;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.model.GroupableModel;
import net.minecraft.client.render.model.MissingModel;
import net.minecraft.client.render.model.json.ModelVariant;
import net.minecraft.client.render.model.json.WeightedUnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.AffineTransformation;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.DelegatingUnbakedModel;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

public class ModelTestModClient implements ClientModInitializer {
	public static final String ID = "fabric-model-loading-api-v1-testmod";

	public static final Identifier HALF_RED_SAND_MODEL_ID = id("half_red_sand");
	public static final Identifier GOLD_BLOCK_MODEL_ID = Identifier.ofVanilla("block/gold_block");
	public static final Identifier BROWN_GLAZED_TERRACOTTA_MODEL_ID = Identifier.ofVanilla("block/brown_glazed_terracotta");

	//static class DownQuadRemovingModel extends ForwardingBakedModel {
	//	DownQuadRemovingModel(BakedModel model) {
	//		wrapped = model;
	//	}
	//
	//	@Override
	//	public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
	//		context.pushTransform(q -> q.cullFace() != Direction.DOWN);
	//		super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
	//		context.popTransform();
	//	}
	//}

	@Override
	public void onInitializeClient() {
		ModelLoadingPlugin.register(pluginContext -> {
			pluginContext.addModels(HALF_RED_SAND_MODEL_ID);

			// replace the brown glazed terracotta model with a missing model
			// with the new item model system in 1.21.4, this also affects the item model
			pluginContext.modifyModelBeforeBake().register(ModelModifier.OVERRIDE_PHASE, (model, context) -> {
				if (context.id().equals(BROWN_GLAZED_TERRACOTTA_MODEL_ID)) {
					return new DelegatingUnbakedModel(MissingModel.ID);
				}

				return model;
			});

			// make fences with west: true and everything else false appear to be a missing model visually
			ModelIdentifier fenceId = BlockModels.getModelId(Blocks.OAK_FENCE.getDefaultState().with(HorizontalConnectingBlock.WEST, true));
			pluginContext.modifyBlockModelBeforeBake().register(ModelModifier.OVERRIDE_PHASE, (model, context) -> {
				if (context.id().equals(fenceId)) {
					return simpleGroupableModel(MissingModel.ID);
				}

				return model;
			});

			// Make wheat stages 1->6 use the same model as stage 0. This can be done with resource packs, this is just a test.
			Identifier wheatId = Registries.BLOCK.getId(Blocks.WHEAT);
			String wheatStage7Variant = BlockModels.propertyMapToString(Blocks.WHEAT.getDefaultState().with(CropBlock.AGE, 7).getEntries());
			GroupableModel wheatStage0Model = simpleGroupableModel(Identifier.ofVanilla("block/wheat_stage0"));
			GroupableModel wheatStage7Model = simpleGroupableModel(Identifier.ofVanilla("block/wheat_stage7"));
			pluginContext.modifyBlockModelBeforeBake().register(ModelModifier.OVERRIDE_PHASE, (model, context) -> {
				if (!context.id().id().equals(wheatId)) {
					return model;
				}

				if (context.id().variant().equals(wheatStage7Variant)) {
					return wheatStage7Model;
				} else {
					return wheatStage0Model;
				}
			});

			// TODO 1.21.4: reintroduce test once FRAPI+Indigo are ported
			// remove bottom face of gold blocks
			//pluginContext.modifyModelAfterBake().register(ModelModifier.WRAP_PHASE, (model, context) -> {
			//	if (context.id().equals(GOLD_BLOCK_MODEL_ID)) {
			//		return new DownQuadRemovingModel(model);
			//	}
			//
			//	return model;
			//});
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

	private static GroupableModel simpleGroupableModel(Identifier model) {
		return new WeightedUnbakedModel(List.of(new ModelVariant(model, AffineTransformation.identity(), false, 1)));
	}
}
