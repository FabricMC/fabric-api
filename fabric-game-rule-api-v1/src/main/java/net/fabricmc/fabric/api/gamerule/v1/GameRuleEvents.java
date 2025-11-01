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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.rule.GameRule;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Provides events for updating {@link GameRule}s.
 */
public final class GameRuleEvents {
	private GameRuleEvents() {
	}

	private static final Map<GameRule<?>, Event<ValueUpdate<?>>> VALUE_UPDATES = new HashMap<>();

	public static <T> Event<ValueUpdate<T>> changeCallback(GameRule<T> rule) {
		//noinspection unchecked
		return (Event<ValueUpdate<T>>) (Event<?>) VALUE_UPDATES.computeIfAbsent(rule, gameRule -> {
			//noinspection unchecked
			return (Event<ValueUpdate<?>>) (Event<?>) EventFactory.createArrayBacked(ValueUpdate.class, (Function<ValueUpdate<T>[], ValueUpdate<T>>) callbacks -> (value, server) -> {
				for (ValueUpdate<T> changedCallback : callbacks) {
					changedCallback.onGameRuleUpdated(value, server);
				}
			});
		});
	}

	/**
	 * A functional interface used as a change callback for {@link GameRule} updates.
	 * @param <T> the type of the value
	 */
	@FunctionalInterface
	public interface ValueUpdate<T> {
		/**
		 * Called when a GameRule's value is updated in {@link MinecraftServer}.
		 * @param value the updated value
		 * @param server the server
		 * @see MinecraftServer#onGameRuleUpdated(GameRule, Object)
		 */
		void onGameRuleUpdated(
				T value,
				MinecraftServer server
		);
	}
}
