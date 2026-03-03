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

package net.fabricmc.fabric.api.environment.attribute.v1;

import org.jspecify.annotations.NullMarked;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.environment.attribute.AttributeLayerRegistryImpl;

/**
 * Utility class allowing you to register and reorder {@link AttributeLayerProvider}s.
 */
@NullMarked
public class AttributeLayerRegistry {
	/**
	 * Register a {@link AttributeLayerProvider}. If a layer provider with the given identifier already exists, an exception
	 * is thrown.
	 *
	 * @param id    The identifier of the layer provider. This identifier can be used to set an ordering via
	 *              {@link #addProviderOrdering}.   .
	 * @param layer The layer provider to register.
	 */
	public static void registerLayerProvider(Identifier id, AttributeLayerProvider layer) {
		AttributeLayerRegistryImpl.registerLayerProvider(id, layer);
	}

	/**
	 * Declares that the layer provider with the first identifier should activate before the layer provider with the
	 * second identifier. Unless this causes a cyclic dependency, the two layer providers are guaranteed to activate in
	 * said order. You may use this to order your layer provider against vanilla layer providers using any of the constants in
	 * {@link AttributeLayerProvider}. If both layer identifiers are the same, then an exception is thrown.
	 *
	 * @param firstLayer  The ID of the layer provider that should activate earlier.
	 * @param secondLayer The ID of the layer provider that should activate later.
	 */
	public static void addProviderOrdering(Identifier firstLayer, Identifier secondLayer) {
		AttributeLayerRegistryImpl.addProviderOrdering(firstLayer, secondLayer);
	}
}
