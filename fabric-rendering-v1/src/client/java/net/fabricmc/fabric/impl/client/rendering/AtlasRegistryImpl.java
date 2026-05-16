package net.fabricmc.fabric.impl.client.rendering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;

public final class AtlasRegistryImpl {
	private static final Set<Identifier> REGISTERED_TEXTURES = new HashSet<>();
	private static final Set<Identifier> REGISTERED_ATLASES = new HashSet<>();
	private static final List<AtlasManager.AtlasConfig> REGISTERED_CONFIGS = new ArrayList<>();
	private static boolean frozen;

	public static void register(Identifier textureId, Identifier atlasId, boolean createMipmaps) {
		register(textureId, atlasId, createMipmaps, Set.of());
	}

	public static void register(Identifier textureId, Identifier atlasId, boolean createMipmaps, Set<MetadataSectionType<?>> additionalMetadata) {
		Objects.requireNonNull(textureId, "textureId must not be null.");
		Objects.requireNonNull(textureId, "atlasId must not be null.");
		Objects.requireNonNull(additionalMetadata, "additionalMetadata must not be null.");

		if (frozen) {
			throw new IllegalStateException("Atlas registry is frozen.");
		}

		if (REGISTERED_TEXTURES.contains(textureId)) {
			throw new IllegalArgumentException(String.format(
					"An atlas with texture %s has already exists.",
					textureId
			));
		}

		if (REGISTERED_ATLASES.contains(atlasId)) {
			throw new IllegalArgumentException(String.format(
					"Atlas %s already exists.",
					atlasId
			));
		}

		REGISTERED_CONFIGS.add(new AtlasManager.AtlasConfig(textureId, atlasId, createMipmaps, additionalMetadata));
		REGISTERED_ATLASES.add(atlasId);
		REGISTERED_TEXTURES.add(textureId);
	}

	public static List<AtlasManager.AtlasConfig> finalizeConfigs() {
		frozen = true;
		return List.copyOf(REGISTERED_CONFIGS);
	}

	private AtlasRegistryImpl() { }
}
