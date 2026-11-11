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

import java.util.Objects;

import net.minecraft.resources.ResourceKey;

import net.fabricmc.fabric.impl.client.rendering.RenderPipelineModifierManagerImpl;

/// Manages currently active {@link RenderPipelineModifier}s to allow rendering with customized pipelines
public final class RenderPipelineModifierManager {
	private RenderPipelineModifierManager() {
	}

	/// Performs a rendering operation with the given modifier active.
	public static void renderWithModifier(ResourceKey<RenderPipelineModifier> modifier, Runnable renderer) {
		Objects.requireNonNull(modifier);
		RenderPipelineModifierManagerImpl.renderWithModifier(modifier, renderer);
	}

	/// invalidate the pipeline transformation cache for a modifier to allow pipelines to be retransformed
	public static void invalidateCacheFor(ResourceKey<RenderPipelineModifier> modifier) {
		RenderPipelineModifierManagerImpl.invalidateCacheFor(modifier);
	}
}
