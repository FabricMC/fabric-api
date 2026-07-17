package net.fabricmc.fabric.impl.gamerule.sync;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleMap;

import java.util.stream.Stream;

public class GameRuleSyncImpl implements ModInitializer {
	private static final int SYNC_PAYLOAD_MAX_SIZE = 20 * 1024 * 1024;

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.clientboundPlay().register(ClientboundSyncGameRulePayload.ID, ClientboundSyncGameRulePayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().registerLarge(ClientboundJoinSyncGameRulesPayload.ID, ClientboundJoinSyncGameRulesPayload.CODEC, SYNC_PAYLOAD_MAX_SIZE);

		ServerPlayerEvents.JOIN.register(player -> {
			Stream<GameRule<?>> availableRules = player.level()
				.getGameRules()
				.availableRules()
				.filter(gameRule -> ((SyncedGameRule) (Object) gameRule).fabric_isSynced());

			ServerPlayNetworking.send(player, new ClientboundJoinSyncGameRulesPayload(GameRuleMap.of(availableRules)));
		});
	}
}
