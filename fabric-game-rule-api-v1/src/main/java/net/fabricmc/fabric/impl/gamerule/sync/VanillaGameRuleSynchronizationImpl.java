package net.fabricmc.fabric.impl.gamerule.sync;

import net.minecraft.world.level.gamerules.GameRule;

public final class VanillaGameRuleSynchronizationImpl {
	private VanillaGameRuleSynchronizationImpl() {
	}

	public static void addSynchronizedGameRule(GameRule<?> gameRule) {
		((SyncedGameRuleSetter) (Object) gameRule).fabric_setSynced();
	}
}
