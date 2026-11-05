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

package net.fabricmc.fabric.impl.client.rendering;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

import net.minecraft.resources.ResourceKey;

import net.fabricmc.fabric.api.client.rendering.v1.pipline.modifier.RenderPipelineModifier;

public final class RenderPipelineModifierRegistryImpl {
	public static final Map<ResourceKey<RenderPipelineModifier>, RenderPipelineModifier> MODIFIERS = new Reference2ReferenceOpenHashMap<>();
	private RenderPipelineModifierRegistryImpl() {
	}

	public static void register(ResourceKey<RenderPipelineModifier> id, RenderPipelineModifier modifier) {
		if (MODIFIERS.putIfAbsent(id, modifier) != null) {
			throw new IllegalStateException("Duplicate modifier registration for key " + id);
		}
	}

	static RenderPipelineModifier getById(ResourceKey<RenderPipelineModifier> id) {
		return MODIFIERS.get(id);
	}
}
