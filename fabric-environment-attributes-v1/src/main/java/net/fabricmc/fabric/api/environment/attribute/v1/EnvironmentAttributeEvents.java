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

import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.environment.attribute.EnvironmentAttributeEventsImpl;

/**
 * Events related to environment attributes.
 */
public class EnvironmentAttributeEvents {
	/**
	 * Returns the {@link InsertLayers} event for the given {@link AttributeLayerPosition}. This event allows inserting
	 * extra attribute layers at that position during the environment attribute setup for a {@link Level}. By default,
	 * Minecraft adds layers for the dimension, then for biomes, then for timelines and lastly for weather. The event
	 * returned by this method is triggered before, in between and after each of these vanilla layers, depending on the
	 * selected position, in the following manner:
	 * <ul>
	 * <li>Event for {@link AttributeLayerPosition#BEFORE_ALL} is triggered.</li>
	 * <li>Minecraft adds the layer for dimension type attribute configurations.</li>
	 * <li>Event for {@link AttributeLayerPosition#BETWEEN_DIMENSION_AND_BIOMES} is triggered.</li>
	 * <li>Minecraft adds the layer for biome attribute configurations.</li>
	 * <li>Event for {@link AttributeLayerPosition#BETWEEN_BIOMES_AND_TIMELINES} is triggered.</li>
	 * <li>Minecraft adds the layer for timeline attribute animations.</li>
	 * <li>Event for {@link AttributeLayerPosition#BETWEEN_TIMELINES_AND_WEATHER} is triggered.</li>
	 * <li>Minecraft adds the layer for some hardcoded weather attribute overrides.</li>
	 * <li>Event for {@link AttributeLayerPosition#AFTER_ALL} is triggered.</li>
	 * </ul>
	 * @param position The position at which you want to insert attribute layers.
	 * @return The event to listen for layer setup.
	 */
	public static Event<InsertLayers> insertLayersEvent(AttributeLayerPosition position) {
		return EnvironmentAttributeEventsImpl.getOrCreateInsertLayersEvent(position);
	}

	/**
	 * Callback for events returned from {@link #insertLayersEvent}.
	 */
	public interface InsertLayers {
		/**
		 * Insert custom attribute layers into the {@link EnvironmentAttributeSystem.Builder}.
		 * @param systemBuilder The environment attribute system builder to modify.
		 * @param level The level for which the attribute system is built.
		 */
		void insertAttributeLayers(EnvironmentAttributeSystem.Builder systemBuilder, Level level);
	}
}
