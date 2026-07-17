package net.fabricmc.fabric.impl.gamerule.sync;

import net.minecraft.world.level.gamerules.GameRuleMap;

public interface FabricSyncedGameRuleOwner {
	/**
	 * @return A list of game rules that are synced to the client via a call to {@link net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder#synced()}
	 */
	default GameRuleMap getGameRules() {
		throw new AssertionError("Implemented in Mixin");
	}

	default void setGameRules(GameRuleMap gameRules) {
		throw new AssertionError("Implemented in Mixin");
	}
}
