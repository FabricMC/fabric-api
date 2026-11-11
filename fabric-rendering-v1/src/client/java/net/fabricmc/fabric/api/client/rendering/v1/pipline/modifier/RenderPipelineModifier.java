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

import com.mojang.renderpearl.api.pipeline.RenderPipeline;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/// A global modifier for a {@link RenderPipeline}.
/// Allows modifying a pipeline on the fly to change the way things rendered using it are rendered
/// @see RenderPipelineModifierRegistry
/// @see RenderPipelineModifierManager
@FunctionalInterface
public interface RenderPipelineModifier {
	ResourceKey<Registry<RenderPipelineModifier>> REGISTRY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("fabric", "pipeline_modifiers"));
	/// @param pipeline the render pipeline to modify, may not be the original pipeline in the chain if other modifiers are involved
	/// @param suggestedNewName a unique name that can be used for the transformed pipeline
	RenderPipeline modify(RenderPipeline pipeline, Identifier suggestedNewName);
}
