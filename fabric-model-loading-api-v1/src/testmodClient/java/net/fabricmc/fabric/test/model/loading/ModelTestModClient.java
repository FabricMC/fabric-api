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

import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.resources.model.MissingBlockModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;

public class ModelTestModClient implements ClientModInitializer {
	public static final String ID = "fabric-model-loading-api-v1-testmod";

	public static final Identifier HALF_RED_SAND_MODEL_ID = id("half_red_sand");
	public static final ExtraModelKey<BlockStateModel> HALF_RED_SAND_MODEL_KEY = ExtraModelKey.create(HALF_RED_SAND_MODEL_ID::toString);
	public static final Identifier WHEAT_STAGE0_MODEL_ID = Identifier.withDefaultNamespace("block/wheat_stage0");
	public static final Identifier WHEAT_STAGE7_MODEL_ID = Identifier.withDefaultNamespace("block/wheat_stage7");
	public static final Identifier BROWN_GLAZED_TERRACOTTA_MODEL_ID = Identifier.withDefaultNamespace("block/brown_glazed_terracotta");

	@Override
	public void onInitializeClient() {
		ModelLoadingPlugin.register(pluginContext -> {
			pluginContext.addModel(HALF_RED_SAND_MODEL_KEY, SimpleUnbakedExtraModel.blockStateModel(HALF_RED_SAND_MODEL_ID));

			// Make wheat stages 1->6 use the same model as stage 0. This can be done with resource packs, this is just a test.
			pluginContext.registerBlockStateResolver(Blocks.WHEAT, context -> {
				BlockState state = context.block().defaultBlockState();

				BlockStateModel.UnbakedRoot wheatStage0Model = simpleUnbakedGroupedBlockStateModel(WHEAT_STAGE0_MODEL_ID);
				BlockStateModel.UnbakedRoot wheatStage7Model = simpleUnbakedGroupedBlockStateModel(WHEAT_STAGE7_MODEL_ID);

				for (int age = 0; age <= 6; age++) {
					context.setModel(state.setValue(CropBlock.AGE, age), wheatStage0Model);
				}

				context.setModel(state.setValue(CropBlock.AGE, 7), wheatStage7Model);
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
			BlockState westOakFence = Blocks.OAK_FENCE.defaultBlockState().setValue(CrossCollisionBlock.WEST, true);
			pluginContext.modifyBlockModelOnLoad().register(ModelModifier.OVERRIDE_PHASE, (model, context) -> {
				if (context.state() == westOakFence) {
					return simpleUnbakedGroupedBlockStateModel(MissingBlockModel.LOCATION);
				}

				return model;
			});

			// Remove bottom face of gold blocks
			BlockState goldBlock = Blocks.GOLD_BLOCK.defaultBlockState();
			pluginContext.modifyBlockModelAfterBake().register(ModelModifier.WRAP_PHASE, (model, context) -> {
				if (context.state() == goldBlock) {
					return new DownQuadRemovingModel(model);
				}

				return model;
			});
		});

		ResourceLoader resourceLoader = ResourceLoader.get(PackType.CLIENT_RESOURCES);
		resourceLoader.registerReloader(SpecificModelReloadListener.ID, SpecificModelReloadListener.INSTANCE);
		resourceLoader.addReloaderOrdering(ResourceReloaderKeys.Client.MODELS, SpecificModelReloadListener.ID);

		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityRenderer instanceof AvatarRenderer playerRenderer) {
				registrationHelper.register(new BakedModelFeatureRenderer<>(playerRenderer, SpecificModelReloadListener.INSTANCE::getSpecificModel));
			}
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(ID, path);
	}

	private static BlockStateModel.UnbakedRoot simpleUnbakedGroupedBlockStateModel(Identifier model) {
		return new SingleVariant.Unbaked(new Variant(model)).asRoot();
	}

	private static class DownQuadRemovingModel extends WrapperBlockStateModel {
		DownQuadRemovingModel(BlockStateModel model) {
			super(model);
		}

		@Override
		public void emitQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
			emitter.pushTransform(q -> q.cullFace() != Direction.DOWN);
			// Modify the cullTest as an example of how to achieve maximum performance
			super.emitQuads(emitter, blockView, pos, state, random, cullFace -> {
				if (cullFace == Direction.DOWN) {
					return true;
				}

				return cullTest.test(cullFace);
			});
			emitter.popTransform();
		}

		@Override
		@Nullable
		public Object createGeometryKey(BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random) {
			Object subkey = wrapped.createGeometryKey(blockView, pos, state, random);

			if (subkey == null) {
				return subkey;
			}

			record Key(Object subkey) {
			}

			return new Key(subkey);
		}
	}
}
