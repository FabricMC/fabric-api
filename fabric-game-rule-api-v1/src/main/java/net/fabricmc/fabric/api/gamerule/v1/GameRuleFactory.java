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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.BiConsumer;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import org.jspecify.annotations.Nullable;

import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;

import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.fabricmc.fabric.impl.gamerule.EnumRuleType;
import net.fabricmc.fabric.impl.gamerule.rule.BoundedIntRule;
import net.fabricmc.fabric.mixin.gamerule.GameRulesBooleanRuleAccessor;

/**
 * A utility class containing factory methods to create game rule types.
 * A game rule is a persisted, per server data value which may control gameplay aspects.
 *
 * <p>Some factory methods allow specification of a callback that is invoked when the value of a game rule has changed.
 * Typically, the callback is used for game rules which may influence game logic, such as {@link GameRules#DISABLE_RAIDS disabling raids}.
 *
 * <p>To register a game rule, you can use {@link GameRuleRegistry#register(String, GameRules.Category, GameRules.Type)}.
 * For example, to register a game rule that is an integer where the acceptable values are between 0 and 10, one would use the following:
 * <blockquote><pre>
 * public static final GameRules.Key&lt;GameRules.IntRule&gt; EXAMPLE_INT_RULE = GameRuleRegistry.register("exampleIntRule", GameRules.Category.UPDATES, GameRuleFactory.createIntRule(1, 10));
 * </pre></blockquote>
 *
 * <p>To register a game rule in a custom category, {@link GameRuleRegistry#register(String, CustomGameRuleCategory, GameRules.Type)} should be used.
 *
 * @see GameRuleRegistry
 */
public final class GameRuleFactory {
	/**
	 * Creates a boolean rule type.
	 *
	 * @param defaultValue the default value of the game rule
	 * @return a boolean rule type
	 */
	public static GameRules.Type<GameRules.BooleanRule> createBooleanRule(boolean defaultValue) {
		return createBooleanRule(defaultValue, (server, rule) -> {
		});
	}

	/**
	 * Creates a boolean rule type.
	 *
	 * @param defaultValue the default value of the game rule
	 * @param changedCallback a callback that is invoked when the value of a game rule has changed
	 * @return a boolean rule type
	 */
	public static GameRules.Type<GameRules.BooleanRule> createBooleanRule(boolean defaultValue, BiConsumer<MinecraftServer, GameRules.BooleanRule> changedCallback) {
		return GameRulesBooleanRuleAccessor.invokeCreate(defaultValue, changedCallback);
	}

	/**
	 * Creates an integer rule type.
	 *
	 * @param defaultValue the default value of the game rule
	 * @return an integer rule type
	 */
	public static GameRules.Type<GameRules.IntRule> createIntRule(int defaultValue) {
		return createIntRule(defaultValue, (server, rule) -> {
		});
	}

	/**
	 * Creates an integer rule type.
	 *
	 * @param defaultValue the default value of the game rule
	 * @param minimumValue the minimum value the game rule may accept
	 * @return an integer rule type
	 */
	public static GameRules.Type<GameRules.IntRule> createIntRule(int defaultValue, int minimumValue) {
		return createIntRule(defaultValue, minimumValue, Integer.MAX_VALUE, (server, rule) -> {
		});
	}

	/**
	 * Creates an integer rule type.
	 *
	 * @param defaultValue the default value of the game rule
	 * @param minimumValue the minimum value the game rule may accept
	 * @param changedCallback a callback that is invoked when the value of a game rule has changed
	 * @return an integer rule type
	 */
	public static GameRules.Type<GameRules.IntRule> createIntRule(int defaultValue, int minimumValue, BiConsumer<MinecraftServer, GameRules.IntRule> changedCallback) {
		return createIntRule(defaultValue, minimumValue, Integer.MAX_VALUE, changedCallback);
	}

	/**
	 * Creates an integer rule type.
	 *
	 * @param defaultValue the default value of the game rule
	 * @param minimumValue the minimum value the game rule may accept
	 * @param maximumValue the maximum value the game rule may accept
	 * @return an integer rule type
	 */
	public static GameRules.Type<GameRules.IntRule> createIntRule(int defaultValue, int minimumValue, int maximumValue) {
		return createIntRule(defaultValue, minimumValue, maximumValue, (server, rule) -> {
		});
	}

	/**
	 * Creates an integer rule type.
	 *
	 * @param defaultValue the default value of the game rule
	 * @param changedCallback a callback that is invoked when the value of a game rule has changed
	 * @return an integer rule type
	 */
	public static GameRules.Type<GameRules.IntRule> createIntRule(int defaultValue, BiConsumer<MinecraftServer, GameRules.IntRule> changedCallback) {
		return createIntRule(defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, changedCallback);
	}

	/**
	 * Creates an integer rule type.
	 *
	 * @param defaultValue the default value of the game rule
	 * @param minimumValue the minimum value the game rule may accept
	 * @param maximumValue the maximum value the game rule may accept
	 * @param changedCallback a callback that is invoked when the value of a game rule has changed
	 * @return an integer rule type
	 */
	public static GameRules.Type<GameRules.IntRule> createIntRule(int defaultValue, int minimumValue, int maximumValue, @Nullable BiConsumer<MinecraftServer, GameRules.IntRule> changedCallback) {
		return new GameRules.Type<>(
				() -> IntegerArgumentType.integer(minimumValue, maximumValue),
				type -> new BoundedIntRule(type, defaultValue, minimumValue, maximumValue), // Internally use a bounded int rule
				changedCallback,
				GameRules.Visitor::visitInt,
				GameRules.IntRule.class,
				FeatureSet.empty()
		);
	}

	/**
	 * Creates a double rule type.
	 *
	 * @param defaultValue the default value of the game rule
	 * @return a double rule type
	 */
	public static GameRules.Type<DoubleRule> createDoubleRule(double defaultValue) {
		return createDoubleRule(defaultValue, (server, rule) -> {
		});
	}

	/**
	 * Creates a double rule type.
	 *
	 * @param defaultValue the default value of the game rule
	 * @param minimumValue the minimum value the game rule may accept
	 * @return a double rule type
	 */
	public static GameRules.Type<DoubleRule> createDoubleRule(double defaultValue, double minimumValue) {
		return createDoubleRule(defaultValue, minimumValue, Double.MAX_VALUE, (server, rule) -> {
		});
	}

	/**
	 * Creates a double rule type.
	 *
	 * @param defaultValue the default value of the game rule
	 * @param minimumValue the minimum value the game rule may accept
	 * @param changedCallback a callback that is invoked when the value of a game rule has changed
	 * @return a double rule type
	 */
	public static GameRules.Type<DoubleRule> createDoubleRule(double defaultValue, double minimumValue, BiConsumer<MinecraftServer, DoubleRule> changedCallback) {
		return createDoubleRule(defaultValue, minimumValue, Double.MAX_VALUE, changedCallback);
	}

	/**
	 * Creates a double rule type.
	 *
	 * @param defaultValue the default value of the game rule
	 * @param minimumValue the minimum value the game rule may accept
	 * @param maximumValue the maximum value the game rule may accept
	 * @return a double rule type
	 */
	public static GameRules.Type<DoubleRule> createDoubleRule(double defaultValue, double minimumValue, double maximumValue) {
		return createDoubleRule(defaultValue, minimumValue, maximumValue, (server, rule) -> {
		});
	}

	/**
	 * Creates a double rule type.
	 *
	 * @param defaultValue the default value of the game rule
	 * @param changedCallback a callback that is invoked when the value of a game rule has changed
	 * @return a double rule type
	 */
	public static GameRules.Type<DoubleRule> createDoubleRule(double defaultValue, BiConsumer<MinecraftServer, DoubleRule> changedCallback) {
		return createDoubleRule(defaultValue, Double.MIN_VALUE, Double.MAX_VALUE, changedCallback);
	}

	/**
	 * Creates a double rule type.
	 *
	 * @param defaultValue the default value of the game rule
	 * @param minimumValue the minimum value the game rule may accept
	 * @param maximumValue the maximum value the game rule may accept
	 * @param changedCallback a callback that is invoked when the value of a game rule has changed
	 * @return a double rule type
	 */
	public static GameRules.Type<DoubleRule> createDoubleRule(double defaultValue, double minimumValue, double maximumValue, BiConsumer<MinecraftServer, DoubleRule> changedCallback) {
		return new GameRules.Type<>(
				() -> DoubleArgumentType.doubleArg(minimumValue, maximumValue),
				type -> new DoubleRule(type, defaultValue, minimumValue, maximumValue),
				changedCallback,
				GameRuleFactory::visitDouble,
				DoubleRule.class,
				FeatureSet.empty()
		);
	}

	/**
	 * Creates an enum rule type.
	 *
	 * <p>All enum values are supported.
	 *
	 * @param defaultValue the default value of the game rule
	 * @param <E> the type of enum this game rule stores
	 * @return an enum rule type
	 */
	public static <E extends Enum<E>> GameRules.Type<EnumRule<E>> createEnumRule(E defaultValue) {
		return createEnumRule(defaultValue, (server, rule) -> {
		});
	}

	/**
	 * Creates an enum rule type.
	 *
	 * <p>All enum values are supported.
	 *
	 * @param defaultValue the default value of the game rule
	 * @param changedCallback a callback that is invoked when the value of a game rule has changed
	 * @param <E> the type of enum this game rule stores
	 * @return an enum rule type
	 */
	public static <E extends Enum<E>> GameRules.Type<EnumRule<E>> createEnumRule(E defaultValue, BiConsumer<MinecraftServer, EnumRule<E>> changedCallback) {
		return createEnumRule(defaultValue, defaultValue.getDeclaringClass().getEnumConstants(), changedCallback);
	}

	/**
	 * Creates an enum rule type.
	 *
	 * @param defaultValue the default value of the game rule
	 * @param supportedValues the values the game rule may support
	 * @param <E> the type of enum this game rule stores
	 * @return an enum rule type
	 */
	public static <E extends Enum<E>> GameRules.Type<EnumRule<E>> createEnumRule(E defaultValue, E[] supportedValues) {
		return createEnumRule(defaultValue, supportedValues, (server, rule) -> {
		});
	}

	/**
	 * Creates an enum rule type.
	 *
	 * @param defaultValue the default value of the game rule
	 * @param supportedValues the values the game rule may support
	 * @param changedCallback a callback that is invoked when the value of a game rule has changed.
	 * @param <E> the type of enum this game rule stores
	 * @return an enum rule type
	 */
	public static <E extends Enum<E>> GameRules.Type<EnumRule<E>> createEnumRule(E defaultValue, E[] supportedValues, BiConsumer<MinecraftServer, EnumRule<E>> changedCallback) {
		checkNotNull(defaultValue, "Default rule value cannot be null");
		checkNotNull(supportedValues, "Supported Values cannot be null");

		if (supportedValues.length == 0) {
			throw new IllegalArgumentException("Cannot register an enum rule where no values are supported");
		}

		return new EnumRuleType<>(
				type -> new EnumRule<>(type, defaultValue, supportedValues),
				changedCallback,
				supportedValues,
				GameRuleFactory::visitEnum,
				(Class<EnumRule<E>>) (Object) EnumRule.class
		);
	}

	// RULE VISITORS - INTERNAL

	private static void visitDouble(GameRules.Visitor visitor, GameRules.Key<DoubleRule> key, GameRules.Type<DoubleRule> type) {
		if (visitor instanceof FabricGameRuleVisitor) {
			((FabricGameRuleVisitor) visitor).visitDouble(key, type);
		}
	}

	private static <E extends Enum<E>> void visitEnum(GameRules.Visitor visitor, GameRules.Key<EnumRule<E>> key, GameRules.Type<EnumRule<E>> type) {
		if (visitor instanceof FabricGameRuleVisitor) {
			((FabricGameRuleVisitor) visitor).visitEnum(key, type);
		}
	}

	private GameRuleFactory() {
	}
}
