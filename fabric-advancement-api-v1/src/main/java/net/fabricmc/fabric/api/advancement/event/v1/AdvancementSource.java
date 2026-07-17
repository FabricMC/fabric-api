package net.fabricmc.fabric.api.advancement.event.v1;

/**
 * Represents the origin of an advancement.
 */
public enum AdvancementSource {
	VANILLA(true),
	MOD(true),
	DATA_PACK(false),
	REPLACED(false);

	private final boolean builtin;

	AdvancementSource(boolean builtin) {
		this.builtin = builtin;
	}

	/**
	 * Returns whether this loot table source is builtin
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
