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
 * {@link AttributeLayerProvider} implementations using {@link AttributeLayerRegistry#registerLayerProvider}. Using
 * {@link AttributeLayerProvider}, you can implement custom logic for modify and overriding attributes, based on
 * things like weather, location or time. You can set restrictions on when your layer provider should apply in relation
 * to vanilla providers or other modded providers using {@link AttributeLayerRegistry#addProviderOrdering}
 *
 * <h2>Background</h2>
 *
 * <p>
 * The {@link EnvironmentAttributeSystem} assigns zero or more layers to every environment attribute. A layer here is
 * represented by a {@link EnvironmentAttributeLayer}. Layers modify or
 * override the value of an environment attribute by some implementation-specific logic. In vanilla Minecraft, defining an
 * environment attribute in a dimension type, biome or timeline will cause a layer for that attribute to be added that
 * modifies the attribute accordingly.
 * </p>
 *
 * <p>
 * Minecraft creates an {@link EnvironmentAttributeSystem} for each {@link Level}, both on the client and the server,
 * and adds layers to this system through four different providers:
 * </p>
 *
 * <ol>
 * <li>Dimension type overrides: a layer is added for each attribute defined in the dimension type of the {@link Level}.</li>
 * <li>Biome overrides: if one or more biomes define an attribute, a layer is added for that attribute that alters the
 * attribute throughout different biomes.</li>
 * <li>Timeline overrides: if one or more timelines define an attribute, a layer isadded for that attribute that alters
 * the attribute according to how the timeline defines it.</li>
 * <li>Weather overrides: if the {@link Level} {@linkplain Level#canHaveWeather() has weather}, Minecraft defines some
 * hardcoded weather layers for weather for some attributes.</li>
 * </ol>
 *
 * <p>
 * Each layer is given the value outputted by the previous layer, and must modify or replace
 * this value based on some context parameters. The first layer is given the default value of the attribute.
 * </p>
 *
 * <h2>Adding modded layers</h2>
 *
 * <p>
 * As mentioned, the {@link AttributeLayerProvider} allows you to add modded layers to environment attributes during
 * the creation of the {@link EnvironmentAttributeSystem}. Every provider is associated to an {@link Identifier},
 * including vanilla's providers (despite that vanilla's providers are not implemented as separate classes).
 * As order matters, attribute layers can be ordered relative to vanilla's layers or other modded layers using
 * {@link AttributeLayerRegistry#addProviderOrdering}.
 * </p>
 *
 * <p>
 * Depending on the type of {@link EnvironmentAttributeLayer}, Minecraft caches values of environment attributes. Minecraft
 * allows three different types of layers:
 * </p>
 *
 * <ul>
 * <li>{@linkplain EnvironmentAttributeLayer.Constant Constant layers}: these modify the value in a context-independent
 * manner. Minecraft uses these for dimensions and expects them to be constant - do not depend on external variables
 * when implementing constant layers. If an attribute has only constant layers, the attribute value is simply cached and
 * never recomputed again. Minecraft uses constant layers to provide a dimension's attribute values.</li>
 * <li>{@linkplain EnvironmentAttributeLayer.Positional Positional layers}: these modify the value based on a position
 * in the world. If a positional layer is added to an attribute, Minecraft will recompute the value every time.
 * Minecraft uses positional layers for biome interpolated attribute values. Note that environment attributes can be
 * sampled without a position. In this case, positional layers are completely ignored.</li>
 * <li>{@linkplain EnvironmentAttributeLayer.TimeBased Time based layers}: these modify the value based on time.
 * Minecraft will cache the value per tick, so that if it is sampled multiple times per tick, it is only evaluated once.
 * Minecraft uses time based layers for timelines and weather.</li>
 * </ul>
 *
 * <p>
 * It is important that modded implementations take into account the constraints of different layer types, otherwise
 * they may not work properly.
 * </p>
 *
 */
public interface AttributeLayerProvider {
	/**
	 * Identifier associated to vanilla's dimension attribute layer provider.
	 */
	Identifier DIMENSIONS = Identifier.withDefaultNamespace("dimensions");

	/**
	 * Identifier associated to vanilla's biome attribute layer provider.
	 */
	Identifier BIOMES = Identifier.withDefaultNamespace("biomes");

	/**
	 * Identifier associated to vanilla's timeline attribute layer provider.
	 */
	Identifier TIMELINES = Identifier.withDefaultNamespace("timelines");

	/**
	 * Identifier associated to vanilla's weather attribute layer provider.
	 */
	Identifier WEATHER = Identifier.withDefaultNamespace("weather");

	/**
	 * The identifier associated to the first vanilla provider. Currently, this is an alias for {@link #DIMENSIONS}.
	 * This constant exists purely for compatibility: if Minecraft ever adds another provider before its dimension provider,
	 * then this constant is updated so that mods can guarantee that their layer provider activates before all
	 * vanilla providers.
	 */
	Identifier FIRST_VANILLA_PROVIDER = DIMENSIONS;

	/**
	 * The identifier associated to the last vanilla provider. Currently, this is an alias for {@link #WEATHER}.
	 * This constant exists purely for compatibility: if Minecraft ever adds another provider after its weather provider,
	 * then this constant is updated so that mods can guarantee that their layer provider activates after all
	 * vanilla providers.
	 */
	Identifier LAST_VANILLA_PROVIDER = WEATHER;

	/**
	 * Called to add attribute layers to an {@link EnvironmentAttributeSystem.Builder} for the given {@link Level}.
	 * This is called both on the client and on the server each time a {@link Level} is initialized.
	 *
	 * @param systemBuilder The {@link EnvironmentAttributeSystem.Builder} to add layers to.
	 * @param level         The {@link Level} that the environment attribute system is being created for.
	 */
	void addAttributeLayers(EnvironmentAttributeSystem.Builder systemBuilder, Level level);
}
