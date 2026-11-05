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

package net.fabricmc.fabric.api.client.rendering.v1.pipline.modifier;

import net.minecraft.resources.ResourceKey;

import net.fabricmc.fabric.impl.client.rendering.RenderPipelineModifierRegistryImpl;

/// A Registry for {@link RenderPipelineModifier}s.
/// @see RenderPipelineModifier
/// @see RenderPipelineModifierManager
public final class RenderPipelineModifierRegistry {
	private RenderPipelineModifierRegistry() {
	}

	/// Registers a pipeline modifier.
	/// Should be called during {@link net.fabricmc.api.ClientModInitializer#onInitializeClient()}
	/// @param id the id of the modifier
	/// @param modifier the modifier
	public static void register(ResourceKey<RenderPipelineModifier> id, RenderPipelineModifier modifier) {
		RenderPipelineModifierRegistryImpl.register(id, modifier);
	}
}
