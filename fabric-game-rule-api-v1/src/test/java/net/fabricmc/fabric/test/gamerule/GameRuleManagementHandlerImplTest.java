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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.Schemas;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.ServerPropertiesLoader;
import net.minecraft.server.dedicated.management.ManagementLogger;
import net.minecraft.server.dedicated.management.dispatch.GameRuleRpcDispatcher;
import net.minecraft.server.dedicated.management.handler.GameRuleManagementHandler;
import net.minecraft.server.dedicated.management.handler.GameRuleManagementHandlerImpl;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.server.function.FunctionLoader;
import net.minecraft.util.ApiServices;
import net.minecraft.util.math.Direction;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRules;

import net.fabricmc.loader.api.FabricLoader;

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
	private MinecraftDedicatedServer server;
	private boolean serverInitialized = false;

	@Test
	void testUpdateDouble() {
		tryMockServer();
		GameRuleManagementHandler handler = new GameRuleManagementHandlerTestImpl(this.server, MANAGEMENT_LOGGER);

		GameRulesTestMod.FIRE_DAMAGE_CHANGED.set(false);

		GameRuleRpcDispatcher.class_12254<Double> result = handler.updateRule(new GameRuleRpcDispatcher.class_12254<>(GameRulesTestMod.ONE_TO_TEN_DOUBLE, 5.5D), CONNECTION_ID);

		assertEquals("""
				{"type":"fabric:double","value":5.5,"key":"minecraft:one_to_ten_double"}
				""", result);

		Assertions.assertFalse(GameRulesTestMod.FIRE_DAMAGE_CHANGED.get());

		Assertions.assertEquals(5.5D, handler.getRule(GameRulesTestMod.ONE_TO_TEN_DOUBLE));
	}

	@Test
	void testUpdateEnum() {
		tryMockServer();
		GameRuleManagementHandler handler = new GameRuleManagementHandlerTestImpl(this.server, MANAGEMENT_LOGGER);

		GameRulesTestMod.FIRE_DAMAGE_CHANGED.set(false);

		GameRuleRpcDispatcher.class_12254<Direction> result = handler.updateRule(new GameRuleRpcDispatcher.class_12254<>(GameRulesTestMod.CARDINAL_DIRECTION_ENUM, Direction.EAST), CONNECTION_ID);

		assertEquals("""
				{"type":"fabric:enum","value":"EAST","key":"minecraft:cardinal_direction"}
				""", result);

		Assertions.assertFalse(GameRulesTestMod.FIRE_DAMAGE_CHANGED.get());

		Assertions.assertEquals(Direction.EAST, handler.getRule(GameRulesTestMod.CARDINAL_DIRECTION_ENUM));
	}

	@Test
	void testUpdateVanillaBoolean() {
		tryMockServer();
		GameRuleManagementHandler handler = new GameRuleManagementHandlerTestImpl(this.server, MANAGEMENT_LOGGER);

		GameRulesTestMod.FIRE_DAMAGE_CHANGED.set(false);

		GameRuleRpcDispatcher.class_12254<Boolean> result = handler.updateRule(new GameRuleRpcDispatcher.class_12254<>(GameRules.FIRE_DAMAGE, false), CONNECTION_ID);
		//GameRuleEvents.CHANGED_CALLBACK.invoker().accept(GameRules.FIRE_DAMAGE, false, server); // manual call because I suspect mock is messing with the logic

		assertEquals("""
				{"type":"boolean","value":false,"key":"minecraft:fire_damage"}
				""", result);

		Assertions.assertTrue(GameRulesTestMod.FIRE_DAMAGE_CHANGED.get());

		Assertions.assertFalse(handler.getRule(GameRules.FIRE_DAMAGE));
	}

	@Test
	void testUpdateVanillaInt() {
		tryMockServer();
		GameRuleManagementHandler handler = new GameRuleManagementHandlerTestImpl(this.server, MANAGEMENT_LOGGER);

		GameRulesTestMod.FIRE_DAMAGE_CHANGED.set(false);

		GameRuleRpcDispatcher.class_12254<Integer> result = handler.updateRule(new GameRuleRpcDispatcher.class_12254<>(GameRules.RANDOM_TICK_SPEED, 123), CONNECTION_ID);

		assertEquals("""
				{"type":"integer","value":123,"key":"minecraft:random_tick_speed"}
				""", result);

		Assertions.assertFalse(GameRulesTestMod.FIRE_DAMAGE_CHANGED.get());

		Assertions.assertEquals(123, handler.getRule(GameRules.RANDOM_TICK_SPEED));
	}

	private void tryMockServer() {
		if (this.serverInitialized) {
			return;
		}

		this.serverInitialized = true;
		FeatureSet featureSet = FeatureSet.empty();

		SaveProperties saveProperties = mock(SaveProperties.class);
		when(saveProperties.getGameRules()).thenReturn(this.gameRules);
		when(saveProperties.getEnabledFeatures()).thenReturn(featureSet);

		RegistryWrapper.Impl<?> wrapper = mock(RegistryWrapper.Impl.class);
		when(wrapper.getOptional(any(TagKey.class))).thenReturn(Optional.empty());

		Registry registry = mock(Registry.class);
		when(registry.withFeatureFilter(any())).thenReturn(wrapper);
		when(registry.contains(any())).thenReturn(true);

		DynamicRegistryManager.Immutable immutable = mock(DynamicRegistryManager.Immutable.class);
		when(immutable.getOrThrow(any())).thenReturn(registry);

		CombinedDynamicRegistries<ServerDynamicRegistryType> registries = mock(CombinedDynamicRegistries.class);
		when(registries.getCombinedRegistryManager()).thenReturn(immutable);

		ServerRecipeManager recipeManager = mock(ServerRecipeManager.class);

		FunctionLoader functionLoader = mock(FunctionLoader.class);
		when(functionLoader.getTagOrEmpty(any())).thenReturn(List.of());

		DataPackContents dataPackContents = mock(DataPackContents.class);
		when(dataPackContents.getRecipeManager()).thenReturn(recipeManager);
		when(dataPackContents.getFunctionLoader()).thenReturn(functionLoader);

		LifecycledResourceManagerImpl resourceManager = mock(LifecycledResourceManagerImpl.class);
		when(resourceManager.streamResourcePacks()).thenReturn(Stream.of());

		SaveLoader saveLoader = mock(SaveLoader.class);
		when(saveLoader.saveProperties()).thenReturn(saveProperties);
		when(saveLoader.combinedDynamicRegistries()).thenReturn(registries);
		when(saveLoader.dataPackContents()).thenReturn(dataPackContents);
		when(saveLoader.resourceManager()).thenReturn(resourceManager);

		Path path = FabricLoader.getInstance().getGameDir();

		LevelStorage.Session session = mock(LevelStorage.Session.class);
		when(session.getDirectory(any())).thenReturn(path);

		ServerPropertiesLoader propertiesLoader = new ServerPropertiesLoader(Paths.get("server.properties"));

		this.server = new MinecraftDedicatedServer(
				Thread.currentThread(),
				session,
				mock(ResourcePackManager.class),
				saveLoader,
				propertiesLoader,
				Schemas.getFixer(),
				mock(ApiServices.class)
		);
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
