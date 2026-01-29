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

package net.fabricmc.fabric.api.dimension.v1;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.level.dimension.DimensionType;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Events for manipulating dimensions.
 */
public class DimensionEvents {
	/**
	 * This event can be used to modify the environment attributes of a dimension.
	 * The main use case is to add modded attributes to vanilla or modded dimensions
	 */
	public static final Event<ModifyAttributes> MODIFY_ATTRIBUTES = EventFactory.createArrayBacked(ModifyAttributes.class, listeners -> (dimension, attributes, registries) -> {
		for (ModifyAttributes listener : listeners) {
			listener.modifyDimensionAttributes(dimension, attributes, registries);
		}
	});

	@FunctionalInterface
	public interface ModifyAttributes {
		void modifyDimensionAttributes(Holder<DimensionType> dimension, EnvironmentAttributeMap.Builder attributes, HolderLookup.Provider registries);
	}
}
