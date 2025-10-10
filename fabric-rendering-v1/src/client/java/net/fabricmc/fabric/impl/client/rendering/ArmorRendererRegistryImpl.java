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
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.Registries;

import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;

public class ArmorRendererRegistryImpl {
	private static final HashMap<Item, ArmorRenderer.Factory> FACTORIES = new HashMap<>();
	private static final HashMap<Item, ArmorRenderer> RENDERERS = new HashMap<>();

	public static void register(ArmorRenderer.Factory factory, ItemConvertible... items) {
		Objects.requireNonNull(factory, "renderer factory is null");

		if (items.length == 0) {
			throw new IllegalArgumentException("Armor renderer registered for no item");
		}

		for (ItemConvertible item : items) {
			Objects.requireNonNull(item.asItem(), "armor item is null");

			if (FACTORIES.putIfAbsent(item.asItem(), factory) != null) {
				throw new IllegalArgumentException("Custom armor renderer already exists for " + Registries.ITEM.getId(item.asItem()));
			}
		}
	}

	public static void register(ArmorRenderer renderer, ItemConvertible... items) {
		Objects.requireNonNull(renderer, "renderer is null");
		register(context -> renderer, items);
	}

	@Nullable
	public static ArmorRenderer get(Item item) {
		return RENDERERS.get(item);
	}

	public static void createArmorRenderers(EntityRendererFactory.Context context) {
		RENDERERS.clear();
		FACTORIES.forEach((item, factory) -> RENDERERS.put(item, factory.createArmorRenderer(context)));
	}
}
