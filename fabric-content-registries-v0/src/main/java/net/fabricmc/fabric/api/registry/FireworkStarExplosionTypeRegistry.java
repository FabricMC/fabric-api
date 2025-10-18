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

package net.fabricmc.fabric.api.registry;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.FireworkStarRecipe;

import net.fabricmc.fabric.impl.content.registry.FireworkStarExplosionTypeRegistryImpl;

/**
 * A registry for the {@link FireworkExplosionComponent.Type} of an item for the {@link FireworkStarRecipe}.
 */
public final class FireworkStarExplosionTypeRegistry {
	private static final Logger LOGGER = LoggerFactory.getLogger(FireworkStarExplosionTypeRegistry.class);

	private FireworkStarExplosionTypeRegistry() {
	}

	/**
	 * Registers an explosion type for the item.
	 * @param item the item to register
	 * @param explosionType the firework explosion type
	 */
	public static void register(ItemConvertible item, FireworkExplosionComponent.Type explosionType) {
		Objects.requireNonNull(item, "Item cannot be null!");
		FireworkExplosionComponent.Type oldValue = FireworkStarExplosionTypeRegistryImpl.getFireworkStarExplosionTypeRegistry().put(item.asItem(), explosionType);

		if (oldValue != null) {
			LOGGER.info("Overriding previous firework star explosion type of {}, was: {}, now: {}", item.asItem().toString(), oldValue, explosionType);
		}
	}
}
