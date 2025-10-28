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

package net.fabricmc.fabric.api.resource.v1;

import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.resource.v1.DataResourceLoaderImpl;

/**
 * Provides various hooks into the {@linkplain net.minecraft.resource.ResourceType#SERVER_DATA server data} resource loader.
 */
@ApiStatus.NonExtendable
public interface DataResourceLoader extends ResourceLoader {
	/**
	 * The resource reloader store key for the recipe manager.
	 *
	 * @apiNote The recipe manager is only available in {@linkplain ResourceType#SERVER_DATA server data} resource reloaders.
	 * <br/>
	 * It should <b>only</b> be accessed in the application phase of the resource reloader,
	 * and you should depend on {@link net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys.Server#RECIPES}.
	 */
	ResourceReloader.Key<ServerRecipeManager> RECIPE_MANAGER_KEY = new ResourceReloader.Key<>();
	/**
	 * The resource reloader store key for the advancement loader.
	 *
	 * @apiNote The advancement loader is only available in {@linkplain ResourceType#SERVER_DATA server data} resource reloaders.
	 * <br/>
	 * It should <b>only</b> be accessed in the application phase of the resource reloader,
	 * and you should depend on {@link net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys.Server#ADVANCEMENTS}.
	 */
	ResourceReloader.Key<ServerAdvancementLoader> ADVANCEMENT_LOADER_KEY = new ResourceReloader.Key<>();
	/**
	 * The resource reloader store key for the data resource store.
	 *
	 * @apiNote The data resource store is only available in {@linkplain ResourceType#SERVER_DATA server data} resource reloaders.
	 * <br/>
	 * It should <b>only</b> be mutated in the application phase of the resource reloader.
	 */
	ResourceReloader.Key<DataResourceStore.Mutable> DATA_RESOURCE_STORE_KEY = new ResourceReloader.Key<>();

	static DataResourceLoader get() {
		return DataResourceLoaderImpl.INSTANCE;
	}

	/**
	 * Registers a data resource reloader.
	 *
	 * @param id the identifier of the resource reloader
	 * @param factory the factory function of the resource reloader
	 * @see #registerReloader(Identifier, ResourceReloader)
	 * @see #addReloaderOrdering(Identifier, Identifier)
	 *
	 * @apiNote In most cases {@link #registerReloader(Identifier, ResourceReloader)} is sufficient and should be preferred,
	 * but for some resource reloaders like {@link net.minecraft.resource.JsonDataLoader} constructing the resource reloader
	 * with a known instance of the wrapper lookup is required.
	 * <br/>
	 * While this may encourage stateful resource reloaders, it is best to primarily use resource reloaders as stateless loaders,
	 * as storing a state may easily lead to incomplete or leaking data.
	 */
	void registerReloader(
			Identifier id,
			Function<RegistryWrapper.WrapperLookup, ResourceReloader> factory
	);
}
