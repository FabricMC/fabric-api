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

import java.util.Objects;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.ArmorModelSet;

import net.fabricmc.fabric.impl.client.rendering.EntityModelLayerImpl;
import net.fabricmc.fabric.mixin.client.rendering.ModelLayersAccessor;

/**
 * A helpers for registering entity model layers and providers for the layer's textured model data.
 */
public final class EntityModelLayerRegistry {
	/**
	 * Registers an entity model layer and registers a provider for a {@linkplain LayerDefinition}.
	 *
	 * @param modelLayer the entity model layer
	 * @param provider the provider for the textured model data
	 */
	public static void registerModelLayer(ModelLayerLocation modelLayer, TexturedModelDataProvider provider) {
		Objects.requireNonNull(modelLayer, "EntityModelLayer cannot be null");
		Objects.requireNonNull(provider, "TexturedModelDataProvider cannot be null");

		if (EntityModelLayerImpl.PROVIDERS.putIfAbsent(modelLayer, provider) != null) {
			throw new IllegalArgumentException(String.format("Cannot replace registration for entity model layer \"%s\"", modelLayer));
		}

		ModelLayersAccessor.getLayers().add(modelLayer);
	}

	/**
	 * Registers entity equipment model layers and registers a provider for a {@link ArmorModelSet} of type {@link LayerDefinition}.
	 * @param equipmentModelData the equipment model data of type {@link ModelLayerLocation}
	 * @param provider the provider for the textured equipment model data
	 */
	public static void registerEquipmentModelLayers(ArmorModelSet<ModelLayerLocation> equipmentModelData, TexturedEquipmentModelDataProvider provider) {
		Objects.requireNonNull(equipmentModelData, "EquipmentModelData cannot be null");
		Objects.requireNonNull(provider, "TexturedEquipmentModelDataProvider cannot be null");

		if (EntityModelLayerImpl.EQUIPMENT_PROVIDERS.putIfAbsent(equipmentModelData, provider) != null) {
			throw new IllegalArgumentException(String.format("Cannot replace registration for entity equipment model layer \"%s\"", equipmentModelData));
		}

		equipmentModelData.map(ModelLayersAccessor.getLayers()::add);
	}

	private EntityModelLayerRegistry() {
	}

	@FunctionalInterface
	public interface TexturedModelDataProvider {
		/**
		 * Creates the textured model data for use in a {@link ModelLayerLocation}.
		 *
		 * @return the textured model data for the entity model layer.
		 */
		LayerDefinition createModelData();
	}

	@FunctionalInterface
	public interface TexturedEquipmentModelDataProvider {
		/**
		 * Creates the textured model data for use in a {@link ArmorModelSet} of type {@link LayerDefinition}.
		 *
		 * @return the textured model data for the entity model layer.
		 */
		ArmorModelSet<LayerDefinition> createEquipmentModelData();
	}
}
