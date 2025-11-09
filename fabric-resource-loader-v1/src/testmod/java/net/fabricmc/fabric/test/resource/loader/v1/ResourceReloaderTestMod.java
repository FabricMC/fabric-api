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

package net.fabricmc.fabric.test.resource.loader.v1;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.v1.DataResourceLoader;
import net.fabricmc.fabric.api.resource.v1.DataResourceStore;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.loader.api.FabricLoader;

public class ResourceReloaderTestMod implements ModInitializer {
	public static final String NAMESPACE = "fabric-resource-loader-v1-testmod";

	private static boolean clientResources = false;
	private static boolean serverResources = false;

	@Override
	public void onInitialize() {
		this.setupClientReloadListeners();
		this.setupServerReloadListeners();

		ServerTickEvents.START_WORLD_TICK.register(world -> {
			if (!clientResources && FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
				throw new AssertionError("Client reload listener was not called.");
			}

			if (!serverResources) {
				throw new AssertionError("Server reload listener was not called.");
			}

			world.getServer().getOrThrow(RegistryReloader.STORE_KEY);
		});
	}

	private void setupClientReloadListeners() {
		Identifier clientFirstId = Identifier.of(NAMESPACE, "client_first");
		Identifier clientSecondId = Identifier.of(NAMESPACE, "client_second");

		ResourceLoader resourceLoader = ResourceLoader.get(ResourceType.CLIENT_RESOURCES);
		resourceLoader.registerReloader(clientSecondId, (SynchronousResourceReloader) manager -> {
			if (!clientResources) {
				throw new AssertionError("Second reload listener was called before the first!");
			}
		});
		resourceLoader.registerReloader(clientFirstId, (SynchronousResourceReloader) manager -> clientResources = true);
		resourceLoader.addReloaderOrdering(clientFirstId, clientSecondId);
	}

	private void setupServerReloadListeners() {
		Identifier serverFirstId = Identifier.of(NAMESPACE, "server_first");
		Identifier serverSecondId = Identifier.of(NAMESPACE, "server_second");

		DataResourceLoader resourceLoader = DataResourceLoader.get();
		resourceLoader.registerReloader(serverSecondId, (SynchronousResourceReloader) manager -> {
			if (!serverResources) {
				throw new AssertionError("Second reload listener was called before the first!");
			}
		});
		resourceLoader.registerReloader(serverFirstId, (SynchronousResourceReloader) manager -> serverResources = true);
		resourceLoader.addReloaderOrdering(serverFirstId, serverSecondId);
		resourceLoader.registerReloader(RegistryReloader.ID, new RegistryReloader());
		resourceLoader.registerReloader(StatefulRegistryReloader.ID, StatefulRegistryReloader::new);
	}

	private static class RegistryReloader implements ResourceReloader {
		private static final Identifier ID = Identifier.of(NAMESPACE, "registry_reloader");
		private static final DataResourceStore.Key<String> STORE_KEY = new DataResourceStore.Key<>();

		@Override
		public CompletableFuture<Void> reload(Store store, Executor prepareExecutor, Synchronizer reloadSynchronizer, Executor applyExecutor) {
			RegistryWrapper.WrapperLookup registries = store.getOrThrow(ResourceLoader.RELOADER_REGISTRY_LOOKUP_KEY);
			registries.getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
			registries.getOrThrow(RegistryKeys.ITEM).getOrThrow(ItemTags.AXES);
			return reloadSynchronizer.whenPrepared(null).thenRunAsync(
					() -> store.getOrThrow(DataResourceLoader.DATA_RESOURCE_STORE_KEY)
							.put(STORE_KEY, "Hello from RegistryReloader."),
					applyExecutor
			);
		}
	}

	private record StatefulRegistryReloader(RegistryWrapper.WrapperLookup registries) implements ResourceReloader {
		private static final Identifier ID = Identifier.of(NAMESPACE, "stateful_registry_reloader");

		@Override
		public CompletableFuture<Void> reload(Store store, Executor prepareExecutor, Synchronizer reloadSynchronizer, Executor applyExecutor) {
			this.registries.getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
			this.registries.getOrThrow(RegistryKeys.ITEM).getOrThrow(ItemTags.AXES);
			return reloadSynchronizer.whenPrepared(null);
		}
	}
}
