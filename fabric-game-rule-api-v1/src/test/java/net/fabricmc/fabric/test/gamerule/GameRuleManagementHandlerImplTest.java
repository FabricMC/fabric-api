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

import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.management.ManagementLogger;
import net.minecraft.server.dedicated.management.dispatch.GameRuleRpcDispatcher;
import net.minecraft.server.dedicated.management.handler.GameRuleManagementHandler;
import net.minecraft.server.dedicated.management.handler.GameRuleManagementHandlerImpl;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameRules;

import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;

public class GameRuleManagementHandlerImplTest {
	static {
		new GameRulesTestMod().onInitialize();
	}

	private static final ManagementConnectionId CONNECTION_ID = new ManagementConnectionId(-1);
	private static final ManagementLogger MANAGEMENT_LOGGER = new ManagementLogger();
	private static final GameRules GAME_RULES = new GameRules(FeatureSet.empty());

	@Test
	void testUpdateDouble() {
		MinecraftDedicatedServer server = mock(MinecraftDedicatedServer.class);
		when(server.getGameRules()).thenReturn(GAME_RULES);
		GameRuleManagementHandler handler = new GameRuleManagementHandlerTestImpl(server, MANAGEMENT_LOGGER);

		GameRuleRpcDispatcher.TypedRule result = handler.updateRule(new GameRuleRpcDispatcher.UntypedRule("oneToTenDouble", "5.5"), CONNECTION_ID);

		assertEquals("""
				{"key":"oneToTenDouble","value":"5.5","type":"fabric:double"}
				""", result);

		verify(server).onGameRuleUpdated(
				eq("oneToTenDouble"),
				argThat(rule -> rule instanceof DoubleRule doubleRule && doubleRule.get() == 5.5D));
	}

	@Test
	void testUpdateEnum() {
		MinecraftDedicatedServer server = mock(MinecraftDedicatedServer.class);
		when(server.getGameRules()).thenReturn(GAME_RULES);
		GameRuleManagementHandler handler = new GameRuleManagementHandlerTestImpl(server, MANAGEMENT_LOGGER);

		GameRuleRpcDispatcher.TypedRule result = handler.updateRule(new GameRuleRpcDispatcher.UntypedRule("cardinalDirection", "north"), CONNECTION_ID);

		assertEquals("""
				{"key":"cardinalDirection","value":"NORTH","type":"fabric:enum"}
				""", result);

		verify(server).onGameRuleUpdated(
				eq("cardinalDirection"),
				argThat(rule -> rule instanceof EnumRule<?> enumRule && enumRule.get() == Direction.NORTH)
		);
	}

	@Test
	void testUpdateVanillaBoolean() {
		MinecraftDedicatedServer server = mock(MinecraftDedicatedServer.class);
		when(server.getGameRules()).thenReturn(GAME_RULES);
		GameRuleManagementHandler handler = new GameRuleManagementHandlerTestImpl(server, MANAGEMENT_LOGGER);

		GameRuleRpcDispatcher.TypedRule result = handler.updateRule(new GameRuleRpcDispatcher.UntypedRule("doFireTick", "false"), CONNECTION_ID);

		assertEquals("""
				{"key":"doFireTick","value":"false","type":"boolean"}
				""", result);

		verify(server).onGameRuleUpdated(
				eq("doFireTick"),
				argThat(rule -> rule instanceof GameRules.BooleanRule booleanRule && !booleanRule.get()));
	}

	@Test
	void testUpdateVanillaInt() {
		MinecraftDedicatedServer server = mock(MinecraftDedicatedServer.class);
		when(server.getGameRules()).thenReturn(GAME_RULES);
		GameRuleManagementHandler handler = new GameRuleManagementHandlerTestImpl(server, MANAGEMENT_LOGGER);

		GameRuleRpcDispatcher.TypedRule result = handler.updateRule(new GameRuleRpcDispatcher.UntypedRule("randomTickSpeed", "123"), CONNECTION_ID);

		assertEquals("""
				{"key":"randomTickSpeed","value":"123","type":"integer"}
				""", result);

		verify(server).onGameRuleUpdated(
				eq("randomTickSpeed"),
				argThat(rule -> rule instanceof GameRules.IntRule intRule && intRule.get() == 123));
	}

	private static void assertEquals(@Language("JSON") String expected, GameRuleRpcDispatcher.TypedRule rule) {
		JsonElement jsonElement = GameRuleRpcDispatcher.TypedRule.CODEC.codec().encodeStart(JsonOps.INSTANCE, rule).getOrThrow();
		Assertions.assertEquals(expected.trim(), jsonElement.toString());
	}

	private static final class GameRuleManagementHandlerTestImpl extends GameRuleManagementHandlerImpl {
		private GameRuleManagementHandlerTestImpl(MinecraftDedicatedServer server, ManagementLogger logger) {
			super(server, logger);
		}

		@Override
		public Stream<Map.Entry<GameRules.Key<?>, GameRules.Type<?>>> getRules() {
			return GameRules.streamAllRules(FeatureSet.empty());
		}
	}
}
