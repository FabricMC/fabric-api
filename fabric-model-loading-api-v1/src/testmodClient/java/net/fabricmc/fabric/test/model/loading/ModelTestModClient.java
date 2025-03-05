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

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.HorizontalConnectingBlock;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.model.MissingModel;
import net.minecraft.client.render.model.SimpleBlockStateModel;
import net.minecraft.client.render.model.json.ModelVariant;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

public class ModelTestModClient implements ClientModInitializer {
	public static final String ID = "fabric-model-loading-api-v1-testmod";

	public static final Identifier HALF_RED_SAND_MODEL_ID = id("half_red_sand");
	public static final Identifier BROWN_GLAZED_TERRACOTTA_MODEL_ID = Identifier.ofVanilla("block/brown_glazed_terracotta");
	public static final Identifier GOLD_BLOCK_MODEL_ID = Identifier.ofVanilla("block/gold_block");

	@Override
	public void onInitializeClient() {
		ModelLoadingPlugin.register(pluginContext -> {
			//pluginContext.addModels(HALF_RED_SAND_MODEL_ID);

			// Make wheat stages 1->6 use the same model as stage 0. This can be done with resource packs, this is just a test.
			pluginContext.registerBlockStateResolver(Blocks.WHEAT, context -> {
				BlockState state = context.block().getDefaultState();

				Identifier wheatStage0Id = Identifier.ofVanilla("block/wheat_stage0");
				Identifier wheatStage7Id = Identifier.ofVanilla("block/wheat_stage7");
				BlockStateModel.UnbakedGrouped wheatStage0Model = simpleUnbakedGroupedBlockStateModel(wheatStage0Id);
				BlockStateModel.UnbakedGrouped wheatStage7Model = simpleUnbakedGroupedBlockStateModel(wheatStage7Id);

				for (int age = 0; age <= 6; age++) {
					context.setModel(state.with(CropBlock.AGE, age), wheatStage0Model);
				}

				context.setModel(state.with(CropBlock.AGE, 7), wheatStage7Model);
			});

			// FIXME
			// Replace the brown glazed terracotta model with a missing model without affecting child models.
			// Since 1.21.4, the item model is not a child model, so it is also affected.
			//pluginContext.modifyModelOnLoad().register(ModelModifier.WRAP_PHASE, (model, context) -> {
			//	if (context.id().equals(BROWN_GLAZED_TERRACOTTA_MODEL_ID)) {
			//		return new WrapperUnbakedModel(model) {
			//			@Override
			//			public void resolve(Resolver resolver) {
			//				super.resolve(resolver);
			//				resolver.resolve(MissingModel.ID);
			//			}
			//
			//			@Override
			//			public BakedModel bake(ModelTextures textures, Baker baker, ModelBakeSettings settings, boolean ambientOcclusion, boolean isSideLit, ModelTransformation transformation) {
			//				return baker.bake(MissingModel.ID, settings);
			//			}
			//		};
			//	}
			//
			//	return model;
			//});

			// Make oak fences with west: true and everything else false appear to be a missing model visually.
			BlockState westOakFence = Blocks.OAK_FENCE.getDefaultState().with(HorizontalConnectingBlock.WEST, true);
			pluginContext.modifyBlockModelOnLoad().register(ModelModifier.OVERRIDE_PHASE, (model, context) -> {
				if (context.state() == westOakFence) {
					return simpleUnbakedGroupedBlockStateModel(MissingModel.ID);
				}

				return model;
			});

			// FIXME
			// Remove bottom face of gold blocks
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
				//registrationHelper.register(new BakedModelFeatureRenderer<>(playerRenderer, SpecificModelReloadListener.INSTANCE::getSpecificModel));
			}
		});
	}

	public static Identifier id(String path) {
		return Identifier.of(ID, path);
	}

	private static BlockStateModel.UnbakedGrouped simpleUnbakedGroupedBlockStateModel(Identifier model) {
		return new SimpleBlockStateModel.Unbaked(new ModelVariant(model)).cached();
	}

	//private static class DownQuadRemovingModel extends WrapperBakedModel implements FabricBakedModel {
	//	DownQuadRemovingModel(BakedModel model) {
	//		super(model);
	//	}
	//
	//	@Override
	//	public void emitBlockQuads(QuadEmitter emitter, BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, Predicate<@Nullable Direction> cullTest) {
	//		emitter.pushTransform(q -> q.cullFace() != Direction.DOWN);
	//		((FabricBakedModel) wrapped).emitBlockQuads(emitter, blockView, state, pos, randomSupplier, cullTest);
	//		emitter.popTransform();
	//	}
	//}
}
