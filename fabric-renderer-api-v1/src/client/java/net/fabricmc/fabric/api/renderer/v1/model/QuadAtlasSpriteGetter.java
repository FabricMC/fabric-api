package net.fabricmc.fabric.api.renderer.v1.model;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadAtlas;

@FunctionalInterface
public interface QuadAtlasSpriteGetter {
	/**
	 * Gets a {@link SpriteFinder} linked to a {@link QuadAtlas}.
	 */
	SpriteFinder spriteFinder(QuadAtlas quadAtlas);
}
