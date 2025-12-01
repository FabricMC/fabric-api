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

package net.fabricmc.fabric.test.resource.loader;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.enchantment.Enchantments;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.v1.DataResourceLoader;
import net.fabricmc.fabric.api.resource.v1.DataResourceStore;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.loader.api.FabricLoader;

public class ResourceReloaderTestMod implements ModInitializer {
	private static boolean clientResources = false;
	private static boolean serverResources = false;

	@Override
	public void onInitialize() {
		this.setupClientReloadListeners();
		this.setupServerReloadListeners();

		ServerTickEvents.START_WORLD_TICK.register(level -> {
			if (!clientResources && FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
				throw new AssertionError("Client reload listener was not called.");
			}

			if (!serverResources) {
				throw new AssertionError("Server reload listener was not called.");
			}

			level.getServer().getOrThrow(RegistryReloader.STORE_KEY);
		});
	}

	private void setupClientReloadListeners() {
		Identifier clientFirstId = Constants.id("client_first");
		Identifier clientSecondId = Constants.id("client_second");

		ResourceLoader resourceLoader = ResourceLoader.get(PackType.CLIENT_RESOURCES);
		resourceLoader.registerReloader(clientSecondId, (ResourceManagerReloadListener) manager -> {
			if (!clientResources) {
				throw new AssertionError("Second reload listener was called before the first!");
			}
		});
		resourceLoader.registerReloader(clientFirstId, (ResourceManagerReloadListener) manager -> clientResources = true);
		resourceLoader.addReloaderOrdering(clientFirstId, clientSecondId);
	}

	private void setupServerReloadListeners() {
		Identifier serverFirstId = Constants.id("server_first");
		Identifier serverSecondId = Constants.id("server_second");

		DataResourceLoader resourceLoader = DataResourceLoader.get();
		resourceLoader.registerReloader(serverSecondId, (ResourceManagerReloadListener) manager -> {
			if (!serverResources) {
				throw new AssertionError("Second reload listener was called before the first!");
			}
		});
		resourceLoader.registerReloader(serverFirstId, (ResourceManagerReloadListener) manager -> serverResources = true);
		resourceLoader.addReloaderOrdering(serverFirstId, serverSecondId);
		resourceLoader.registerReloader(RegistryReloader.ID, new RegistryReloader());
		resourceLoader.registerReloader(StatefulRegistryReloader.ID, StatefulRegistryReloader::new);
	}

	private static class RegistryReloader implements PreparableReloadListener {
		private static final Identifier ID = Constants.id("registry_reloader");
		private static final DataResourceStore.Key<String> STORE_KEY = new DataResourceStore.Key<>();

		@Override
		public CompletableFuture<Void> reload(SharedState store, Executor prepareExecutor, PreparationBarrier reloadSynchronizer, Executor applyExecutor) {
			HolderLookup.Provider registries = store.get(ResourceLoader.RELOADER_REGISTRY_LOOKUP_KEY);
			registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
			registries.lookupOrThrow(Registries.ITEM).getOrThrow(ItemTags.AXES);
			return reloadSynchronizer.wait(null).thenRunAsync(
					() -> store.get(DataResourceLoader.DATA_RESOURCE_STORE_KEY)
							.put(STORE_KEY, "Hello from RegistryReloader."),
					applyExecutor
			);
		}
	}

	private record StatefulRegistryReloader(HolderLookup.Provider registries) implements PreparableReloadListener {
		private static final Identifier ID = Constants.id("stateful_registry_reloader");

		@Override
		public CompletableFuture<Void> reload(SharedState store, Executor prepareExecutor, PreparationBarrier reloadSynchronizer, Executor applyExecutor) {
			this.registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
			this.registries.lookupOrThrow(Registries.ITEM).getOrThrow(ItemTags.AXES);
			return reloadSynchronizer.wait(null);
		}
	}
}
