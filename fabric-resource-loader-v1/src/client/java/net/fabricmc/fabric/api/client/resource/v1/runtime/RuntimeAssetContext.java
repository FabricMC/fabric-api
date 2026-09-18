package net.fabricmc.fabric.api.client.resource.v1.runtime;

import org.jetbrains.annotations.ApiStatus;

import net.fabricmc.fabric.api.client.resource.v1.runtime.provider.assets.RuntimeAssetProvider;

/// Used to define [asset providers][RuntimeAssetProvider] and [provider handlers][ProviderHandler].
///
/// @see RuntimeAssetProvider
@ApiStatus.NonExtendable
public interface RuntimeAssetContext {
	/// Add a new [RuntimeAssetProvider] to the runtime asset generator.
	<T extends RuntimeAssetProvider> void addProvider(RuntimeAssetProvider.Factory<T> factory);

	/// Register a [ProviderHandler] for instances of a particular [Class] of [RuntimeAssetProvider].
	<T extends RuntimeAssetProvider> void registerHandler(
			Class<T> clazz,
			ProviderHandler<T> handler
	);

	/// Handles a type of [RuntimeAssetProvider] during runtime asset generation.
	@FunctionalInterface
	interface ProviderHandler<T extends RuntimeAssetProvider> {
		/// Invoked after all providers are collected and instantiated.
		/// Hook into relevant resource loading events here.
		void handleProvider(T provider);
	}
}
