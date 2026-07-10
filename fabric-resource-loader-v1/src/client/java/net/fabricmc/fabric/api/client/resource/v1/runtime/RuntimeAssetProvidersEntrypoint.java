package net.fabricmc.fabric.api.client.resource.v1.runtime;

/// The `fabric-resource-loader-v1:runtime_asset_providers` entrypoint.
///
/// @see #onInitializeRuntimeAssetProviders(RuntimeAssetContext)
@FunctionalInterface
public interface RuntimeAssetProvidersEntrypoint {
	/// Invoked once when the game starts.
	///
	/// Add [asset providers][net.fabricmc.fabric.api.client.resource.v1.runtime.provider.assets.RuntimeAssetProvider] and define provider handlers here.
	void onInitializeRuntimeAssetProviders(RuntimeAssetContext context);
}
