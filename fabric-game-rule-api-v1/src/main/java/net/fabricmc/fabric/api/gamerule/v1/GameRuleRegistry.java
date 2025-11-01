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

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.rule.GameRule;

import net.fabricmc.fabric.impl.gamerule.RuleCategoryExtensions;

/**
 * A utility class which allows for registration of game rules.
 */
public final class GameRuleRegistry {
	private GameRuleRegistry() {
	}

	/**
	 * Registers a {@link GameRule}.
	 *
	 * @param id   the id of the rule
	 * @param rule the rule
	 * @param <T>  the type of rule
	 */
	public static <T> GameRule<T> register(Identifier id, GameRule<T> rule) {
		return Registry.register(Registries.GAME_RULE, id, rule);
	}

	/**
	 * Registers a {@link GameRule} with a custom category.
	 *
	 * @param id the id of the rule
	 * @param rule the rule
	 * @param category the rule type
	 * @param <T>  the type of rule
	 */
	public static <T> GameRule<T> register(Identifier id, GameRule<T> rule, CustomGameRuleCategory category) {
		((RuleCategoryExtensions) (Object) rule).fabric_setCustomCategory(category);
		return Registry.register(Registries.GAME_RULE, id, rule);
	}

	/**
	 * Checks if a name for a game rule is already registered.
	 *
	 * @param ruleName the rule name to test
	 * @return true if the name is taken.
	 */
	public static boolean hasRegistration(String ruleName) {
		return Registries.GAME_RULE.containsId(Identifier.tryParse(ruleName));
	}
}
