package net.fabricmc.fabric.api.debug.v1.client.renderer;

import java.util.Objects;

import net.minecraft.util.debug.DebugSubscription;

import net.fabricmc.fabric.impl.debug.client.renderer.DebugRendererRegistryImpl;

/// Registry for custom
/// [debug renderers][net.minecraft.client.renderer.debug.DebugRenderer.SimpleDebugRenderer].
public final class DebugRendererRegistry {
	/// Registers a debug renderer for the given [DebugSubscription].
	///
	/// @param <T> the inner type of the [DebugSubscription].
	/// @param debugSubscription the [DebugSubscription].
	/// @param rendererFactory the factory/constructor for the debug renderer.
	public static <T> void register(
			DebugSubscription<T> debugSubscription,
			DebugRendererFactory rendererFactory
	) {
		Objects.requireNonNull(debugSubscription);
		DebugRendererRegistryImpl.register(debugSubscription, rendererFactory);
	}

	/// Registers a debug renderer for the given [DebugSubscription] if
	/// `isEnabledFlag` is `true`.
	///
	/// @param <T> the inner type of the [DebugSubscription].
	/// @param debugSubscription the [DebugSubscription].
	/// @param rendererFactory the factory/constructor for the debug renderer.
	/// @param isEnabledFlag the flag determining whether to register this debug
	/// renderer.
	public static <T> void register(
			DebugSubscription<T> debugSubscription,
			DebugRendererFactory rendererFactory,
			boolean isEnabledFlag
	) {
		if (isEnabledFlag) {
			register(debugSubscription, rendererFactory);
		}
	}
}
