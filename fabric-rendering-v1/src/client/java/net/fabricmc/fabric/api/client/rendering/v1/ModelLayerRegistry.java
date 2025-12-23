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

import net.fabricmc.fabric.impl.client.rendering.ModelLayerImpl;
import net.fabricmc.fabric.mixin.client.rendering.ModelLayersAccessor;

/**
 * A helpers for registering model layers and providers for the layer's definition.
 */
public final class ModelLayerRegistry {
	/**
	 * Registers a model layer and registers a provider for a {@linkplain LayerDefinition}.
	 *
	 * @param modelLayer the model layer location
	 * @param provider the provider for the layer definition
	 */
	public static void registerModelLayer(ModelLayerLocation modelLayer, TexturedLayerDefinitionProvider provider) {
		Objects.requireNonNull(modelLayer, "ModelLayerLocation cannot be null");
		Objects.requireNonNull(provider, "TexturedLayerDefinitionProvider cannot be null");

		if (ModelLayerImpl.PROVIDERS.putIfAbsent(modelLayer, provider) != null) {
			throw new IllegalArgumentException(String.format("Cannot replace registration for model layer \"%s\"", modelLayer));
		}

		ModelLayersAccessor.getLayers().add(modelLayer);
	}

	/**
	 * Registers armor model layers and registers a provider for a {@link ArmorModelSet} of type {@link LayerDefinition}.
	 * @param armorModelSet the armor model set of type {@link ModelLayerLocation}
	 * @param provider the provider for the textured armor model set
	 */
	public static void registerArmorModelLayers(ArmorModelSet<ModelLayerLocation> armorModelSet, TexturedArmorModelSetProvider provider) {
		Objects.requireNonNull(armorModelSet, "ArmorModelSet cannot be null");
		Objects.requireNonNull(provider, "TexturedArmorModelSetProvider cannot be null");

		if (ModelLayerImpl.ARMOR_PROVIDERS.putIfAbsent(armorModelSet, provider) != null) {
			throw new IllegalArgumentException(String.format("Cannot replace registration for armor model layer \"%s\"",
					armorModelSet
			));
		}

		armorModelSet.map(ModelLayersAccessor.getLayers()::add);
	}

	private ModelLayerRegistry() {
	}

	@FunctionalInterface
	public interface TexturedLayerDefinitionProvider {
		/**
		 * Creates the textured layer definition for use in a {@link ModelLayerLocation}.
		 *
		 * @return the textured layer definition for the model layer location.
		 */
		LayerDefinition createLayerDefinition();
	}

	@FunctionalInterface
	public interface TexturedArmorModelSetProvider {
		/**
		 * Creates the textured layer definition for use in a {@link ArmorModelSet} of type {@link LayerDefinition}.
		 *
		 * @return the textured layer definition for the model layer.
		 */
		ArmorModelSet<LayerDefinition> createArmorModelSet();
	}
}
