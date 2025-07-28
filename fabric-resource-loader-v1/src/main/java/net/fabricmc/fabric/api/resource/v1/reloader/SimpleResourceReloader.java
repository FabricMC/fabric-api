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

package net.fabricmc.fabric.api.resource.v1.reloader;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.SynchronousResourceReloader;

/**
 * A simplified version of the "resource reload listener" interface, hiding the
 * peculiarities of the API.
 *
 * <p>In essence, there are two stages:
 *
 * <ul><li>load: create an instance of your data object containing all loaded and
 * processed information,
 * <li>apply: apply the information from the data object to the game instance.</ul>
 *
 * <p>The load stage should be self-contained as it can run on any thread! However,
 * the apply stage is guaranteed to run on the game thread.
 *
 * <p>For a fully synchronous alternative, consider using
 * {@link SynchronousResourceReloader}.
 *
 * @param <T> the type for the intermediate reloading state
 */
public interface SimpleResourceReloader<T> extends ResourceReloader {
	@Override
	default CompletableFuture<Void> reload(ResourceReloader.Synchronizer helper, ResourceManager manager, Executor loadExecutor, Executor applyExecutor) {
		return this.load(manager, loadExecutor).thenCompose(helper::whenPrepared).thenCompose(
			(o) -> this.apply(o, manager, applyExecutor)
		);
	}

	/**
	 * Asynchronously process and load resource-based data. The code
	 * must be thread-safe and not modify game state!
	 *
	 * @param manager  the resource manager used during reloading
	 * @param executor the executor which should be used for this stage
	 * @return a CompletableFuture representing the "data loading" stage
	 */
	CompletableFuture<T> load(ResourceManager manager, Executor executor);

	/**
	 * Synchronously apply loaded data to the game state.
	 *
	 * @param manager  the resource manager used during reloading
	 * @param executor the executor which should be used for this stage
	 * @return a CompletableFuture representing the "data applying" stage
	 */
	CompletableFuture<Void> apply(T data, ResourceManager manager, Executor executor);
}
