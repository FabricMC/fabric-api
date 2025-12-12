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

package net.fabricmc.fabric.impl.resource.loader;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;

public class ResourceManagerHelperImpl implements ResourceManagerHelper {
	private static final Map<PackType, ResourceManagerHelperImpl> registryMap = new HashMap<>();

	private final ResourceLoader resourceLoader;

	private ResourceManagerHelperImpl(PackType type) {
		this.resourceLoader = ResourceLoader.get(type);
	}

	public static ResourceManagerHelperImpl get(PackType type) {
		return registryMap.computeIfAbsent(type, ResourceManagerHelperImpl::new);
	}

	@Override
	public void registerReloadListener(IdentifiableResourceReloadListener listener) {
		this.resourceLoader.registerReloader(listener.getFabricId(), listener);
		listener.getFabricDependencies().forEach(dependency -> this.resourceLoader.addReloaderOrdering(dependency, listener.getFabricId()));
	}

	@Override
	public void registerReloadListener(Identifier identifier, Function<HolderLookup.Provider, IdentifiableResourceReloadListener> listenerFactory) {
		this.resourceLoader.registerReloader(identifier, new PreparableReloadListener() {
			@Override
			public CompletableFuture<Void> reload(SharedState store, Executor prepareExecutor, PreparationBarrier reloadSynchronizer, Executor applyExecutor) {
				HolderLookup.Provider registries = store.get(ResourceLoader.RELOADER_REGISTRY_LOOKUP_KEY);
				PreparableReloadListener resourceReloader = listenerFactory.apply(registries);

				return resourceReloader.reload(store, prepareExecutor, reloadSynchronizer, applyExecutor);
			}
		});
	}
}
