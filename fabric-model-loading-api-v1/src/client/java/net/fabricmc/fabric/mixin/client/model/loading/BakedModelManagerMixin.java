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

import java.io.Reader;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.item.ItemAssetsLoader;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BlockStatesLoader;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.render.model.ReferencedModelsCollector;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.fabricmc.fabric.impl.client.model.loading.BakedModelsHooks;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingEventDispatcher;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingPluginManager;

@Mixin(BakedModelManager.class)
abstract class BakedModelManagerMixin implements FabricBakedModelManager {
	@Unique
	@Nullable
	private volatile CompletableFuture<ModelLoadingEventDispatcher> eventDispatcherFuture;

	@Unique
	@Nullable
	private Map<ExtraModelKey<?>, ?> extraModels;

	@Override
	@SuppressWarnings("unchecked")
	public <T> @Nullable T getModel(ExtraModelKey<T> key) {
		return extraModels == null ? null : (T) extraModels.get(key);
	}

	@Inject(method = "reload", at = @At("HEAD"))
	private void onHeadReload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Executor prepareExecutor, Executor applyExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		eventDispatcherFuture = ModelLoadingPluginManager.preparePlugins(manager, prepareExecutor).thenApplyAsync(ModelLoadingEventDispatcher::new, prepareExecutor);
	}

	@ModifyReturnValue(method = "reload", at = @At("RETURN"))
	private CompletableFuture<Void> resetEventDispatcherFuture(CompletableFuture<Void> future) {
		return future.thenApplyAsync(v -> {
			eventDispatcherFuture = null;
			return v;
		});
	}

	@ModifyExpressionValue(method = "reload", at = @At(value = "INVOKE", target = "net/minecraft/client/render/model/BakedModelManager.reloadModels(Lnet/minecraft/resource/ResourceManager;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
	private CompletableFuture<Map<Identifier, UnbakedModel>> hookModels(CompletableFuture<Map<Identifier, UnbakedModel>> modelsFuture) {
		return modelsFuture.thenCombine(eventDispatcherFuture, (models, eventDispatcher) -> eventDispatcher.modifyModelsOnLoad(models));
	}

	@ModifyExpressionValue(method = "reload", at = @At(value = "INVOKE", target = "net/minecraft/client/render/model/BlockStatesLoader.load(Lnet/minecraft/resource/ResourceManager;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
	private CompletableFuture<BlockStatesLoader.LoadedModels> hookBlockStateModels(CompletableFuture<BlockStatesLoader.LoadedModels> modelsFuture) {
		return modelsFuture.thenCombine(eventDispatcherFuture, (models, eventDispatcher) -> eventDispatcher.modifyBlockModelsOnLoad(models));
	}

	@ModifyArg(method = "reload", at = @At(value = "INVOKE", target = "java/util/concurrent/CompletableFuture.thenApplyAsync(Ljava/util/function/Function;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", ordinal = 1), index = 0)
	private Function<Void, ?> hookTextureCollect(Function<Void, CompletableFuture<?>> function) {
		return withModelDispatcher(function);
	}

	@ModifyArg(method = "reload", at = @At(value = "INVOKE", target = "java/util/concurrent/CompletableFuture.thenComposeAsync(Ljava/util/function/Function;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", ordinal = 0), index = 0)
	private Function<Void, CompletableFuture<?>> hookModelBaking(Function<Void, CompletableFuture<?>> function) {
		return withModelDispatcher(function);
	}

	@Unique
	private <T, R> Function<T, R> withModelDispatcher(Function<T, R> function) {
		CompletableFuture<ModelLoadingEventDispatcher> future = eventDispatcherFuture;
		if (future == null) return function;
		return x -> {
			ModelLoadingEventDispatcher.CURRENT.set(future.join());

			try {
				return function.apply(x);
			} finally {
				ModelLoadingEventDispatcher.CURRENT.remove();
			}
		};
	}

	@Inject(method = "collect", at = @At(value = "INVOKE", target = "net/minecraft/client/render/model/ReferencedModelsCollector.collectModels()Ljava/util/Map;"))
	private static void resolveExtraModels(
			Map<Identifier, UnbakedModel> modelMap, BlockStatesLoader.LoadedModels stateDefinition, ItemAssetsLoader.Result result, CallbackInfoReturnable<?> cir,
			@Local ReferencedModelsCollector collector
	) {
		// We know eventDispatcherFuture is available, as it is required by the item and block models (hookModels).
		ModelLoadingEventDispatcher eventDispatcher = ModelLoadingEventDispatcher.CURRENT.get();
		if (eventDispatcher != null) eventDispatcher.getExtraModels().values().forEach(collector::resolve);
	}

	@Inject(method = "upload", at = @At(value = "INVOKE_STRING", target = "net/minecraft/util/profiler/Profiler.swap(Ljava/lang/String;)V", args = "ldc=cache"))
	private void onUpload(CallbackInfo ci, @Local ModelBaker.BakedModels bakedModels) {
		extraModels = ((BakedModelsHooks) (Object) bakedModels).fabric_getExtraModels();
	}

	// We want to redirect the JsonUnbakedModel.deserialize call, but its return type is JsonUnbakedModel, so we can't
	// do that directly.
	// Instead, cancel the original call and then modify the null value when it's being used to construct the Pair.
	@Redirect(method = "method_65750(Ljava/util/Map$Entry;)Lcom/mojang/datafixers/util/Pair;", at = @At(value = "INVOKE", target = "net/minecraft/client/render/model/json/JsonUnbakedModel.deserialize(Ljava/io/Reader;)Lnet/minecraft/client/render/model/json/JsonUnbakedModel;"))
	private static JsonUnbakedModel cancelVanillaDeserialize(Reader reader) {
		return null;
	}

	// Here we replace the null model with one produced by our own deserializer.
	// The Pair's type is actually Pair<Identifier, JsonUnbakedModel>, but since generics don't really exist, vanilla
	// code doesn't explicitly cast the model to JsonUnbakedModel, and the enclosing method returns UnbakedModels per
	// its return type, it's safe to return an UnbakedModel here.
	@ModifyArg(method = "method_65750(Ljava/util/Map$Entry;)Lcom/mojang/datafixers/util/Pair;", at = @At(value = "INVOKE", target = "com/mojang/datafixers/util/Pair.of(Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;", remap = false), index = 1)
	private static Object actuallyDeserializeModel(Object originalModel, @Local Reader reader) {
		return UnbakedModelDeserializer.deserialize(reader);
	}
}
