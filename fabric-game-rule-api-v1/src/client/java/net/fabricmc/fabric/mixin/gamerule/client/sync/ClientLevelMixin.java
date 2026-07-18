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

package net.fabricmc.fabric.mixin.gamerule.client.sync;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleMap;

import net.fabricmc.fabric.impl.gamerule.sync.SyncedGameRule;
import net.fabricmc.fabric.mixin.gamerule.sync.LevelMixin;

import java.util.Objects;

@Mixin(ClientLevel.class)
public class ClientLevelMixin extends LevelMixin {
	@Unique
	private GameRuleMap syncedGameRules = GameRuleMap.of();

	@Override
	public GameRuleMap getSyncedGameRules() {
		return this.syncedGameRules;
	}

	@Override
	public void setSyncedGameRules(GameRuleMap gameRules) {
		this.syncedGameRules = gameRules;
	}

	@Override
	public <T> T getSyncedValue(GameRule<T> gameRule) {
		if (!((SyncedGameRule) (Object) gameRule).fabric_isSynced()) {
			throw new IllegalArgumentException("Un-synced game rule %s should not be passed into getSyncedValue".formatted(gameRule));
		}

		return Objects.requireNonNull(this.syncedGameRules.get(gameRule));
	}
}
