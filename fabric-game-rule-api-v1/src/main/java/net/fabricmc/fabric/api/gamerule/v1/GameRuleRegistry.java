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

import net.fabricmc.fabric.impl.gamerule.RuleCategoryExtensions;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.rule.GameRule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class which allows for registration of game rules.
 *
 * Note that {@link net.minecraft.world.rule.GameRules} already
 * contains many registration methods not found here.
 */
public final class GameRuleRegistry {

	public static final Logger LOGGER = LoggerFactory.getLogger(GameRuleRegistry.class);

	private GameRuleRegistry() {
	}

	/**
	 * Registers a {@link GameRule}.
	 *
	 * @param name   the name of the rule
	 * @param rule the rule
	 * @param <T>  the type of rule
	 */
	public static <T> GameRule<T> register(String name, GameRule<T> rule) {
		return Registry.register(Registries.GAME_RULE, name, rule);
	}

	/**
	 * Registers a {@link GameRule} with a custom category.
	 *
	 * @param name 	the name of the rule
	 * @param rule the rule
	 * @param category the rule type
	 * @param <T>  the type of rule
	 */
	public static <T> GameRule<T> register(String name, GameRule<T> rule, CustomGameRuleCategory category) {
		((RuleCategoryExtensions) (Object) rule).fabric_setCustomCategory(category);
		return Registry.register(Registries.GAME_RULE, name, rule);
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
