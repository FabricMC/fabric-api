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

import java.util.Deque;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import net.fabricmc.fabric.api.client.rendering.v1.pipline.modifier.RenderPipelineModifier;

public final class RenderPipelineModifierManagerImpl {
	public static final Deque<Pair<ResourceKey<RenderPipelineModifier>, RenderPipelineModifier>> ACTIVE_MODIFIERS = new LinkedList<>();
	public static final Map<ResourceKey<RenderPipelineModifier>, Map<RenderPipeline, RenderPipeline>> TRANSFORMED_PIPELINE_CACHE = new Reference2ReferenceOpenHashMap<>();

	private RenderPipelineModifierManagerImpl() {
	}

	public static RenderPipeline modify(RenderPipeline pipeline) {
		RenderSystem.assertOnRenderThread();

		if (ACTIVE_MODIFIERS.isEmpty()) {
			return pipeline;
		}

		for (Pair<ResourceKey<RenderPipelineModifier>, RenderPipelineModifier> modifier : ACTIVE_MODIFIERS) {
			Map<RenderPipeline, RenderPipeline> cache = TRANSFORMED_PIPELINE_CACHE.computeIfAbsent(modifier.getFirst(), _ -> new Reference2ReferenceOpenHashMap<>());
			pipeline = cache.computeIfAbsent(pipeline, last -> {
				Identifier name = last.getLocation().withSuffix("/transform/" + modifier.getFirst().identifier().toString().replace(":", "/"));
				RenderPipeline next = modifier.getSecond().modify(last, name);

				if (last != next && last.getLocation().equals(next.getLocation())) {
					throw new IllegalStateException(String.format(
							Locale.ROOT,
							"Modified pipeline %s must use a unique location instead of the incoming pipeline's location %s, ideally the provided location %s",
							next,
							last.getLocation(),
							name
					));
				}

				return next;
			});
		}

		return pipeline;
	}

	public static void renderWithModifier(ResourceKey<RenderPipelineModifier> modifier, Runnable renderer) {
		try {
			ACTIVE_MODIFIERS.push(Pair.of(modifier, RenderPipelineModifierRegistryImpl.getById(modifier)));
			renderer.run();
		} finally {
			ACTIVE_MODIFIERS.pop();
		}
	}

	public static void invalidateCacheFor(ResourceKey<RenderPipelineModifier> modifier) {
		TRANSFORMED_PIPELINE_CACHE.remove(modifier);
	}
}
