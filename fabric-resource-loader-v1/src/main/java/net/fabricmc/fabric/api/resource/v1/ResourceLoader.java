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
import org.jetbrains.annotations.NotNull;

import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.resource.v1.reloader.IdentifiableResourceReloader;
import net.fabricmc.fabric.impl.resource.v1.ResourceLoaderImpl;

/**
 * Provides various hooks into the resource loader.
 */
@ApiStatus.NonExtendable
public interface ResourceLoader {
	static ResourceLoader get(ResourceType type) {
		return ResourceLoaderImpl.get(type);
	}

	/**
	 * Register a resource reloader for a given resource manager type.
	 *
	 * @param reloader the resource reloader
	 * @see #registerReloader(Identifier, ResourceReloader)
	 * @see #registerReloader(Identifier, Function)
	 * @see #addReloaderOrdering(Identifier, Identifier)
	 */
	void registerReloader(IdentifiableResourceReloader reloader);

	/**
	 * Register a resource reloader for a given resource manager type.
	 *
	 * @param id the identifier of the resource reloader
	 * @param reloader the resource reloader
	 * @see #registerReloader(IdentifiableResourceReloader)
	 * @see #registerReloader(Identifier, Function)
	 * @see #addReloaderOrdering(Identifier, Identifier)
	 */
	default void registerReloader(Identifier id, ResourceReloader reloader) {
		this.registerReloader(IdentifiableResourceReloader.wrap(id, reloader));
	}

	/**
	 * Register a resource reloader for a given resource manager type.
	 *
	 * <p>Note: This is only supported for server data reloaders.
	 *
	 * @param id the identifier of the resource reloader
	 * @param reloaderFactory a function that creates a new instance of the listener with a given registry lookup
	 * @see #registerReloader(IdentifiableResourceReloader)
	 * @see #registerReloader(Identifier, ResourceReloader)
	 * @see #addReloaderOrdering(Identifier, Identifier)
	 */
	void registerReloader(Identifier id, Function<RegistryWrapper.WrapperLookup, ResourceReloader> reloaderFactory);

	/**
	 * Requests that resource reloaders registered as the first identifier is applied before the other referenced resource reloader.
	 *
	 * <p>Incompatible ordering constraints such as cycles will lead to inconsistent behavior:
	 * some constraints will be respected and some will be ignored. If this happens, a warning will be logged.
	 *
	 * <p>Please keep in mind that this only takes effect during the application stage!
	 *
	 * @param firstReloader  the identifier of the resource reloader that should run before the other
	 * @param secondReloader the identifier of the resource reloader that should run after the other
	 * @see net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys identifiers of Vanilla resource reloaders
	 * @see #registerReloader(Identifier, ResourceReloader) register a new resource reloader
	 * @see #registerReloader(IdentifiableResourceReloader) register a new resource reloader
	 */
	void addReloaderOrdering(@NotNull Identifier firstReloader, @NotNull Identifier secondReloader);
}
