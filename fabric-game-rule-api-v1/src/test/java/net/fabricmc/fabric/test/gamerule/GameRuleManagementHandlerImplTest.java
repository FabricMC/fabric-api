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

import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import net.minecraft.registry.Registries;
import net.minecraft.world.rule.GameRule;

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
import net.minecraft.world.rule.GameRules;
import net.minecraft.world.SaveProperties;

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
		SaveProperties saveProperties = mock(SaveProperties.class);
		when(server.getSaveProperties()).thenReturn(saveProperties);
		when(saveProperties.getGameRules()).thenReturn(GAME_RULES);
		GameRuleManagementHandler handler = new GameRuleManagementHandlerTestImpl(server, MANAGEMENT_LOGGER);

		GameRuleRpcDispatcher.class_12254<Double> result = handler.updateRule(new GameRuleRpcDispatcher.class_12254<>(GameRulesTestMod.ONE_TO_TEN_DOUBLE, 5.5D), CONNECTION_ID);

		assertEquals("""
				{"key":"one_to_ten_double","value":"5.5","type":"fabric:double"}
				""", result);

		verify(server).onGameRuleUpdated(
				eq(GameRulesTestMod.ONE_TO_TEN_DOUBLE),
				argThat(rule -> handler.getRule(GameRulesTestMod.ONE_TO_TEN_DOUBLE) == 5.5D));
	}

	@Test
	void testUpdateEnum() {
		MinecraftDedicatedServer server = mock(MinecraftDedicatedServer.class);
		SaveProperties saveProperties = mock(SaveProperties.class);
		when(server.getSaveProperties()).thenReturn(saveProperties);
		when(saveProperties.getGameRules()).thenReturn(GAME_RULES);
		GameRuleManagementHandler handler = new GameRuleManagementHandlerTestImpl(server, MANAGEMENT_LOGGER);

		GameRuleRpcDispatcher.class_12254<Direction> result = handler.updateRule(new GameRuleRpcDispatcher.class_12254<>(GameRulesTestMod.CARDINAL_DIRECTION_ENUM, Direction.NORTH), CONNECTION_ID);

		assertEquals("""
				{"key":"cardinal_direction","value":"NORTH","type":"fabric:enum"}
				""", result);

		verify(server).onGameRuleUpdated(
				eq(GameRulesTestMod.CARDINAL_DIRECTION_ENUM),
				argThat(rule -> handler.getRule(GameRulesTestMod.CARDINAL_DIRECTION_ENUM) == Direction.NORTH)
		);
	}

	@Test
	void testUpdateVanillaBoolean() {
		MinecraftDedicatedServer server = mock(MinecraftDedicatedServer.class);
		SaveProperties saveProperties = mock(SaveProperties.class);
		when(server.getSaveProperties()).thenReturn(saveProperties);
		when(saveProperties.getGameRules()).thenReturn(GAME_RULES);
		GameRuleManagementHandler handler = new GameRuleManagementHandlerTestImpl(server, MANAGEMENT_LOGGER);

		GameRuleRpcDispatcher.class_12254<Boolean> result = handler.updateRule(new GameRuleRpcDispatcher.class_12254<>(GameRules.FIRE_DAMAGE, false), CONNECTION_ID);

		assertEquals("""
				{"key":"fire_damage","value":"false","type":"boolean"}
				""", result);

		verify(server).onGameRuleUpdated(
				eq(GameRules.FIRE_DAMAGE),
				argThat(rule -> !handler.getRule(GameRules.FIRE_DAMAGE)));
	}

	@Test
	void testUpdateVanillaInt() {
		MinecraftDedicatedServer server = mock(MinecraftDedicatedServer.class);
		SaveProperties saveProperties = mock(SaveProperties.class);
		when(server.getSaveProperties()).thenReturn(saveProperties);
		when(saveProperties.getGameRules()).thenReturn(GAME_RULES);
		GameRuleManagementHandler handler = new GameRuleManagementHandlerTestImpl(server, MANAGEMENT_LOGGER);

		GameRuleRpcDispatcher.class_12254<Integer> result = handler.updateRule(new GameRuleRpcDispatcher.class_12254<>(GameRules.RANDOM_TICK_SPEED, 123), CONNECTION_ID);

		assertEquals("""
				{"key":"random_tick_speed","value":"123","type":"integer"}
				""", result);

		verify(server).onGameRuleUpdated(
				eq(GameRules.RANDOM_TICK_SPEED),
				argThat(rule -> handler.getRule(GameRules.RANDOM_TICK_SPEED) == 123));
	}

	private static <T> void assertEquals(@Language("JSON") String expected, GameRuleRpcDispatcher.class_12254<T> rule) {
		JsonElement jsonElement = GameRuleRpcDispatcher.class_12254.field_64088.encodeStart(JsonOps.INSTANCE, rule).getOrThrow();
		Assertions.assertEquals(expected.trim(), jsonElement.toString());
	}

	private static final class GameRuleManagementHandlerTestImpl extends GameRuleManagementHandlerImpl {
		private GameRuleManagementHandlerTestImpl(MinecraftDedicatedServer server, ManagementLogger logger) {
			super(server, logger);
		}

		public Stream<GameRule<?>> getRules() {
			return Registries.GAME_RULE.stream().filter(rule -> rule.getFeatureSet().isSubsetOf(FeatureSet.empty()));
		}
	}
}
