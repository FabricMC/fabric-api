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

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.HorizontalConnectingBlock;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.GroupableModel;
import net.minecraft.client.render.model.MissingModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.ModelVariant;
import net.minecraft.client.render.model.json.WeightedUnbakedModel;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.AffineTransformation;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.WrapperUnbakedModel;
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

			// Make wheat stages 1->6 use the same model as stage 0. This can be done with resource packs, this is just a test.
			pluginContext.registerBlockStateResolver(Blocks.WHEAT, context -> {
				BlockState state = context.block().getDefaultState();

				Identifier wheatStage0Id = Identifier.ofVanilla("block/wheat_stage0");
				Identifier wheatStage7Id = Identifier.ofVanilla("block/wheat_stage7");
				GroupableModel wheatStage0Model = simpleGroupableModel(wheatStage0Id);
				GroupableModel wheatStage7Model = simpleGroupableModel(wheatStage7Id);

				for (int age = 0; age <= 6; age++) {
					context.setModel(state.with(CropBlock.AGE, age), wheatStage0Model);
				}

				context.setModel(state.with(CropBlock.AGE, 7), wheatStage7Model);
			});

			// Replace the brown glazed terracotta model with a missing model without affecting child models.
			// Since 1.21.4, the item model is not a child model, so it is also affected.
			pluginContext.modifyModelOnLoad().register(ModelModifier.WRAP_PHASE, (model, context) -> {
				if (context.id().equals(BROWN_GLAZED_TERRACOTTA_MODEL_ID)) {
					return new WrapperUnbakedModel(model) {
						@Override
						public void resolve(Resolver resolver) {
							super.resolve(resolver);
							resolver.resolve(MissingModel.ID);
						}

						@Override
						public BakedModel bake(ModelTextures textures, Baker baker, ModelBakeSettings settings, boolean ambientOcclusion, boolean isSideLit, ModelTransformation transformation) {
							return baker.bake(MissingModel.ID, settings);
						}
					};
				}

				return model;
			});

			// Make oak fences with west: true and everything else false appear to be a missing model visually.
			BlockState westOakFence = Blocks.OAK_FENCE.getDefaultState().with(HorizontalConnectingBlock.WEST, true);
			pluginContext.modifyBlockModelOnLoad().register(ModelModifier.OVERRIDE_PHASE, (model, context) -> {
				if (context.state() == westOakFence) {
					return simpleGroupableModel(MissingModel.ID);
				}

				return model;
			});

			// TODO 1.21.4: reintroduce test once FRAPI+Indigo are ported
			// remove bottom face of gold blocks
			//pluginContext.modifyModelOnLoad().register(ModelModifier.WRAP_PHASE, (model, context) -> {
			//	if (context.id().equals(GOLD_BLOCK_MODEL_ID)) {
			//		return new WrapperUnbakedModel(model) {
			//			@Override
			//			public BakedModel bake(ModelTextures textures, Baker baker, ModelBakeSettings settings, boolean ambientOcclusion, boolean isSideLit, ModelTransformation transformation) {
			//				return new DownQuadRemovingModel(super.bake(textures, baker, settings, ambientOcclusion, isSideLit, transformation));
			//			}
			//		};
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
