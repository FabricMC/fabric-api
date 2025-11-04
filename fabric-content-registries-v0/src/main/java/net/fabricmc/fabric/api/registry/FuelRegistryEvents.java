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

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.block.entity.FuelValues;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Contains events to aid in modifying fuels.
 */
@ApiStatus.NonExtendable
public interface FuelRegistryEvents {
	/**
	 * An event that is called when the fuel registry is being built after vanilla fuels have been registered and before exclusions have been applied.
	 */
	Event<FuelRegistryEvents.BuildCallback> BUILD = EventFactory.createArrayBacked(FuelRegistryEvents.BuildCallback.class, listeners -> (builder, context) -> {
		for (FuelRegistryEvents.BuildCallback listener : listeners) {
			listener.build(builder, context);
		}
	});

	/**
	 * An event that is called when the fuel registry is being built after vanilla exclusions have been applied.
	 */
	Event<FuelRegistryEvents.ExclusionsCallback> EXCLUSIONS = EventFactory.createArrayBacked(FuelRegistryEvents.ExclusionsCallback.class, listeners -> (builder, context) -> {
		for (FuelRegistryEvents.ExclusionsCallback listener : listeners) {
			listener.buildExclusions(builder, context);
		}
	});

	@ApiStatus.NonExtendable
	interface Context {
		/**
		 * Get the base smelt time for the fuel, for furnaces this defaults to 200.
		 * @return the base smelt time
		 */
		int baseSmeltTime();

		/**
		 * Get the {@link HolderLookup.Provider} for all registries.
		 * @return the registry lookup
		 */
		HolderLookup.Provider registries();

		/**
		 * Get the currently enabled feature set.
		 * @return the {@link FeatureFlagSet} instance
		 */
		FeatureFlagSet enabledFeatures();
	}

	/**
	 * Use this event to register custom fuels.
	 */
	@FunctionalInterface
	interface BuildCallback {
		/**
		 * Called when the fuel registry is being built after vanilla fuels have been registered and before exclusions have been applied.
		 *
		 * @param builder the builder being used to construct a {@link FuelValues} instance
		 * @param context the context for the event
		 */
		void build(FuelValues.Builder builder, Context context);
	}

	/**
	 * Use this event to register custom fuels.
	 */
	@FunctionalInterface
	interface ExclusionsCallback {
		/**
		 * Called when the fuel registry is being built after vanilla exclusions have been applied.
		 *
		 * @param builder the builder being used to construct a {@link FuelValues} instance
		 * @param context the context for the event
		 */
		void buildExclusions(FuelValues.Builder builder, Context context);
	}
}
