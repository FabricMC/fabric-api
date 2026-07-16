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

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.DimensionEffects;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import net.fabricmc.fabric.impl.client.rendering.DimensionRenderingRegistryImpl;

/**
 * Dimensional renderers render world specific visuals of a world.
 * They may be used to render the sky, weather, or clouds.
 * The {@link DimensionEffects} is the vanilla dimensional renderer.
 */
public interface DimensionRenderingRegistry {
	/**
	 * Registers the custom sky renderer for a {@link World}.
	 *
	 * <p>This overrides vanilla sky rendering. A renderer registered here takes precedence over
	 * {@link FabricDimensionEffects#renderSky(WorldRenderContext)} for compatibility.
	 *
	 * @param key A {@link RegistryKey} for your {@link World}
	 * @param renderer A {@link SkyRenderer} implementation
	 * @throws IllegalArgumentException if key is already registered.
	 * @deprecated Use a custom {@link DimensionEffects} implementation that overrides
	 * {@link FabricDimensionEffects#renderSky(WorldRenderContext)} instead.
	 */
	@Deprecated
	static void registerSkyRenderer(RegistryKey<World> key, SkyRenderer renderer) {
		DimensionRenderingRegistryImpl.registerSkyRenderer(key, renderer);
	}

	/**
	 * Registers a custom weather renderer for a {@link World}.
	 *
	 * <p>This overrides vanilla weather rendering. A renderer registered here takes precedence over
	 * {@link FabricDimensionEffects#renderWeather(WorldRenderContext)} for compatibility.
	 *
	 * @param key A RegistryKey for your {@link World}
	 * @param renderer A {@link WeatherRenderer} implementation
	 * @throws IllegalArgumentException if key is already registered.
	 * @deprecated Use a custom {@link DimensionEffects} implementation that overrides
	 * {@link FabricDimensionEffects#renderWeather(WorldRenderContext)} instead.
	 */
	@Deprecated
	static void registerWeatherRenderer(RegistryKey<World> key, WeatherRenderer renderer) {
		DimensionRenderingRegistryImpl.registerWeatherRenderer(key, renderer);
	}

	/**
	 * Registers dimension effects for an {@link net.minecraft.util.Identifier}.
	 *
	 * <p>This registers a new option for the "effects" entry of the dimension type json.
	 *
	 * @param key     The {@link net.minecraft.util.Identifier} for the new option entry.
	 * @param effects The {@link DimensionEffects} option.
	 * @throws IllegalArgumentException if key is already registered.
	 */
	static void registerDimensionEffects(Identifier key, DimensionEffects effects) {
		DimensionRenderingRegistryImpl.registerDimensionEffects(key, effects);
	}

	/**
	 * Registers a custom cloud renderer for a {@link World}.
	 *
	 * <p>This overrides vanilla cloud rendering. A renderer registered here takes precedence over
	 * {@link FabricDimensionEffects#renderCloud(WorldRenderContext)} for compatibility.
	 *
	 * @param key      A {@link RegistryKey} for your {@link World}
	 * @param renderer A {@link CloudRenderer} implementation
	 * @throws IllegalArgumentException if key is already registered.
	 * @deprecated Use a custom {@link DimensionEffects} implementation that overrides
	 * {@link FabricDimensionEffects#renderCloud(WorldRenderContext)} instead.
	 */
	@Deprecated
	static void registerCloudRenderer(RegistryKey<World> key, CloudRenderer renderer) {
		DimensionRenderingRegistryImpl.registerCloudRenderer(key, renderer);
	}

	/**
	 * Gets the custom sky renderer for the given {@link World}.
	 *
	 * @param key A {@link RegistryKey} for your {@link World}
	 * @return {@code null} if no custom sky renderer is registered for the dimension.
	 */
	@Nullable
	static SkyRenderer getSkyRenderer(RegistryKey<World> key) {
		return DimensionRenderingRegistryImpl.getSkyRenderer(key);
	}

	/**
	 * Gets the custom cloud renderer for the given {@link World}.
	 *
	 * @param key A {@link RegistryKey} for your {@link World}
	 * @return {@code null} if no custom cloud renderer is registered for the dimension.
	 */
	@Nullable
	static CloudRenderer getCloudRenderer(RegistryKey<World> key) {
		return DimensionRenderingRegistryImpl.getCloudRenderer(key);
	}

	/**
	 * Gets the custom weather effect renderer for the given {@link World}.
	 *
	 * @return {@code null} if no custom weather effect renderer is registered for the dimension.
	 */
	@Nullable
	static WeatherRenderer getWeatherRenderer(RegistryKey<World> key) {
		return DimensionRenderingRegistryImpl.getWeatherRenderer(key);
	}

	/**
	 * Gets the dimension effects registered for an id.
	 * @param key A {@link RegistryKey} for your {@link World}.
	 * @return overworld effect if no dimension effects is registered for the key.
	 */
	@Nullable
	static DimensionEffects getDimensionEffects(Identifier key) {
		return DimensionRenderingRegistryImpl.getDimensionEffects(key);
	}

	@FunctionalInterface
	interface SkyRenderer {
		void render(WorldRenderContext context);
	}

	@FunctionalInterface
	interface WeatherRenderer {
		void render(WorldRenderContext context);
	}

	@FunctionalInterface
	interface CloudRenderer {
		void render(WorldRenderContext context);
	}
}
