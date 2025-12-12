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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import net.fabricmc.fabric.api.resource.v1.DataResourceLoader;

public final class DataResourceLoaderImpl extends ResourceLoaderImpl implements DataResourceLoader {
	public static final DataResourceLoaderImpl INSTANCE = new DataResourceLoaderImpl();
	private final Map<Identifier, Function<HolderLookup.Provider, PreparableReloadListener>> addedReloaderFactories
			= new LinkedHashMap<>();

	private DataResourceLoaderImpl() {
		super(PackType.SERVER_DATA);
	}

	@Override
	protected boolean hasResourceReloader(Identifier id) {
		return super.hasResourceReloader(id) || this.addedReloaderFactories.containsKey(id);
	}

	@Override
	public void registerReloader(Identifier id, Function<HolderLookup.Provider, PreparableReloadListener> factory) {
		Objects.requireNonNull(id, "The reloader identifier should not be null.");
		Objects.requireNonNull(factory, "The reloader factory should not be null.");
		this.checkUniqueResourceReloader(id);

		for (Map.Entry<Identifier, Function<HolderLookup.Provider, PreparableReloadListener>> entry
				: this.addedReloaderFactories.entrySet()
		) {
			if (entry.getValue() == factory) {
				throw new IllegalStateException(
						"Resource reloader factory with ID %s already in resource reloader factory set with ID %s!"
								.formatted(id, entry.getKey())
				);
			}
		}

		this.addedReloaderFactories.put(id, factory);
	}

	@Override
	protected Set<Map.Entry<Identifier, PreparableReloadListener>> collectReloadersToAdd(
			@Nullable SetupMarkerResourceReloader setupMarker
	) {
		if (setupMarker == null) {
			throw new IllegalStateException("The setup marker should not be null for data resource loading.");
		}

		HolderLookup.Provider registries = setupMarker.registries();
		Set<Map.Entry<Identifier, PreparableReloadListener>> reloadersToAdd = super.collectReloadersToAdd(setupMarker);

		for (Map.Entry<Identifier, Function<HolderLookup.Provider, PreparableReloadListener>> entry
				: this.addedReloaderFactories.entrySet()
		) {
			PreparableReloadListener reloader = entry.getValue().apply(registries);
			reloadersToAdd.add(Map.entry(entry.getKey(), reloader));
		}

		return reloadersToAdd;
	}
}
