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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.mixin.client.rendering.AtlasManagerAccessor;

public final class AtlasRegistryImpl {
	private static final List<AtlasManager.AtlasConfig> REGISTERED_CONFIGS = new ArrayList<>();

	private static final Set<Identifier> REGISTERED_TEXTURES = new HashSet<>();
	private static final Set<Identifier> REGISTERED_ATLASES = new HashSet<>();

	static {
		for (final AtlasManager.AtlasConfig knownAtlas : AtlasManagerAccessor.getKnownAtlases()) {
			REGISTERED_TEXTURES.add(knownAtlas.textureId());
			REGISTERED_ATLASES.add(knownAtlas.definitionLocation());
		}
	}

	private static boolean frozen;

	public static void register(AtlasManager.AtlasConfig config) {
		Objects.requireNonNull(config, "config must not be null");

		if (frozen) {
			throw new IllegalStateException("The atlas registry has already been finalized.");
		}

		if (REGISTERED_TEXTURES.contains(config.textureId())) {
			throw new IllegalArgumentException("An atlas with texture " + config.textureId() + " has already been registered.");
		}

		if (REGISTERED_ATLASES.contains(config.definitionLocation())) {
			throw new IllegalArgumentException("Atlas " + config.definitionLocation() + " has already been registered.");
		}

		REGISTERED_CONFIGS.add(config);
		REGISTERED_ATLASES.add(config.definitionLocation());
		REGISTERED_TEXTURES.add(config.textureId());
	}

	public static Identifier generateTextureLocation(Identifier atlasId) {
		Objects.requireNonNull(atlasId, "atlasId must not be null");
		return atlasId.withPath(path -> "textures/atlas/" + path + ".png");
	}

	public static List<AtlasManager.AtlasConfig> getAtlases() {
		return List.copyOf(REGISTERED_CONFIGS);
	}

	public static List<AtlasManager.AtlasConfig> finalizeConfigs() {
		frozen = true;
		return getAtlases();
	}

	private AtlasRegistryImpl() { }
}
