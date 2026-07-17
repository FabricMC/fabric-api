package net.fabricmc.fabric.mixin.gamerule.client.sync;

import net.fabricmc.fabric.impl.gamerule.sync.FabricSyncedGameRuleOwner;

import net.minecraft.client.multiplayer.ClientLevel;

import net.minecraft.world.level.gamerules.GameRuleMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientLevel.class)
public class ClientLevelMixin implements FabricSyncedGameRuleOwner {
	@Unique private GameRuleMap syncedGameRules = GameRuleMap.of();

	@Override
	public GameRuleMap getGameRules() {
		return this.syncedGameRules;
	}

	@Override
	public void setGameRules(GameRuleMap gameRules) {
		this.syncedGameRules = gameRules;
	}

}
