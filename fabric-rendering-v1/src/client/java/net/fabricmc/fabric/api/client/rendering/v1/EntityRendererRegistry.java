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

package net.fabricmc.fabric.api.client.rendering.v1;

import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

import net.fabricmc.fabric.impl.client.rendering.EntityRendererRegistryImpl;

/**
 * Helper class for registering EntityRenderers.
 *
 * <p>Use {@link net.minecraft.client.render.entity.EntityRendererFactories#register(EntityType, EntityRendererFactory)} instead.
 *
 * @deprecated Replaced with transitive access wideners in Fabric Transitive Access Wideners (v1).
 */
@Deprecated
public final class EntityRendererRegistry {
	/**
	 * Register an {@link EntityRenderer} for an {@link EntityType}. Can be called clientside before the world is rendered.
	 *
	 * @param entityType            the {@link EntityType} to register a renderer for
	 * @param entityRendererFactory a {@link EntityRendererFactory} that creates a {@link EntityRenderer}, called
	 *                              when {@link EntityRenderManager} is initialized or immediately if the dispatcher
	 *                              class is already loaded
	 * @param <E>                   the {@link Entity}
	 */
	public static <E extends Entity> void register(EntityType<? extends E> entityType, EntityRendererFactory<E> entityRendererFactory) {
		EntityRendererRegistryImpl.register(entityType, entityRendererFactory);
	}

	private EntityRendererRegistry() {
	}
}
