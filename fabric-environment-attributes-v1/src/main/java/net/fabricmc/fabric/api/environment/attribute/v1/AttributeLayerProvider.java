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

import net.minecraft.resources.Identifier;
import net.minecraft.world.attribute.EnvironmentAttributeLayer;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.level.Level;

/**
 * Provides {@link EnvironmentAttributeLayer}s to an {@link EnvironmentAttributeSystem}. You may register custom
 * {@link AttributeLayerProvider} implementations using {@link AttributeLayerRegistry#registerLayerProvider}.
 *
 * <p>
 * Attribute layers can be ordered relative to vanilla's layers or other modded layers using
 * {@link AttributeLayerRegistry#addLayerOrdering}. The order defines which layers override which other layers: layers
 * that come first in the ordering are overriden by layers that come later in the ordering. For example, in vanilla,
 * biome layers come after dimension layers, since biome-local attributes override dimension-global attributes.
 * </p>
 *
 * <p>
 * Minecraft adds layers in four phases: dimension-global attributes, then biome-local attributes, then
 * timeline-interpolated attributes, and finally some hardcoded weather attributes. Each of these phases, as well modded
 * layer providers, are associated with an identifier that can be sorted against.
 * </p>
 */
public interface AttributeLayerProvider {
	/**
	 * Identifier associated to vanilla's dimension attribute layers.
	 */
	Identifier DIMENSION = Identifier.withDefaultNamespace("dimensions");

	/**
	 * Identifier associated to vanilla's biome attribute layers.
	 */
	Identifier BIOMES = Identifier.withDefaultNamespace("biomes");

	/**
	 * Identifier associated to vanilla's timeline attribute layers.
	 */
	Identifier TIMELINES = Identifier.withDefaultNamespace("timelines");

	/**
	 * Identifier associated to vanilla's weather attribute layers.
	 */
	Identifier WEATHER = Identifier.withDefaultNamespace("weather");

	/**
	 * The identifier associated to the first vanilla phase. Currently, that is {@link #DIMENSION}.
	 * This constant exists purely for compatibility: if Minecraft ever adds another layer before its dimension phase,
	 * then this constant is updated.
	 */
	Identifier FIRST_VANILLA_PHASE = DIMENSION;

	/**
	 * The identifier associated to the last vanilla phase. Currently, that is {@link #WEATHER}.
	 * This constant exists purely for compatibility: if Minecraft ever adds another layer after its weather phase,
	 * then this constant is updated.
	 */
	Identifier LAST_VANILLA_PHASE = WEATHER;

	/**
	 * Called to add attribute layers to an {@link EnvironmentAttributeSystem.Builder} for the given {@link Level}.
	 * This is called both on the client and on the server for every {@link Level} that is created.
	 *
	 * @param systemBuilder The {@link EnvironmentAttributeSystem.Builder} to add layers to.
	 * @param level         The {@link Level} that the environment attribute system is being created for.
	 */
	void addAttributeLayers(EnvironmentAttributeSystem.Builder systemBuilder, Level level);
}
