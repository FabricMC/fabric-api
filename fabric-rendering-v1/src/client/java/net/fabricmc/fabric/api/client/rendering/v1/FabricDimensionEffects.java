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

/**
 * Fabric-provided extensions for {@link net.minecraft.client.render.DimensionEffects}.
 *
 * <p>Note: This interface is automatically implemented on all
 * {@link net.minecraft.client.render.DimensionEffects} instances via Mixin and interface injection.
 *
 * <p>Implement these methods on a custom {@link net.minecraft.client.render.DimensionEffects}
 * to render dimension-specific sky, weather, or clouds. For per-world behavior, inspect
 * {@link WorldRenderContext#world()}.
 *
 * <p>Legacy renderers registered through {@link DimensionRenderingRegistry} take precedence for
 * compatibility. If no legacy renderer is registered for the current world, the corresponding
 * method below is called. Returning {@code true} cancels vanilla rendering for that render pass;
 * returning {@code false} allows vanilla rendering to continue.
 */
public interface FabricDimensionEffects {
	/**
	 * Renders custom sky content for this dimension.
	 *
	 * @param context context for the current world render
	 * @return {@code true} to skip vanilla sky rendering, or {@code false} to allow it to continue
	 */
	default boolean renderSky(WorldRenderContext context) {
		return false;
	}

	/**
	 * Renders custom weather content for this dimension.
	 *
	 * @param context context for the current world render
	 * @return {@code true} to skip vanilla weather rendering, or {@code false} to allow it to continue
	 */
	default boolean renderWeather(WorldRenderContext context) {
		return false;
	}

	/**
	 * Renders custom cloud content for this dimension.
	 *
	 * @param context context for the current world render
	 * @return {@code true} to skip vanilla cloud rendering, or {@code false} to allow it to continue
	 */
	default boolean renderCloud(WorldRenderContext context) {
		return false;
	}
}
