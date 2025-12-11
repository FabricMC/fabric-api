package net.fabricmc.fabric.api.renderer.v1.mesh;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

/**
 * An atlas that a {@link QuadView} uses.
 */
public enum QuadAtlas {
	BLOCK(TextureAtlas.LOCATION_BLOCKS),
	ITEM(TextureAtlas.LOCATION_ITEMS);

	private final Identifier id;

	QuadAtlas(Identifier id) {
		this.id = id;
	}

	public Identifier getId() {
		return id;
	}
}
