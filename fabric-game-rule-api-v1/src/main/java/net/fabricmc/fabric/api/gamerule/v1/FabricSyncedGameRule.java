package net.fabricmc.fabric.api.gamerule.v1;

/**
 * A game rule that can be queried from the client. It will be synced to the client when joining a {@link net.minecraft.server.level.ServerLevel} or when the value is changed.
 * @see FabricSyncedGameRulesList
 * @see GameRuleBuilder#synced()
 */
public interface FabricSyncedGameRule {
	/**
	 * @return whether this game rule is synced.
	 */
	default boolean isSynced() {
		throw new AssertionError("Implemented via Mixin");
	}
}
