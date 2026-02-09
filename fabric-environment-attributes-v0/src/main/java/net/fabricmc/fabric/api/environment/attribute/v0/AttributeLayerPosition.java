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

package net.fabricmc.fabric.api.environment.attribute.v0;

/**
 * Positions at which environment attribute layers can be inserted. Each position is associated with an event in
 * {@link EnvironmentAttributeEvents}. The enum is ordered by in which order Minecraft adds its default layers.
 */
public enum AttributeLayerPosition {
	/**
	 * The position before all vanilla layers.
	 */
	BEFORE_ALL,

	/**
	 * The position between dimension and biome layers.
	 */
	BETWEEN_DIMENSION_AND_BIOMES,

	/**
	 * The position between biome and timeline layers.
	 */
	BETWEEN_BIOMES_AND_TIMELINES,

	/**
	 * The position between timeline and weather layers.
	 */
	BETWEEN_TIMELINES_AND_WEATHER,

	/**
	 * The position after all vanilla layers.
	 */
	AFTER_ALL
}
