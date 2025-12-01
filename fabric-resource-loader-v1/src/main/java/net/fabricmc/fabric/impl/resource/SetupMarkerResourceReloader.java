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

package net.fabricmc.fabric.impl.resource;

import net.minecraft.core.HolderLookup;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.flag.FeatureFlagSet;

import net.fabricmc.fabric.api.resource.v1.DataResourceLoader;

// Used to inject into the ResourceReloader store.
public record SetupMarkerResourceReloader(
		ReloadableServerResources dataPackContents,
		HolderLookup.Provider registries,
		FeatureFlagSet featureSet
) implements ResourceManagerReloadListener {
	@Override
	public void prepareSharedState(SharedState store) {
		store.set(DataResourceLoader.RELOADER_REGISTRY_LOOKUP_KEY, this.registries);
		store.set(DataResourceLoader.RELOADER_FEATURE_SET_KEY, this.featureSet);
		store.set(DataResourceLoader.ADVANCEMENT_LOADER_KEY, this.dataPackContents.getAdvancements());
		store.set(DataResourceLoader.RECIPE_MANAGER_KEY, this.dataPackContents.getRecipeManager());
		store.set(
				DataResourceLoader.DATA_RESOURCE_STORE_KEY,
				((FabricDataResourceStoreHolder) this.dataPackContents).fabric$getDataResourceStore()
		);
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		// Do nothing.
	}
}
