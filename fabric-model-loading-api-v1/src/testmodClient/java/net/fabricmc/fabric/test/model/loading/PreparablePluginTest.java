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

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.texture.AtlasManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;

/**
 * Allows putting model files in {@code /model_replacements} instead of {@code /models} to override models.
 * This is just a test for off-thread data loading.
 *
 * <p>The visible effect in game is that gold blocks use the diamond texture instead...
 */
public class PreparablePluginTest implements ClientModInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final ResourceFinder MODEL_REPLACEMENTS_FINDER = ResourceFinder.json("model_replacements");

	@Override
	public void onInitializeClient() {
		PreparableModelLoadingPlugin.register(PreparablePluginTest::loadModelReplacements, (replacementModels, pluginContext) -> {
			pluginContext.modifyModelOnLoad().register((model, ctx) -> {
				@Nullable
				UnbakedModel replacementModel = replacementModels.get(ctx.id());

				if (replacementModel != null) {
					return replacementModel;
				}

				return model;
			});
		});
	}

	/**
	 * Adaptation of the {@link BakedModelManager} method.
	 */
	private static CompletableFuture<Map<Identifier, JsonUnbakedModel>> loadModelReplacements(ResourceReloader.Store resourceReloaderStore, Executor executor) {
		Objects.requireNonNull(resourceReloaderStore.getOrThrow(AtlasManager.stitchKey));

		return CompletableFuture.supplyAsync(() -> MODEL_REPLACEMENTS_FINDER.findResources(resourceReloaderStore.getResourceManager()), executor).thenCompose(models2 -> {
			ArrayList<CompletableFuture<Pair<Identifier, JsonUnbakedModel>>> list = new ArrayList<>(models2.size());

			for (Map.Entry<Identifier, Resource> entry : models2.entrySet()) {
				list.add(CompletableFuture.supplyAsync(() -> {
					try (BufferedReader reader = entry.getValue().getReader()) {
						// Remove model_replacements/ prefix from the identifier
						Identifier modelId = MODEL_REPLACEMENTS_FINDER.toResourceId(entry.getKey());

						return Pair.of(modelId, JsonUnbakedModel.deserialize(reader));
					} catch (Exception exception) {
						LOGGER.error("Failed to load model {}", entry.getKey(), exception);
						return null;
					}
				}, executor));
			}

			return Util.combineSafe(list).thenApply(models -> models.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond)));
		});
	}
}
