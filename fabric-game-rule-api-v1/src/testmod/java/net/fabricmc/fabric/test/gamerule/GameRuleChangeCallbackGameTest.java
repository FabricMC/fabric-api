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

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.world.rule.GameRules;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.impl.gamerule.RuleTypeExtensions;

public class GameRuleChangeCallbackGameTest {
	@GameTest
	public void test(TestContext context) {
		ServerWorld serverWorld = context.getWorld();
		MinecraftServer server = serverWorld.getServer();

		GameRules gameRules = serverWorld.getGameRules();

		// Test change callback positive
		GameRulesTestMod.FIRE_DAMAGE_CHANGED.set(false);
		boolean fireDamage = !gameRules.getValue(GameRules.FIRE_DAMAGE);
		gameRules.setValue(GameRules.FIRE_DAMAGE, fireDamage, server);
		context.assertEquals(gameRules.getValue(GameRules.FIRE_DAMAGE), fireDamage, Text.literal("GameRules.FIRE_DAMAGE failed to change properly"));
		context.assertTrue(GameRulesTestMod.FIRE_DAMAGE_CHANGED.get(), Text.literal("Change callback failed to detect changing GameRules.FIRE_DAMAGE"));

		// Test change callback negative and enum supported values
		for (int i = 0; i < Direction.values().length; i++) {
			GameRulesTestMod.FIRE_DAMAGE_CHANGED.set(false);
			Direction direction = (((RuleTypeExtensions) (Object) GameRulesTestMod.CARDINAL_DIRECTION_ENUM_RULE).fabric_enumCycle(gameRules.getValue(GameRulesTestMod.CARDINAL_DIRECTION_ENUM_RULE)));
			gameRules.setValue(GameRulesTestMod.CARDINAL_DIRECTION_ENUM_RULE, direction, server);
			context.assertEquals(gameRules.getValue(GameRulesTestMod.CARDINAL_DIRECTION_ENUM_RULE), direction, Text.literal("CARDINAL_DIRECTION_ENUM_RULE failed to change properly"));
			context.assertFalse(GameRulesTestMod.FIRE_DAMAGE_CHANGED.get(), Text.literal("Change callback incorrectly detected changing GameRules.FIRE_DAMAGE"));

			Direction.Axis axis = direction.getAxis();
			context.assertTrue(axis == Direction.Axis.X || axis == Direction.Axis.Z, Text.literal("Enum Rule's supported values failed! Expected Axis X or Z, actually got Axis " + axis.name() + " and Direction " + direction.name()));
		}

		// Test change callback negative
		GameRulesTestMod.FIRE_DAMAGE_CHANGED.set(false);
		gameRules.setValue(GameRulesTestMod.ONE_TO_TEN_DOUBLE, 2.4D, server);
		context.assertEquals(gameRules.getValue(GameRulesTestMod.ONE_TO_TEN_DOUBLE), 2.4D, Text.literal("ONE_TO_TEN_DOUBLE failed to change properly"));
		context.assertFalse(GameRulesTestMod.FIRE_DAMAGE_CHANGED.get(), Text.literal("Change callback incorrectly detected changing GameRules.FIRE_DAMAGE"));

		context.complete();
	}
}
