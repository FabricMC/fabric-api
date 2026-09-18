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

package net.fabricmc.fabric.impl.gamerule.client;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.level.gamerules.GameRule;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.gamerule.v1.client.ClientGameRuleEvents;

public final class ClientGameRuleEventsImpl {
	private ClientGameRuleEventsImpl() {
	}

	private static final Map<GameRule<?>, Event<ClientGameRuleEvents.ValueSync<?>>> VALUE_SYNCS = new IdentityHashMap<>();

	public static <T> Event<ClientGameRuleEvents.ValueSync<T>> syncCallback(GameRule<T> rule) {
		//noinspection unchecked
		return (Event<ClientGameRuleEvents.ValueSync<T>>) (Event<?>) VALUE_SYNCS.computeIfAbsent(rule, gameRule -> {
			//noinspection unchecked
			return (Event<ClientGameRuleEvents.ValueSync<?>>) (Event<?>) EventFactory.createArrayBacked(ClientGameRuleEvents.ValueSync.class, (Function<ClientGameRuleEvents.ValueSync<T>[], ClientGameRuleEvents.ValueSync<T>>) callbacks -> (value, level) -> {
				for (ClientGameRuleEvents.ValueSync<T> changedCallback : callbacks) {
					changedCallback.onGameRuleSynced(value, level);
				}
			});
		});
	}

	@Nullable
	public static <T> Event<ClientGameRuleEvents.ValueSync<T>> getValueSync(GameRule<T> rule) {
		//noinspection unchecked
		return (Event<ClientGameRuleEvents.ValueSync<T>>) (Event<?>) VALUE_SYNCS.get(rule);
	}
}
