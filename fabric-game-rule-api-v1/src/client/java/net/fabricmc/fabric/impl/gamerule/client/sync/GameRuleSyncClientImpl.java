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

package net.fabricmc.fabric.impl.gamerule.client.sync;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.gamerules.v1.client.ClientGameRuleEvents;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.gamerules.GameRuleMap;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.gamerule.v1.FabricSyncedGameRuleOwner;
import net.fabricmc.fabric.impl.gamerule.client.ClientGameRuleEventsImpl;
import net.fabricmc.fabric.impl.gamerule.sync.ClientboundJoinSyncGameRulesPayload;
import net.fabricmc.fabric.impl.gamerule.sync.ClientboundSyncGameRulePayload;

public class GameRuleSyncClientImpl implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(ClientboundSyncGameRulePayload.ID, (payload, context) -> {
			ClientLevel level = context.client().level;
			if (level instanceof FabricSyncedGameRuleOwner syncedGameRules) {
				updateClientGameRules(level, syncedGameRules.getSyncedGameRules(), payload);
			}
		});
		ClientPlayNetworking.registerGlobalReceiver(ClientboundJoinSyncGameRulesPayload.ID, (payload, context) -> {
			if (context.client().level instanceof FabricSyncedGameRuleOwner syncedGameRules) {
				syncedGameRules.setSyncedGameRules(payload.gameRules());
			}
		});
	}

	private static <T> void updateClientGameRules(ClientLevel level, GameRuleMap gameRules, ClientboundSyncGameRulePayload<T> payload) {
		gameRules.set(payload.gameRule(), payload.value());
		Event<ClientGameRuleEvents.ValueSync<T>> valueSync = ClientGameRuleEventsImpl.getValueSync(payload.gameRule());

		if (valueSync != null) {
			valueSync.invoker().onGameRuleSynced(payload.value(), level);
		}
	}
}
