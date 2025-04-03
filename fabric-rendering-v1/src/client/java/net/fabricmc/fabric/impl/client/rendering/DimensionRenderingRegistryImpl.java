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

package net.fabricmc.fabric.impl.client.rendering;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.DimensionEffects;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry.CloudRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry.SkyRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry.WeatherRenderer;

public final class DimensionRenderingRegistryImpl {
	private static final Map<RegistryKey<World>, SkyRenderer> SKY_RENDERERS = new IdentityHashMap<>();
	private static final Map<RegistryKey<World>, CloudRenderer> CLOUD_RENDERERS = new IdentityHashMap<>();
	private static final Map<RegistryKey<World>, WeatherRenderer> WEATHER_RENDERERS = new IdentityHashMap<>();

	public static void registerSkyRenderer(RegistryKey<World> key, DimensionRenderingRegistry.SkyRenderer renderer) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(renderer);

		SKY_RENDERERS.putIfAbsent(key, renderer);
	}

	public static void registerWeatherRenderer(RegistryKey<World> key, WeatherRenderer renderer) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(renderer);

		WEATHER_RENDERERS.putIfAbsent(key, renderer);
	}

	public static void registerDimensionEffects(Identifier key, DimensionEffects effects) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(effects);
		//The map containing all dimension effects returns a default if null so a null check doesn't work.

		throw new UnsupportedOperationException("Not supported in 25w14craftmine");
	}

	public static void registerCloudRenderer(RegistryKey<World> key, CloudRenderer renderer) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(renderer);

		CLOUD_RENDERERS.putIfAbsent(key, renderer);
	}

	@Nullable
	public static SkyRenderer getSkyRenderer(RegistryKey<World> key) {
		return SKY_RENDERERS.get(key);
	}

	@Nullable
	public static CloudRenderer getCloudRenderer(RegistryKey<World> key) {
		return CLOUD_RENDERERS.get(key);
	}

	@Nullable
	public static WeatherRenderer getWeatherRenderer(RegistryKey<World> key) {
		return WEATHER_RENDERERS.get(key);
	}

	@Nullable
	public static DimensionEffects getDimensionEffects(Identifier key) {
		throw new UnsupportedOperationException("Not supported in 25w14craftmine");
	}
}
