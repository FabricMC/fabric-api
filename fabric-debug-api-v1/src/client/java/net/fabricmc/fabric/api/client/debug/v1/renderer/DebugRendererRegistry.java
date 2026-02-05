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

package net.fabricmc.fabric.api.client.debug.v1.renderer;

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
