package net.fabricmc.fabric.api.client.resource.v1.runtime.provider.assets;

import net.fabricmc.loader.api.ModContainer;

/// Provides assets for a [`RuntimeAssetContext`][net.fabricmc.fabric.api.client.resource.v1.runtime.RuntimeAssetContext].
public abstract class RuntimeAssetProvider {
	private final ModContainer container;

	protected RuntimeAssetProvider(ModContainer container) {
		this.container = container;
	}

	public ModContainer getContainer() {
		return container;
	}

	public interface Factory<T extends RuntimeAssetProvider> {
		T create(ModContainer container);
	}
}
