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

package net.fabricmc.fabric.impl.gamerule;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.level.gamerules.GameRule;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleEvents;

public final class GameRuleEventsImpl {
	private GameRuleEventsImpl() {
	}

	private static final Map<GameRule<?>, Event<GameRuleEvents.ValueUpdate<?>>> VALUE_UPDATES = new IdentityHashMap<>();

	public static <T> Event<GameRuleEvents.ValueUpdate<T>> changeCallback(GameRule<T> rule) {
		//noinspection unchecked
		return (Event<GameRuleEvents.ValueUpdate<T>>) (Event<?>) VALUE_UPDATES.computeIfAbsent(rule, gameRule -> {
			//noinspection unchecked
			return (Event<GameRuleEvents.ValueUpdate<?>>) (Event<?>) EventFactory.createArrayBacked(GameRuleEvents.ValueUpdate.class, (Function<GameRuleEvents.ValueUpdate<T>[], GameRuleEvents.ValueUpdate<T>>) callbacks -> (value, server) -> {
				for (GameRuleEvents.ValueUpdate<T> changedCallback : callbacks) {
					changedCallback.onGameRuleUpdated(value, server);
				}
			});
		});
	}

	@Nullable
	public static <T> Event<GameRuleEvents.ValueUpdate<T>> getValueUpdate(GameRule<T> rule) {
		//noinspection unchecked
		return (Event<GameRuleEvents.ValueUpdate<T>>) (Event<?>) VALUE_UPDATES.get(rule);
	}
}
