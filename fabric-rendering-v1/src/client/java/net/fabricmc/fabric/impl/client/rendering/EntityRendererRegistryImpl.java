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

import java.util.HashMap;
import java.util.function.BiConsumer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/**
 * Helper class for registering EntityRenderers.
 */
public final class EntityRendererRegistryImpl {
	private static HashMap<EntityType<?>, EntityRendererProvider<?>> map = new HashMap<>();
	private static BiConsumer<EntityType<?>, EntityRendererProvider<?>> handler = (type, function) -> map.put(type, function);

	public static <T extends Entity> void register(EntityType<? extends T> entityType, EntityRendererProvider<T> factory) {
		handler.accept(entityType, factory);
	}

	public static void setup(BiConsumer<EntityType<?>, EntityRendererProvider<?>> vanillaHandler) {
		map.forEach(vanillaHandler);
		handler = vanillaHandler;
	}

	private EntityRendererRegistryImpl() {
	}
}
