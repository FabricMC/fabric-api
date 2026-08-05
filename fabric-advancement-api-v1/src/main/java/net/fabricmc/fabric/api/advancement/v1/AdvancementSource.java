package net.fabricmc.fabric.api.advancement.v1;

/**
 * Represents the origin of an advancement.
 */
public enum AdvancementSource {
	/**
	 * Represents a vanilla advancement.
	 */
	VANILLA(true),
	/**
	 * Represents an advancement created by a mod.
	 */
	MOD(true),
	/**
	 * Represents an advancement created by a datapack.
	 */
	DATA_PACK(false),
	/**
	 * Represents an advancement that has been modified with the advancement API.
	 */
	REPLACED(false);

	private final boolean builtin;

	AdvancementSource(boolean builtin) {
		this.builtin = builtin;
	}

	/**
	 * Returns whether this advancement source is builtin
	 * and bundled in the vanilla or mod resources.
	 *
	 * <p>{@link #VANILLA} and {@link #MOD} are builtin.
	 *
	 * @return {@code true} if builtin, {@code false} otherwise
	 */
	public boolean isBuiltin() {
		return builtin;
	}
}
