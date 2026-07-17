package net.fabricmc.fabric.api.advancement.event.v1;

/**
 * Represents the origin of an advancement.
 */
public enum AdvancementSource {
	VANILLA,
	MOD,
	DATA_PACK,
	REPLACED;

	/**
	 * @return true if the advancement comes from the vanilla game or a mod, false otherwise.
	 */
	public boolean isBuiltin() {
		return this == VANILLA || this == MOD;
	}
}
