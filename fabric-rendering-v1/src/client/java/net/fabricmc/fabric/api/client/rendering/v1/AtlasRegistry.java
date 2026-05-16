package net.fabricmc.fabric.api.client.rendering.v1;

import net.fabricmc.fabric.impl.client.rendering.AtlasRegistryImpl;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;

import java.util.Set;

/**
 * A registry to add atlases to {@link net.minecraft.client.resources.model.sprite.AtlasManager}.
 */
public final class AtlasRegistry {
	/**
	 * Registers an atlas using a given texture and atlas id.
	 *
	 * @param textureId The id of the texture that will be generated.
	 * @param atlasId The id of the registered atlas.
	 * @param hasMipmaps Whether to generate mipmaps for the atlas.
	 * @param additionalMetadata Additional metadata for use in atlas sources.
	 */
	public static void register(Identifier textureId, Identifier atlasId, boolean hasMipmaps, Set<MetadataSectionType<?>> additionalMetadata) {
		AtlasRegistryImpl.register(textureId, atlasId, hasMipmaps, additionalMetadata);
	}

	/**
	 * Registers an atlas using a given texture and atlas id.
	 *
	 * @param textureId The id of the texture that will be generated.
	 * @param atlasId The id of the registered atlas.
	 * @param hasMipmaps Whether to generate mipmaps for the atlas.
	 */
	public static void register(Identifier textureId, Identifier atlasId, boolean hasMipmaps) {
		AtlasRegistryImpl.register(textureId, atlasId, hasMipmaps);
	}

	private AtlasRegistry() { }
}
