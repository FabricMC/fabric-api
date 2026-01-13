package net.fabricmc.fabric.api.debug.v1.client;

import java.util.Objects;

import net.minecraft.util.debug.DebugSubscription;

import net.fabricmc.fabric.impl.debug.client.ClientDebugSubscriptionRegistryImpl;

/// A registry for [debug subscriptions][DebugSubscription] on the client,
/// allowing listening to registered debug subscriptions on the client.
public final class ClientDebugSubscriptionRegistry {
	/// Registers a [DebugSubscription] on the client.
	///
	/// **Note:** this will register **outside development environments** if it
	/// is not checked. Surround calls to this method with
	/// [net.fabricmc.loader.api.FabricLoader#isDevelopmentEnvironment] if you
	/// do not intend for a debug feature to be present in production.
	///
	/// @param <T> the inner type of the [DebugSubscription].
	/// @param debugSubscription the [DebugSubscription] to register.
	public static <T> void register(DebugSubscription<T> debugSubscription) {
		Objects.requireNonNull(debugSubscription);
		ClientDebugSubscriptionRegistryImpl.register(debugSubscription);
	}

	/// Registers a [DebugSubscription] on the client if the `isEnabledFlag`
	/// parameter is `true`.
	///
	/// **Note:** this will register **outside development environments** if it
	/// is not checked. Surround calls to this method with
	/// [net.fabricmc.loader.api.FabricLoader#isDevelopmentEnvironment] if you
	/// do not intend for a debug feature to be present in production.
	///
	/// @param <T> the inner type of the [DebugSubscription].
	/// @param debugSubscription the [DebugSubscription] to register.
	/// @param isEnabledFlag the flag determining whether to register this
	/// [DebugSubscription].
	public static <T> void register(
			DebugSubscription<T> debugSubscription,
			boolean isEnabledFlag
	) {
		if (isEnabledFlag) {
			register(debugSubscription);
		}
	}
}
