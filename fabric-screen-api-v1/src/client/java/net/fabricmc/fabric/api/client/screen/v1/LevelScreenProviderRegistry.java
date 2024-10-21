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

package net.fabricmc.fabric.api.client.screen.v1;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.gui.screen.world.LevelScreenProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.gen.WorldPreset;

import net.fabricmc.fabric.mixin.screen.LevelScreenProviderAccessor;

/**
 * Adds registration hooks for {@link LevelScreenProvider}s.
 */
public final class LevelScreenProviderRegistry {
	private static final Logger LOGGER = LoggerFactory.getLogger(LevelScreenProviderRegistry.class);

	private LevelScreenProviderRegistry() {
	}

	/**
	 * Registers a provider for a screen that allows users to adjust the generation options for a given world preset.
	 *
	 * @param worldPreset the world preset to register the provider for
	 * @param provider    the provider for the screen
	 */
	public static void register(RegistryKey<WorldPreset> worldPreset, LevelScreenProvider provider) {
		Objects.requireNonNull(worldPreset, "world preset cannot be null");
		Objects.requireNonNull(provider, "level screen provider cannot be null");

		Optional<RegistryKey<WorldPreset>> key = Optional.of(worldPreset);
		LevelScreenProvider old = LevelScreenProviderAccessor.fabric_getWorldPresetToScreenProvider().put(key, provider);

		if (old != null) {
			LOGGER.debug("Replaced old level screen provider mapping from {} to {} with {}", worldPreset, old, provider);
		}
	}
}
