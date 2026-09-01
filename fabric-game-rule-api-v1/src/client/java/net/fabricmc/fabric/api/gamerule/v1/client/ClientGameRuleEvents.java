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

package net.fabricmc.fabric.api.gamerule.v1.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.gamerules.GameRule;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.gamerule.client.ClientGameRuleEventsImpl;

/**
 * Provides events for updating {@link GameRule}s on the client.
 * @see net.fabricmc.fabric.api.gamerule.v1.FabricSyncedGameRule
 */
public final class ClientGameRuleEvents {
	private ClientGameRuleEvents() {
	}

	public static <T> Event<ValueSync<T>> syncCallback(GameRule<T> gameRule) {
		return ClientGameRuleEventsImpl.syncCallback(gameRule);
	}

	/**
	 * A functional interface used as a sync callback for {@link GameRule} updates.
	 * @param <T> the type of the value
	 */
	@FunctionalInterface
	public interface ValueSync<T> {
		/**
		 * Called when a GameRule's value is synced to the client.
		 * @param value the updated value
		 * @param level the level
		 */
		void onGameRuleSynced(
				T value,
				ClientLevel level
		);
	}
}
