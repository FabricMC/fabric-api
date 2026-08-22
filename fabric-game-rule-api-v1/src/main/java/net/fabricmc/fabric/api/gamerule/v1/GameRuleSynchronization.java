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

package net.fabricmc.fabric.api.gamerule.v1;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import net.minecraft.world.level.gamerules.GameRule;

import net.fabricmc.fabric.impl.gamerule.sync.GameRuleSynchronizationImpl;

/**
 * A utility class for syncing game rules.
 *
 * <p>This is mainly for vanilla or other mods' game rules. For custom game rules, {@link GameRuleBuilder#synced() the builder method} should be used instead.
 */
public final class GameRuleSynchronization {
	private GameRuleSynchronization() {
	}

	public static void synchronizeGameRule(GameRule<?> gameRule) {
		Objects.requireNonNull(gameRule, "game rule can't be null!");
		GameRuleSynchronizationImpl.addSynchronizedGameRule(gameRule);
	}

	public static void synchronizeGameRules(GameRule<?>... gameRules) {
		synchronizeGameRules(List.of(gameRules));
	}

	public static void synchronizeGameRules(Collection<GameRule<?>> gameRules) {
		Objects.requireNonNull(gameRules, "game rule collection can't be null!");

		for (GameRule<?> gameRule : gameRules) {
			synchronizeGameRule(gameRule);
		}
	}
}
