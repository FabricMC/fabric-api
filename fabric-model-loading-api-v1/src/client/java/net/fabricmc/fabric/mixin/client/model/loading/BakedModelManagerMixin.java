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
import java.util.function.Function;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BlockStatesLoader;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.render.model.ReferencedModelsCollector;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.fabricmc.fabric.impl.client.model.loading.BakedModelsHooks;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingEventDispatcher;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingPluginManager;

@Mixin(BakedModelManager.class)
abstract class BakedModelManagerMixin implements FabricBakedModelManager {
	@Shadow
	@Final
	private BakedModel missingBlockModel;

	@Unique
	@Nullable
	private volatile CompletableFuture<ModelLoadingEventDispatcher> eventDispatcherFuture;

	@Unique
	@Nullable
	private Map<Identifier, BakedModel> extraModels;

	@Override
	public BakedModel getModel(Identifier id) {
		if (extraModels == null) {
			return missingBlockModel;
		}

		return extraModels.getOrDefault(id, missingBlockModel);
	}

	@Inject(method = "reload", at = @At("HEAD"))
	private void onHeadReload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Executor prepareExecutor, Executor applyExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		eventDispatcherFuture = ModelLoadingPluginManager.preparePlugins(manager, prepareExecutor).thenApplyAsync(ModelLoadingEventDispatcher::new);
	}

	@ModifyReturnValue(method = "reload", at = @At("RETURN"))
	private CompletableFuture<Void> resetEventDispatcherFuture(CompletableFuture<Void> future) {
		return future.thenApplyAsync(v -> {
			eventDispatcherFuture = null;
			return v;
		});
	}

	@ModifyExpressionValue(method = "reload", at = @At(value = "INVOKE", target = "net/minecraft/client/render/model/BlockStatesLoader.load(Lnet/minecraft/client/render/model/UnbakedModel;Lnet/minecraft/resource/ResourceManager;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
	private CompletableFuture<BlockStatesLoader.BlockStateDefinition> hookBlockStateModels(CompletableFuture<BlockStatesLoader.BlockStateDefinition> modelsFuture) {
		return modelsFuture.thenCombine(eventDispatcherFuture, (models, eventDispatcher) -> eventDispatcher.modifyBlockModelsOnLoad(models));
	}

	@ModifyArg(method = "reload", at = @At(value = "INVOKE", target = "java/util/concurrent/CompletableFuture.thenApplyAsync(Ljava/util/function/Function;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", ordinal = 1), index = 0)
	private Function<Void, ReferencedModelsCollector> hookModelDiscovery(Function<Void, ReferencedModelsCollector> function) {
		return v -> {
			CompletableFuture<ModelLoadingEventDispatcher> future = eventDispatcherFuture;

			if (future == null) {
				return function.apply(v);
			}

			ModelLoadingEventDispatcher.CURRENT.set(future.join());
			ReferencedModelsCollector referencedModelsCollector = function.apply(v);
			ModelLoadingEventDispatcher.CURRENT.remove();
			return referencedModelsCollector;
		};
	}

	@ModifyArg(method = "reload", at = @At(value = "INVOKE", target = "java/util/concurrent/CompletableFuture.thenApplyAsync(Ljava/util/function/Function;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", ordinal = 3), index = 0)
	private Function<Void, Object> hookModelBaking(Function<Void, Object> function) {
		return v -> {
			CompletableFuture<ModelLoadingEventDispatcher> future = eventDispatcherFuture;

			if (future == null) {
				return function.apply(v);
			}

			ModelLoadingEventDispatcher.CURRENT.set(future.join());
			Object bakingResult = function.apply(v);
			ModelLoadingEventDispatcher.CURRENT.remove();
			return bakingResult;
		};
	}

	@Inject(method = "upload", at = @At(value = "INVOKE_STRING", target = "net/minecraft/util/profiler/Profiler.swap(Ljava/lang/String;)V", args = "ldc=cache"))
	private void onUpload(CallbackInfo ci, @Local ModelBaker.BakedModels bakedModels) {
		extraModels = ((BakedModelsHooks) (Object) bakedModels).fabric_getExtraModels();
	}
}
