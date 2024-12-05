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

package net.fabricmc.fabric.impl.client.model.loading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.UnmodifiableView;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Util;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;

public final class ModelLoadingPluginManager {
	private static final List<ModelLoadingPlugin> PLUGINS = new ArrayList<>();
	private static final List<HolderImpl<?>> PREPARABLE_PLUGINS = new ArrayList<>();

	@UnmodifiableView
	public static final List<ModelLoadingPlugin> PLUGINS_VIEW = Collections.unmodifiableList(PLUGINS);
	@UnmodifiableView
	public static final List<PreparableModelLoadingPlugin.Holder<?>> PREPARABLE_PLUGINS_VIEW = Collections.unmodifiableList(PREPARABLE_PLUGINS);

	public static void registerPlugin(ModelLoadingPlugin plugin) {
		Objects.requireNonNull(plugin, "plugin must not be null");

		PLUGINS.add(plugin);
	}

	public static <T> void registerPlugin(PreparableModelLoadingPlugin.DataLoader<T> loader, PreparableModelLoadingPlugin<T> plugin) {
		Objects.requireNonNull(loader, "data loader must not be null");
		Objects.requireNonNull(plugin, "plugin must not be null");

		PREPARABLE_PLUGINS.add(new HolderImpl<>(loader, plugin));
	}

	public static CompletableFuture<List<ModelLoadingPlugin>> preparePlugins(ResourceManager resourceManager, Executor executor) {
		List<CompletableFuture<ModelLoadingPlugin>> futures = new ArrayList<>();

		for (ModelLoadingPlugin plugin : PLUGINS) {
			futures.add(CompletableFuture.completedFuture(plugin));
		}

		for (HolderImpl<?> holder : PREPARABLE_PLUGINS) {
			futures.add(preparePlugin(holder, resourceManager, executor));
		}

		return Util.combineSafe(futures);
	}

	private static <T> CompletableFuture<ModelLoadingPlugin> preparePlugin(HolderImpl<T> holder, ResourceManager resourceManager, Executor executor) {
		CompletableFuture<T> dataFuture = holder.loader.load(resourceManager, executor);
		return dataFuture.thenApply(data -> pluginContext -> holder.plugin.initialize(data, pluginContext));
	}

	private ModelLoadingPluginManager() { }

	private record HolderImpl<T>(PreparableModelLoadingPlugin.DataLoader<T> loader, PreparableModelLoadingPlugin<T> plugin) implements PreparableModelLoadingPlugin.Holder<T> {
	}
}
