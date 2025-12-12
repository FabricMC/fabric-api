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

package net.fabricmc.fabric.api.resource;

import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;

import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.fabric.impl.resource.ResourceLoaderImpl;
import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl;
import net.fabricmc.loader.api.ModContainer;

/**
 * Helper for working with {@link ResourceManager} instances, and other resource loader generalities.
 */
@Deprecated
@ApiStatus.NonExtendable
public interface ResourceManagerHelper {
	/**
	 * Add a resource reload listener for a given registry.
	 *
	 * @param listener The resource reload listener.
	 * @deprecated Use {@link net.fabricmc.fabric.api.resource.v1.ResourceLoader#registerReloader(Identifier, PreparableReloadListener)} instead.
	 */
	@Deprecated
	default void addReloadListener(IdentifiableResourceReloadListener listener) {
		registerReloadListener(listener);
	}

	/**
	 * Register a resource reload listener for a given resource manager type.
	 *
	 * @param listener The resource reload listener.
	 * @deprecated Use {@link net.fabricmc.fabric.api.resource.v1.ResourceLoader#registerReloader(Identifier, PreparableReloadListener)} instead.
	 */
	@Deprecated
	void registerReloadListener(IdentifiableResourceReloadListener listener);

	/**
	 * Register a resource reload listener for a given resource manager type.
	 *
	 * <p>Note: This is only supported for server data reload listeners.
	 *
	 * @param identifier The identifier of the listener.
	 * @param listenerFactory   A function that creates a new instance of the listener with a given registry lookup.
	 * @deprecated Use {@link net.fabricmc.fabric.api.resource.v1.ResourceLoader#RELOADER_REGISTRY_LOOKUP_KEY} with {@link net.minecraft.server.packs.resources.PreparableReloadListener.SharedState},
	 * or {@link net.fabricmc.fabric.api.resource.v1.DataResourceLoader#registerReloader(Identifier, Function)} instead.
	 */
	@Deprecated
	void registerReloadListener(Identifier identifier, Function<HolderLookup.Provider, IdentifiableResourceReloadListener> listenerFactory);

	/**
	 * Get the ResourceManagerHelper instance for a given resource type.
	 *
	 * @param type The given resource type.
	 * @return The ResourceManagerHelper instance.
	 */
	static ResourceManagerHelper get(PackType type) {
		return ResourceManagerHelperImpl.get(type);
	}

	/**
	 * Registers a built-in resource pack.
	 *
	 * <p>A built-in resource pack is an extra resource pack provided by your mod which is not always active, it's similar to the "Programmer Art" resource pack.
	 *
	 * <p>Why and when to use it? A built-in resource pack should be used to provide extra assets/data that should be optional with your mod but still directly provided by it.
	 * For example, it could provide textures of your mod in another resolution, or could allow to provide different styles of your assets.
	 *
	 * <p>The path in which the resource pack is located is in the mod JAR file under the {@code "resourcepacks/<id path>"} directory. {@code id path} being the path specified
	 * in the identifier of this built-in resource pack.
	 *
	 * @param id             the identifier of the resource pack
	 * @param container      the mod container
	 * @param activationType the activation type of the resource pack
	 * @return {@code true} if successfully registered the resource pack, else {@code false}
	 * @deprecated See {@link ResourceLoader#registerBuiltinPack(Identifier, ModContainer, PackActivationType)}
	 */
	@Deprecated
	static boolean registerBuiltinResourcePack(Identifier id, ModContainer container, ResourcePackActivationType activationType) {
		return ResourceLoader.registerBuiltinPack(id, container, activationType.replacement);
	}

	/**
	 * Registers a built-in resource pack.
	 *
	 * <p>A built-in resource pack is an extra resource pack provided by your mod which is not always active, it's similar to the "Programmer Art" resource pack.
	 *
	 * <p>Why and when to use it? A built-in resource pack should be used to provide extra assets/data that should be optional with your mod but still directly provided by it.
	 * For example, it could provide textures of your mod in another resolution, or could allow to provide different styles of your assets.
	 *
	 * <p>The path in which the resource pack is located is in the mod JAR file under the {@code "resourcepacks/<id path>"} directory. {@code id path} being the path specified
	 * in the identifier of this built-in resource pack.
	 *
	 * @param id             the identifier of the resource pack
	 * @param container      the mod container
	 * @param displayName    the display name of the resource pack, should include mod name for clarity
	 * @param activationType the activation type of the resource pack
	 * @return {@code true} if successfully registered the resource pack, else {@code false}
	 * @deprecated See {@link ResourceLoader#registerBuiltinPack(Identifier, ModContainer, Component, PackActivationType)}
	 */
	@Deprecated
	static boolean registerBuiltinResourcePack(Identifier id, ModContainer container, Component displayName, ResourcePackActivationType activationType) {
		return ResourceLoader.registerBuiltinPack(id, container, displayName, activationType.replacement);
	}

	/**
	 * Registers a built-in resource pack.
	 *
	 * <p>A built-in resource pack is an extra resource pack provided by your mod which is not always active, it's similar to the "Programmer Art" resource pack.
	 *
	 * <p>Why and when to use it? A built-in resource pack should be used to provide extra assets/data that should be optional with your mod but still directly provided by it.
	 * For example, it could provide textures of your mod in another resolution, or could allow to provide different styles of your assets.
	 *
	 * <p>The path in which the resource pack is located is in the mod JAR file under the {@code "resourcepacks/<id path>"} directory. {@code id path} being the path specified
	 * in the identifier of this built-in resource pack.
	 *
	 * @param id             the identifier of the resource pack
	 * @param container      the mod container
	 * @param displayName    the display name of the resource pack, should include mod name for clarity
	 * @param activationType the activation type of the resource pack
	 * @return {@code true} if successfully registered the resource pack, else {@code false}
	 * @deprecated Use {@link #registerBuiltinResourcePack(Identifier, ModContainer, Component, ResourcePackActivationType)} instead.
	 */
	@Deprecated
	static boolean registerBuiltinResourcePack(Identifier id, ModContainer container, String displayName, ResourcePackActivationType activationType) {
		return ResourceLoader.registerBuiltinPack(id, container, Component.literal(displayName), activationType.replacement);
	}

	/**
	 * Registers a built-in resource pack.
	 *
	 * <p>A built-in resource pack is an extra resource pack provided by your mod which is not always active, it's similar to the "Programmer Art" resource pack.
	 *
	 * <p>Why and when to use it? A built-in resource pack should be used to provide extra assets/data that should be optional with your mod but still directly provided by it.
	 * For example, it could provide textures of your mod in another resolution, or could allow to provide different styles of your assets.
	 *
	 * <p>The {@code subPath} corresponds to a path in the JAR file which points to the resource pack folder. For example the subPath can be {@code "resourcepacks/extra"}.
	 *
	 * <p>Note about the enabled by default parameter: a resource pack cannot be enabled by default, only data packs can.
	 * Making this work for resource packs is near impossible without touching how Vanilla handles disabled resource packs.
	 *
	 * @param id               the identifier of the resource pack
	 * @param subPath          the sub path in the mod resources
	 * @param container        the mod container
	 * @param enabledByDefault {@code true} if enabled by default, else {@code false}
	 * @return {@code true} if successfully registered the resource pack, else {@code false}
	 * @deprecated Please use {@link #registerBuiltinResourcePack(Identifier, ModContainer, ResourcePackActivationType)} instead, the {@code sub path} should be removed in a future
	 * release in favor of the identifier path.
	 */
	@Deprecated
	static boolean registerBuiltinResourcePack(Identifier id, String subPath, ModContainer container, boolean enabledByDefault) {
		return ResourceLoaderImpl.registerBuiltinPack(id, subPath, container, Component.literal(id.getNamespace() + "/" + id.getPath()),
				enabledByDefault ? PackActivationType.DEFAULT_ENABLED : PackActivationType.NORMAL);
	}
}
