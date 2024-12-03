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

package net.fabricmc.fabric.api.client.model.loading.v1;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;

import net.minecraft.block.Block;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingPluginManager;

/**
 * A model loading plugin is used to extend the model loading process through the passed {@link Context} object.
 *
 * <p>{@link PreparableModelLoadingPlugin} can be used if some resources need to be loaded from the
 * {@link ResourceManager}.
 */
@FunctionalInterface
public interface ModelLoadingPlugin {
	/**
	 * Registers a model loading plugin.
	 */
	static void register(ModelLoadingPlugin plugin) {
		ModelLoadingPluginManager.registerPlugin(plugin);
	}

	/**
	 * Gets a list of all registered model loading plugins.
	 */
	@UnmodifiableView
	static List<ModelLoadingPlugin> getAll() {
		return ModelLoadingPluginManager.PLUGINS_VIEW;
	}

	/**
	 * Called towards the beginning of the model loading process, every time resource are (re)loaded.
	 * Use the context object to extend model loading as desired.
	 */
	void initialize(Context pluginContext);

	@ApiStatus.NonExtendable
	interface Context {
		/**
		 * Adds one or more models that will be loaded, baked, and made available through
		 * {@link FabricBakedModelManager#getModel(Identifier)}.
		 */
		void addModels(Identifier... ids);

		/**
		 * Adds multiple models that will be loaded, baked, and made available through
		 * {@link FabricBakedModelManager#getModel(Identifier)}.
		 */
		void addModels(Collection<? extends Identifier> ids);

		/**
		 * Registers a block state resolver for a block.
		 *
		 * <p>The block must be registered and a block state resolver must not have been previously registered for the
		 * block.
		 */
		void registerBlockStateResolver(Block block, BlockStateResolver resolver);

		/**
		 * Event access to monitor unbaked model loads and replace the loaded model.
		 */
		Event<ModelModifier.OnLoad> modifyModelOnLoad();

		/**
		 * Event access to monitor unbaked block model loads and replace the loaded model.
		 */
		Event<ModelModifier.OnLoadBlock> modifyBlockModelOnLoad();
	}
}
