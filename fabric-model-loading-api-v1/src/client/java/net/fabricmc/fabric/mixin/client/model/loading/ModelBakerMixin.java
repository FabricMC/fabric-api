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

package net.fabricmc.fabric.mixin.client.model.loading;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.model.BakedSimpleModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.util.Identifier;
import net.minecraft.util.thread.AsyncHelper;

import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.impl.client.model.loading.BakedModelsHooks;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingEventDispatcher;

@Mixin(ModelBaker.class)
abstract class ModelBakerMixin {
	@Shadow
	@Final
	static Logger LOGGER;

	@Shadow
	@Final
	Map<Identifier, BakedSimpleModel> simpleModels;

	@Unique
	@Nullable
	private ModelLoadingEventDispatcher fabric_eventDispatcher;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onReturnInit(CallbackInfo ci) {
		fabric_eventDispatcher = ModelLoadingEventDispatcher.CURRENT.get();
	}

	@ModifyArg(method = "bake", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/thread/AsyncHelper;mapValues(Ljava/util/Map;Ljava/util/function/BiFunction;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", ordinal = 0), index = 1)
	private BiFunction<BlockState, BlockStateModel.UnbakedGrouped, BlockStateModel> hookBlockModelBake(BiFunction<BlockState, BlockStateModel.UnbakedGrouped, BlockStateModel> bifunction) {
		if (fabric_eventDispatcher == null) {
			return bifunction;
		}

		return (state, unbakedModel) -> {
			ModelLoadingEventDispatcher.CURRENT.set(fabric_eventDispatcher);
			BlockStateModel model = bifunction.apply(state, unbakedModel);
			ModelLoadingEventDispatcher.CURRENT.remove();
			return model;
		};
	}

	@ModifyReturnValue(method = "bake", at = @At("RETURN"))
	private CompletableFuture<ModelBaker.BakedModels> withExtraModels(CompletableFuture<ModelBaker.BakedModels> models, @Local Executor executor, @Local ModelBaker.BakerImpl baker) {
		if (fabric_eventDispatcher == null) return models;

		CompletableFuture<Map<ExtraModelKey<?>, Object>> extraModels = AsyncHelper.mapValues(fabric_eventDispatcher.getExtraModels(), (key, model) -> {
			try {
				return model.bake(baker);
			} catch (Exception e) {
				LOGGER.warn("Unable to bake extra model: '{}'", key, e);
				return null;
			}
		}, executor);
		return models.thenCombine(extraModels, (res, extra) -> {
			((BakedModelsHooks) (Object) res).fabric_setExtraModels(extra);
			return res;
		});
	}

	@WrapOperation(method = "method_68018", at = @At(value = "INVOKE", target = "net/minecraft/client/render/model/BlockStateModel$UnbakedGrouped.bake(Lnet/minecraft/block/BlockState;Lnet/minecraft/client/render/model/Baker;)Lnet/minecraft/client/render/model/BlockStateModel;"))
	private static BlockStateModel wrapBlockModelBake(BlockStateModel.UnbakedGrouped unbakedModel, BlockState state, Baker baker, Operation<BlockStateModel> operation) {
		ModelLoadingEventDispatcher eventDispatcher = ModelLoadingEventDispatcher.CURRENT.get();

		if (eventDispatcher == null) {
			return operation.call(unbakedModel, state, baker);
		}

		return eventDispatcher.modifyBlockModel(unbakedModel, state, baker, operation);
	}

	@WrapOperation(method = "method_68019", at = @At(value = "INVOKE", target = "net/minecraft/client/render/item/model/ItemModel$Unbaked.bake(Lnet/minecraft/client/render/item/model/ItemModel$BakeContext;)Lnet/minecraft/client/render/item/model/ItemModel;"))
	private ItemModel wrapItemModelBake(ItemModel.Unbaked unbakedModel, ItemModel.BakeContext bakeContext, Operation<ItemModel> operation, @Local Identifier itemId) {
		if (fabric_eventDispatcher == null) {
			return operation.call(unbakedModel, bakeContext);
		}

		return fabric_eventDispatcher.modifyItemModel(unbakedModel, itemId, bakeContext, operation);
	}
}
