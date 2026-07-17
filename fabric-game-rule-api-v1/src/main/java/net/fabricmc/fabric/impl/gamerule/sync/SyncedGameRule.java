package net.fabricmc.fabric.impl.gamerule.sync;

public interface SyncedGameRule {
	boolean fabric_isSynced();

	void fabric_setSynced();
}
