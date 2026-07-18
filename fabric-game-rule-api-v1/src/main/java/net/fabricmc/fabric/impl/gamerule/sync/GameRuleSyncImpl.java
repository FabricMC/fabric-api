/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.impl.gamerule.sync;

import net.minecraft.world.level.gamerules.GameRuleMap;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.gamerule.v1.FabricSyncedGameRulesList;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class GameRuleSyncImpl implements ModInitializer {
	private static final int SYNC_PAYLOAD_MAX_SIZE = 20 * 1024 * 1024;

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.clientboundPlay().register(ClientboundSyncGameRulePayload.ID, ClientboundSyncGameRulePayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().registerLarge(ClientboundJoinSyncGameRulesPayload.ID, ClientboundJoinSyncGameRulesPayload.CODEC, SYNC_PAYLOAD_MAX_SIZE);

		ServerPlayerEvents.JOIN.register(player -> {
			GameRuleMap syncedGameRules = ((FabricSyncedGameRulesList) player.level()).getSyncedGameRules();

			if (!syncedGameRules.keySet().isEmpty()) {
				ServerPlayNetworking.send(player, new ClientboundJoinSyncGameRulesPayload(syncedGameRules));
			}
		});
	}
}
