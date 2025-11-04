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

import net.minecraft.SharedConstants;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.jsonrpc.JsonRpcLogger;
import net.minecraft.server.jsonrpc.internalapi.MinecraftGameRuleService;
import net.minecraft.server.jsonrpc.internalapi.MinecraftGameRuleServiceImpl;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import net.minecraft.server.jsonrpc.methods.GameRulesService;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.WorldData;

public class GameRuleManagementHandlerImplTest {
	@BeforeAll
	static void bootstrap() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
		new GameRulesTestMod().onInitialize();
	}

	private static final ClientInfo CONNECTION_ID = new ClientInfo(-1);
	private static final JsonRpcLogger MANAGEMENT_LOGGER = new JsonRpcLogger();
	private final GameRules gameRules = new GameRules(FeatureFlagSet.of());

	@Test
	void testUpdateDouble() {
		DedicatedServer server = mockServer();
		MinecraftGameRuleService handler = new GameRuleManagementHandlerTestImpl(server, MANAGEMENT_LOGGER);

		GameRulesService.GameRuleUpdate<Double> result = handler.updateGameRule(new GameRulesService.GameRuleUpdate<>(GameRulesTestMod.ONE_TO_TEN_DOUBLE, 5.5D), CONNECTION_ID);

		assertEquals("""
				{"type":"fabric:double","value":5.5,"key":"minecraft:one_to_ten_double"}
				""", result);

		verify(server).onGameRuleChanged(
				eq(GameRulesTestMod.ONE_TO_TEN_DOUBLE),
				argThat(rule -> handler.getRuleValue(GameRulesTestMod.ONE_TO_TEN_DOUBLE) == 5.5D));
	}

	@Test
	void testFabricId() {
		DedicatedServer server = mockServer();
		MinecraftGameRuleService handler = new GameRuleManagementHandlerTestImpl(server, MANAGEMENT_LOGGER);

		GameRulesService.GameRuleUpdate<Boolean> result = handler.updateGameRule(new GameRulesService.GameRuleUpdate<>(GameRulesTestMod.RED_BOOLEAN, false), CONNECTION_ID);

		assertEquals("""
				{"type":"boolean","value":false,"key":"fabric:red_boolean"}
				""", result);
	}

	@Test
	void testUpdateEnum() {
		DedicatedServer server = mockServer();
		MinecraftGameRuleService handler = new GameRuleManagementHandlerTestImpl(server, MANAGEMENT_LOGGER);

		GameRulesService.GameRuleUpdate<Direction> result = handler.updateGameRule(new GameRulesService.GameRuleUpdate<>(GameRulesTestMod.CARDINAL_DIRECTION_ENUM_RULE, Direction.EAST), CONNECTION_ID);

		assertEquals("""
				{"type":"fabric:enum","value":"EAST","key":"minecraft:cardinal_direction"}
				""", result);

		verify(server).onGameRuleChanged(
				eq(GameRulesTestMod.CARDINAL_DIRECTION_ENUM_RULE),
				argThat(rule -> handler.getRuleValue(GameRulesTestMod.CARDINAL_DIRECTION_ENUM_RULE) == Direction.EAST)
		);
	}

	@Test
	void testUpdateVanillaBoolean() {
		DedicatedServer server = mockServer();
		MinecraftGameRuleService handler = new GameRuleManagementHandlerTestImpl(server, MANAGEMENT_LOGGER);

		GameRulesService.GameRuleUpdate<Boolean> result = handler.updateGameRule(new GameRulesService.GameRuleUpdate<>(GameRules.FIRE_DAMAGE, false), CONNECTION_ID);

		assertEquals("""
				{"type":"boolean","value":false,"key":"minecraft:fire_damage"}
				""", result);

		verify(server).onGameRuleChanged(
				eq(GameRules.FIRE_DAMAGE),
				argThat(rule -> !handler.getRuleValue(GameRules.FIRE_DAMAGE)));
	}

	@Test
	void testUpdateVanillaInt() {
		DedicatedServer server = mockServer();
		MinecraftGameRuleService handler = new GameRuleManagementHandlerTestImpl(server, MANAGEMENT_LOGGER);

		GameRulesService.GameRuleUpdate<Integer> result = handler.updateGameRule(new GameRulesService.GameRuleUpdate<>(GameRules.RANDOM_TICK_SPEED, 123), CONNECTION_ID);

		assertEquals("""
				{"type":"integer","value":123,"key":"minecraft:random_tick_speed"}
				""", result);

		verify(server).onGameRuleChanged(
				eq(GameRules.RANDOM_TICK_SPEED),
				argThat(rule -> handler.getRuleValue(GameRules.RANDOM_TICK_SPEED) == 123));
	}

	private DedicatedServer mockServer() {
		DedicatedServer server = mock(DedicatedServer.class);
		WorldData saveProperties = mock(WorldData.class);
		when(server.getWorldData()).thenReturn(saveProperties);
		when(saveProperties.getGameRules()).thenReturn(this.gameRules);
		return server;
	}

	private static <T> void assertEquals(@Language("JSON") String expected, GameRulesService.GameRuleUpdate<T> rule) {
		JsonElement jsonElement = GameRulesService.GameRuleUpdate.TYPED_CODEC.encodeStart(JsonOps.INSTANCE, rule).getOrThrow();
		Assertions.assertEquals(expected.trim(), jsonElement.toString());
	}

	private static final class GameRuleManagementHandlerTestImpl extends MinecraftGameRuleServiceImpl {
		private GameRuleManagementHandlerTestImpl(DedicatedServer server, JsonRpcLogger logger) {
			super(server, logger);
		}

		public Stream<GameRule<?>> getAvailableGameRules() {
			return BuiltInRegistries.GAME_RULE.stream().filter(rule -> rule.requiredFeatures().isSubsetOf(FeatureFlagSet.of()));
		}
	}
}
