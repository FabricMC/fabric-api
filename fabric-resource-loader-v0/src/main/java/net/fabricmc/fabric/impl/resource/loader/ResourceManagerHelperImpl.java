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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.CompositePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.util.Tuple;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.loader.api.ModContainer;

public class ResourceManagerHelperImpl implements ResourceManagerHelper {
	private static final Map<PackType, ResourceManagerHelperImpl> registryMap = new HashMap<>();
	private static final Set<Tuple<Component, ModNioResourcePack>> builtinResourcePacks = new HashSet<>();

	private final ResourceLoader resourceLoader;

	private ResourceManagerHelperImpl(PackType type) {
		this.resourceLoader = ResourceLoader.get(type);
	}

	public static ResourceManagerHelperImpl get(PackType type) {
		return registryMap.computeIfAbsent(type, ResourceManagerHelperImpl::new);
	}

	/**
	 * Registers a built-in resource pack. Internal implementation.
	 *
	 * @param id             the identifier of the resource pack
	 * @param subPath        the sub path in the mod resources
	 * @param container      the mod container
	 * @param displayName    the display name of the resource pack
	 * @param activationType the activation type of the resource pack
	 * @return {@code true} if successfully registered the resource pack, else {@code false}
	 * @see ResourceManagerHelper#registerBuiltinResourcePack(Identifier, ModContainer, Component, ResourcePackActivationType)
	 * @see ResourceManagerHelper#registerBuiltinResourcePack(Identifier, ModContainer, ResourcePackActivationType)
	 */
	public static boolean registerBuiltinResourcePack(Identifier id, String subPath, ModContainer container, Component displayName, ResourcePackActivationType activationType) {
		// Assuming the mod has multiple paths, we simply "hope" that the  file separator is *not* different across them
		List<Path> paths = container.getRootPaths();
		String separator = paths.getFirst().getFileSystem().getSeparator();
		subPath = subPath.replace("/", separator);
		ModNioResourcePack resourcePack = ModNioResourcePack.create(id.toString(), container, subPath, PackType.CLIENT_RESOURCES, activationType, false);
		ModNioResourcePack dataPack = ModNioResourcePack.create(id.toString(), container, subPath, PackType.SERVER_DATA, activationType, false);
		if (resourcePack == null && dataPack == null) return false;

		if (resourcePack != null) {
			builtinResourcePacks.add(new Tuple<>(displayName, resourcePack));
		}

		if (dataPack != null) {
			builtinResourcePacks.add(new Tuple<>(displayName, dataPack));
		}

		return true;
	}

	/**
	 * Registers a built-in resource pack. Internal implementation.
	 *
	 * @param id             the identifier of the resource pack
	 * @param subPath        the sub path in the mod resources
	 * @param container      the mod container
	 * @param activationType the activation type of the resource pack
	 * @return {@code true} if successfully registered the resource pack, else {@code false}
	 * @see ResourceManagerHelper#registerBuiltinResourcePack(Identifier, ModContainer, ResourcePackActivationType)
	 * @see ResourceManagerHelper#registerBuiltinResourcePack(Identifier, ModContainer, Component, ResourcePackActivationType)
	 */
	public static boolean registerBuiltinResourcePack(Identifier id, String subPath, ModContainer container, ResourcePackActivationType activationType) {
		return registerBuiltinResourcePack(id, subPath, container, Component.literal(id.getNamespace() + "/" + id.getPath()), activationType);
	}

	public static void registerBuiltinResourcePacks(PackType resourceType, Consumer<Pack> consumer) {
		// Loop through each registered built-in resource packs and add them if valid.
		for (Tuple<Component, ModNioResourcePack> entry : builtinResourcePacks) {
			ModNioResourcePack pack = entry.getB();

			// Add the built-in pack only if namespaces for the specified resource type are present.
			if (!pack.getNamespaces(resourceType).isEmpty()) {
				// Make the resource pack profile for built-in pack, should never be always enabled.
				PackLocationInfo info = new PackLocationInfo(
						entry.getB().packId(),
						entry.getA(),
						new BuiltinModResourcePackSource(pack.getFabricModMetadata().getName()),
						entry.getB().knownPackInfo()
				);
				PackSelectionConfig info2 = new PackSelectionConfig(
						pack.getActivationType() == ResourcePackActivationType.ALWAYS_ENABLED,
						Pack.Position.TOP,
						false
				);

				Pack profile = Pack.readMetaAndCreate(info, new Pack.ResourcesSupplier() {
					@Override
					public PackResources openPrimary(PackLocationInfo var1) {
						return entry.getB();
					}

					@Override
					public PackResources openFull(PackLocationInfo var1, Pack.Metadata metadata) {
						ModNioResourcePack pack = entry.getB();

						if (metadata.overlays().isEmpty()) {
							return pack;
						}

						List<PackResources> overlays = new ArrayList<>(metadata.overlays().size());

						for (String overlay : metadata.overlays()) {
							overlays.add(pack.createOverlay(overlay));
						}

						return new CompositePackResources(pack, overlays);
					}
				}, resourceType, info2);
				consumer.accept(profile);
			}
		}
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
