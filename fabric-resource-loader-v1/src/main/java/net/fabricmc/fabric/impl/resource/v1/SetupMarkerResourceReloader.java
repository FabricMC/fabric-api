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

package net.fabricmc.fabric.impl.resource.v1;

import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.DataPackContents;

import net.fabricmc.fabric.api.resource.v1.DataResourceLoader;

// Used to inject into the ResourceReloader store.
public record SetupMarkerResourceReloader(
		DataPackContents dataPackContents,
		RegistryWrapper.WrapperLookup registries,
		FeatureSet featureSet
) implements SynchronousResourceReloader {
	@Override
	public void prepareSharedState(Store store) {
		store.put(DataResourceLoader.RELOADER_REGISTRY_LOOKUP_KEY, this.registries);
		store.put(DataResourceLoader.RELOADER_FEATURE_SET_KEY, this.featureSet);
		store.put(DataResourceLoader.ADVANCEMENT_LOADER_KEY, this.dataPackContents.getServerAdvancementLoader());
		store.put(DataResourceLoader.RECIPE_MANAGER_KEY, this.dataPackContents.getRecipeManager());
		store.put(
				DataResourceLoader.DATA_RESOURCE_STORE_KEY,
				((FabricDataResourceStoreHolder) this.dataPackContents).fabric$getDataResourceStore()
		);
	}

	@Override
	public void reload(ResourceManager manager) {
		// Do nothing.
	}
}
