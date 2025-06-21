package net.fabricmc.fabric.api.datagen.v1;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import net.minecraft.util.Identifier;

/**
 * Constructor class for making Maps for BlockModel textures.
 * {@see FabricBlockModelSupplier}
 */

public class FabricTextureMap {
	private final HashMap<String, Identifier> map;
	private final List<String> bufferNames;

	/**
	 * Constructor for the FabricTextureMap. Sets the private variables for later accession.
	 *
	 * @param textureNames Different texture names.
	 */
	public FabricTextureMap(String... textureNames) {
		this.map = new HashMap<>();
		this.bufferNames = Arrays.stream(textureNames).toList();
	}

	/**
	 * Link the texture names to the locations of their textures.
	 *
	 * @param textureLocations Identifiers for the location of each texture.
	 */
	public FabricTextureMap set(Identifier... textureLocations) {
		List<Identifier> textureList = Arrays.stream(textureLocations).toList();
		int difference = textureList.size() - this.bufferNames.size();
		if (difference > 0) {
			throw new IllegalStateException(String.format("You need to provide %s more texture names to allocate to texture locations!", difference));
		}
		if (difference < 0) {
			throw new IllegalStateException(String.format("You need to provide %s more texture locations to link to texture names!", -difference));
		}

		for (int i = 0; i < bufferNames.size(); i++) {
			map.put(bufferNames.get(i), textureList.get(i));
		}

		return this;
	}

	/**
	 * Return the resulting {@link HashMap}.
	 *
	 * @return this HashMap.
	 */
	public HashMap<String, Identifier> get() {
		return this.map;
	}
}
