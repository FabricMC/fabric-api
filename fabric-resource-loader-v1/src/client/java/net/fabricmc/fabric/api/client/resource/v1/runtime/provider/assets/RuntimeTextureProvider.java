package net.fabricmc.fabric.api.client.resource.v1.runtime.provider.assets;

import net.fabricmc.loader.api.ModContainer;

/// Provides textures for a [`RuntimeAssetContext`][net.fabricmc.fabric.api.client.resource.v1.runtime.RuntimeAssetContext].
public abstract class RuntimeTextureProvider extends RuntimeAssetProvider {
	protected RuntimeTextureProvider(ModContainer container) {
		super(container);
	}
}
