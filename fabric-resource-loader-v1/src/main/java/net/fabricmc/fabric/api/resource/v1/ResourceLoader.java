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

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.flag.FeatureFlagSet;

import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.fabric.impl.resource.ResourceLoaderImpl;
import net.fabricmc.loader.api.ModContainer;

/**
 * Provides various hooks into the resource loader.
 */
@ApiStatus.NonExtendable
public interface ResourceLoader {
	/**
	 * The resource listener state key for the registry lookup {@link HolderLookup.Provider}.
	 *
	 * @apiNote The holder lookup is only available in {@linkplain PackType#SERVER_DATA server data} reload listeners.
	 */
	PreparableReloadListener.StateKey<HolderLookup.Provider> REGISTRY_LOOKUP_KEY = new PreparableReloadListener.StateKey<>();
	/**
	 * The resource listener state key for the currently enabled feature flag set.
	 *
	 * @apiNote The feature flag set is only available in {@linkplain PackType#SERVER_DATA server data} reload listeners.
	 */
	PreparableReloadListener.StateKey<FeatureFlagSet> FEATURE_FLAG_SET_KEY = new PreparableReloadListener.StateKey<>();

	static ResourceLoader get(PackType type) {
		return ResourceLoaderImpl.get(type);
	}

	/**
	 * Registers a reload listener for a given resource manager type.
	 *
	 * @param id the identifier of the resource listener
	 * @param listener the resource listener
	 * @see #addListenerOrdering(Identifier, Identifier)
	 */
	void registerReloadListener(Identifier id, PreparableReloadListener listener);

	/**
	 * Requests that reload listeners registered as the first identifier is applied before the other referenced reload listener.
	 *
	 * <p>Incompatible ordering constraints such as cycles will lead to inconsistent behavior:
	 * some constraints will be respected and some will be ignored. If this happens, a warning will be logged.
	 *
	 * <p>Please keep in mind that this only takes effect during the application stage!
	 *
	 * @param firstListener  the identifier of the reload listener that should run before the other
	 * @param secondListener the identifier of the reload listener that should run after the other
	 * @see net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys identifiers of Vanilla reload listeners
	 * @see #registerReloadListener(Identifier, PreparableReloadListener) register a new reload listener
	 */
	void addListenerOrdering(Identifier firstListener, Identifier secondListener);

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
	 * @return {@code true} if successfully registered the resource pack, or {@code false} otherwise
	 * @see #registerBuiltinPack(Identifier, ModContainer, Component, PackActivationType)
	 */
	static boolean registerBuiltinPack(Identifier id, ModContainer container, PackActivationType activationType) {
		return ResourceLoaderImpl.registerBuiltinPack(id, "resourcepacks/" + id.getPath(), container, activationType);
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
	 * @return {@code true} if successfully registered the resource pack, or {@code false} otherwise
	 * @see #registerBuiltinPack(Identifier, ModContainer, PackActivationType)
	 */
	static boolean registerBuiltinPack(Identifier id, ModContainer container, Component displayName, PackActivationType activationType) {
		return ResourceLoaderImpl.registerBuiltinPack(id, "resourcepacks/" + id.getPath(), container, displayName, activationType);
	}
}
