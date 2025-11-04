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
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.registry.Registries;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.management.ManagementLogger;
import net.minecraft.server.dedicated.management.dispatch.GameRuleRpcDispatcher;
import net.minecraft.server.dedicated.management.handler.GameRuleManagementHandler;
import net.minecraft.server.dedicated.management.handler.GameRuleManagementHandlerImpl;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.util.math.Direction;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRules;

public class GameRuleManagementHandlerImplTest {
	@BeforeAll
	static void bootstrap() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
		new GameRulesTestMod().onInitialize();
	}

	private static final ManagementConnectionId CONNECTION_ID = new ManagementConnectionId(-1);
	private static final ManagementLogger MANAGEMENT_LOGGER = new ManagementLogger();
	private final GameRules gameRules = new GameRules(FeatureSet.empty());

	@Test
	void testUpdateDouble() {
		MinecraftDedicatedServer server = mockServer();
		GameRuleManagementHandler handler = new GameRuleManagementHandlerTestImpl(server, MANAGEMENT_LOGGER);

		GameRuleRpcDispatcher.RuleEntry<Double> result = handler.updateRule(new GameRuleRpcDispatcher.RuleEntry<>(GameRulesTestMod.ONE_TO_TEN_DOUBLE, 5.5D), CONNECTION_ID);

		assertEquals("""
				{"type":"fabric:double","value":5.5,"key":"minecraft:one_to_ten_double"}
				""", result);

		verify(server).onGameRuleUpdated(
				eq(GameRulesTestMod.ONE_TO_TEN_DOUBLE),
				argThat(rule -> handler.getValue(GameRulesTestMod.ONE_TO_TEN_DOUBLE) == 5.5D));
	}

	@Test
	void testFabricId() {
		MinecraftDedicatedServer server = mockServer();
		GameRuleManagementHandler handler = new GameRuleManagementHandlerTestImpl(server, MANAGEMENT_LOGGER);

		GameRuleRpcDispatcher.RuleEntry<Boolean> result = handler.updateRule(new GameRuleRpcDispatcher.RuleEntry<>(GameRulesTestMod.RED_BOOLEAN, false), CONNECTION_ID);

		assertEquals("""
				{"type":"boolean","value":false,"key":"fabric:red_boolean"}
				""", result);
	}

	@Test
	void testUpdateEnum() {
		MinecraftDedicatedServer server = mockServer();
		GameRuleManagementHandler handler = new GameRuleManagementHandlerTestImpl(server, MANAGEMENT_LOGGER);

		GameRuleRpcDispatcher.RuleEntry<Direction> result = handler.updateRule(new GameRuleRpcDispatcher.RuleEntry<>(GameRulesTestMod.CARDINAL_DIRECTION_ENUM_RULE, Direction.EAST), CONNECTION_ID);

		assertEquals("""
				{"type":"fabric:enum","value":"EAST","key":"minecraft:cardinal_direction"}
				""", result);

		verify(server).onGameRuleUpdated(
				eq(GameRulesTestMod.CARDINAL_DIRECTION_ENUM_RULE),
				argThat(rule -> handler.getValue(GameRulesTestMod.CARDINAL_DIRECTION_ENUM_RULE) == Direction.EAST)
		);
	}

	@Test
	void testUpdateVanillaBoolean() {
		MinecraftDedicatedServer server = mockServer();
		GameRuleManagementHandler handler = new GameRuleManagementHandlerTestImpl(server, MANAGEMENT_LOGGER);

		GameRuleRpcDispatcher.RuleEntry<Boolean> result = handler.updateRule(new GameRuleRpcDispatcher.RuleEntry<>(GameRules.FIRE_DAMAGE, false), CONNECTION_ID);

		assertEquals("""
				{"type":"boolean","value":false,"key":"minecraft:fire_damage"}
				""", result);

		verify(server).onGameRuleUpdated(
				eq(GameRules.FIRE_DAMAGE),
				argThat(rule -> !handler.getValue(GameRules.FIRE_DAMAGE)));
	}

	@Test
	void testUpdateVanillaInt() {
		MinecraftDedicatedServer server = mockServer();
		GameRuleManagementHandler handler = new GameRuleManagementHandlerTestImpl(server, MANAGEMENT_LOGGER);

		GameRuleRpcDispatcher.RuleEntry<Integer> result = handler.updateRule(new GameRuleRpcDispatcher.RuleEntry<>(GameRules.RANDOM_TICK_SPEED, 123), CONNECTION_ID);

		assertEquals("""
				{"type":"integer","value":123,"key":"minecraft:random_tick_speed"}
				""", result);

		verify(server).onGameRuleUpdated(
				eq(GameRules.RANDOM_TICK_SPEED),
				argThat(rule -> handler.getValue(GameRules.RANDOM_TICK_SPEED) == 123));
	}

	private MinecraftDedicatedServer mockServer() {
		MinecraftDedicatedServer server = mock(MinecraftDedicatedServer.class);
		SaveProperties saveProperties = mock(SaveProperties.class);
		when(server.getSaveProperties()).thenReturn(saveProperties);
		when(saveProperties.getGameRules()).thenReturn(this.gameRules);
		return server;
	}

	private static <T> void assertEquals(@Language("JSON") String expected, GameRuleRpcDispatcher.RuleEntry<T> rule) {
		JsonElement jsonElement = GameRuleRpcDispatcher.RuleEntry.TYPED_CODEC.encodeStart(JsonOps.INSTANCE, rule).getOrThrow();
		Assertions.assertEquals(expected.trim(), jsonElement.toString());
	}

	private static final class GameRuleManagementHandlerTestImpl extends GameRuleManagementHandlerImpl {
		private GameRuleManagementHandlerTestImpl(MinecraftDedicatedServer server, ManagementLogger logger) {
			super(server, logger);
		}

		public Stream<GameRule<?>> getRules() {
			return Registries.GAME_RULE.stream().filter(rule -> rule.getRequiredFeatures().isSubsetOf(FeatureSet.empty()));
		}
	}
}
