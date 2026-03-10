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

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRenderStateDataExtractor;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRenderStateExtractionCallback;

public final class RenderStateExtractionCallbackContextImpl implements EntityRenderStateExtractionCallback.Context {
	private final @Nullable EntityType<?> type;
	private final EntityRenderer<?, ?> renderer;
	private final EntityRendererProvider.Context rendererContext;
	private final List<EntityRenderStateDataExtractor> extractors = new ArrayList<>();

	public RenderStateExtractionCallbackContextImpl(@Nullable EntityType<?> type, EntityRenderer<?, ?> renderer, EntityRendererProvider.Context rendererContext) {
		this.type = type;
		this.renderer = renderer;
		this.rendererContext = rendererContext;
	}

	@Override
	public void add(EntityRenderStateDataExtractor extractor) {
		extractors.add(extractor);
	}

	@Override
	public @Nullable EntityType<?> type() {
		return type;
	}

	@Override
	public EntityRenderer<?, ?> renderer() {
		return renderer;
	}

	@Override
	public EntityRendererProvider.Context rendererContext() {
		return rendererContext;
	}

	public List<EntityRenderStateDataExtractor> extractors() {
		return extractors;
	}
}
