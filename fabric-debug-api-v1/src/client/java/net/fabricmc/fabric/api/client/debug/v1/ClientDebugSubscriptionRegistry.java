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

package net.fabricmc.fabric.api.client.debug.v1;

import java.util.Objects;

import net.minecraft.util.debug.DebugSubscription;

import net.fabricmc.fabric.impl.debug.client.ClientDebugSubscriptionRegistryImpl;

/// A registry for [debug subscriptions][DebugSubscription] on the client,
/// allowing listening to registered debug subscriptions on the client.
public final class ClientDebugSubscriptionRegistry {
	/// Registers a [DebugSubscription] on the client.
	///
	/// @apiNote This will register **outside development environments** if it
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
	/// @apiNote This will register **outside development environments** if it
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
