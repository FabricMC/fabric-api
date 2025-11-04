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

package net.fabricmc.fabric.test.gamerule;

import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gamerules.GameRules;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.impl.gamerule.RuleTypeExtensions;

public class GameRuleChangeCallbackGameTest {
	@GameTest
	public void test(GameTestHelper context) {
		ServerLevel serverWorld = context.getLevel();
		MinecraftServer server = serverWorld.getServer();

		GameRules gameRules = serverWorld.getGameRules();

		// Test change callback positive
		GameRulesTestMod.FIRE_DAMAGE_CHANGED.set(false);
		boolean fireDamage = !gameRules.get(GameRules.FIRE_DAMAGE);
		gameRules.set(GameRules.FIRE_DAMAGE, fireDamage, server);
		context.assertValueEqual(gameRules.get(GameRules.FIRE_DAMAGE), fireDamage, Component.literal("GameRules.FIRE_DAMAGE failed to change properly"));
		context.assertTrue(GameRulesTestMod.FIRE_DAMAGE_CHANGED.get(), Component.literal("Change callback failed to detect changing GameRules.FIRE_DAMAGE"));

		// Test change callback negative and enum supported values
		for (int i = 0; i < Direction.values().length; i++) {
			GameRulesTestMod.FIRE_DAMAGE_CHANGED.set(false);
			Direction direction = (((RuleTypeExtensions) (Object) GameRulesTestMod.CARDINAL_DIRECTION_ENUM_RULE).fabric_enumCycle(gameRules.get(GameRulesTestMod.CARDINAL_DIRECTION_ENUM_RULE)));
			gameRules.set(GameRulesTestMod.CARDINAL_DIRECTION_ENUM_RULE, direction, server);
			context.assertValueEqual(gameRules.get(GameRulesTestMod.CARDINAL_DIRECTION_ENUM_RULE), direction, Component.literal("CARDINAL_DIRECTION_ENUM_RULE failed to change properly"));
			context.assertFalse(GameRulesTestMod.FIRE_DAMAGE_CHANGED.get(), Component.literal("Change callback incorrectly detected changing GameRules.FIRE_DAMAGE"));

			Direction.Axis axis = direction.getAxis();
			context.assertTrue(axis == Direction.Axis.X || axis == Direction.Axis.Z, Component.literal("Enum Rule's supported values failed! Expected Axis X or Z, actually got Axis " + axis.name() + " and Direction " + direction.name()));
		}

		// Test change callback negative
		GameRulesTestMod.FIRE_DAMAGE_CHANGED.set(false);
		gameRules.set(GameRulesTestMod.ONE_TO_TEN_DOUBLE, 2.4D, server);
		context.assertValueEqual(gameRules.get(GameRulesTestMod.ONE_TO_TEN_DOUBLE), 2.4D, Component.literal("ONE_TO_TEN_DOUBLE failed to change properly"));
		context.assertFalse(GameRulesTestMod.FIRE_DAMAGE_CHANGED.get(), Component.literal("Change callback incorrectly detected changing GameRules.FIRE_DAMAGE"));

		context.succeed();
	}
}
