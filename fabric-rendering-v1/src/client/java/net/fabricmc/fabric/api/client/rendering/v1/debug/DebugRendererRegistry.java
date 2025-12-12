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

package net.fabricmc.fabric.api.client.rendering.v1.debug;

import java.util.function.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.util.debug.DebugSubscription;

import net.fabricmc.fabric.impl.client.rendering.DebugRendererRegistryImpl;

/**
 * Helper class for registering custom {@linkplain DebugRenderer debug renderers}.
 */
public final class DebugRendererRegistry {
	private DebugRendererRegistry() {
	}

	public static <T> void register(
			DebugRendererFactory debugRenderer,
			DebugSubscription<T> debugSubscription
	) {
		DebugRendererRegistryImpl.register(debugRenderer, debugSubscription);
	}

	public static <T> void registerConditional(
			DebugRendererFactory debugRenderer,
			DebugSubscription<T> debugSubscription,
			Predicate<Minecraft> isEnabled
	) {
		DebugRendererRegistryImpl.registerConditional(debugRenderer, debugSubscription, isEnabled);
	}
}
