package net.fabricmc.fabric.impl.gamerule.client.sync;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.impl.gamerule.sync.ClientboundJoinSyncGameRulesPayload;
import net.fabricmc.fabric.impl.gamerule.sync.ClientboundSyncGameRulePayload;
import net.fabricmc.fabric.impl.gamerule.sync.FabricSyncedGameRuleOwner;

public class GameRuleSyncClientImpl implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(ClientboundSyncGameRulePayload.ID, (payload, context) -> {
			if (context.client().level instanceof FabricSyncedGameRuleOwner syncedGameRules)
				payload.updateGameRules(syncedGameRules.getGameRules());
		});
		ClientPlayNetworking.registerGlobalReceiver(ClientboundJoinSyncGameRulesPayload.ID, (payload, context) -> {
			if (context.client().level instanceof FabricSyncedGameRuleOwner syncedGameRules)
				syncedGameRules.setGameRules(payload.gameRules());
		});
	}
}
