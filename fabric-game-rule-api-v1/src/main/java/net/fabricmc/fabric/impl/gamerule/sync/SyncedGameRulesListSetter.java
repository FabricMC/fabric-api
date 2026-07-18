package net.fabricmc.fabric.impl.gamerule.sync;

import net.minecraft.world.level.gamerules.GameRuleMap;

public interface SyncedGameRulesListSetter {
	default void fabric_setSyncedGameRules(GameRuleMap gameRules) {
		throw new AssertionError("Implemented via Mixin");
	}
}
