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

package net.fabricmc.fabric.mixin.gamerule.sync;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleMap;
import net.minecraft.world.level.gamerules.GameRules;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends LevelMixin {
	@Shadow
	public abstract GameRules getGameRules();

	@Override
	public <T> T getSyncedValue(GameRule<T> gameRule) {
		if (!gameRule.isSynced()) {
			throw new IllegalArgumentException("Un-synced game rule %s should not be called from getSyncedValue".formatted(gameRule));
		}

		return this.getGameRules().get(gameRule);
	}

	@Override
	public GameRuleMap getSyncedGameRules() {
		GameRuleMap syncedGameRules = GameRuleMap.of();

		this.getGameRules().availableRules().forEach(gameRule -> {
			if (gameRule.isSynced()) {
				this.fabric_set(syncedGameRules, gameRule);
			}
		});

		return syncedGameRules;
	}

	@Unique
	private <T> void fabric_set(GameRuleMap gameRuleMap, GameRule<T> gameRule) {
		gameRuleMap.set(gameRule, this.getGameRules().get(gameRule));
	}
}
