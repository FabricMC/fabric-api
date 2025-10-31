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

import java.util.function.ToIntFunction;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;

import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.dedicated.management.dispatch.GameRuleType;
import net.minecraft.world.Category;
import net.minecraft.world.Visitor;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRules;

import net.fabricmc.fabric.impl.gamerule.RuleCategoryExtensions;
import net.fabricmc.fabric.impl.gamerule.RuleTypeExtensions;
import net.fabricmc.fabric.impl.gamerule.rpc.FabricGameRuleType;

/**
 * A utility class containing factory methods to create game rule types.
 * A game rule is a persisted, per server data value which may control gameplay aspects.
 *
 * <p>To register a game rule, you can use {@link GameRuleRegistry#register(String, GameRule)} or any register method from {@link GameRules}.
 * For example, to register a game rule that is an integer where the default value is 1 and the acceptable values are between 0 and 10, one would use the following:
 * <blockquote><pre>
 * public static final GameRule&lt;Integer&gt; EXAMPLE_INT_RULE = GameRules.registerIntRule("example_int_rule", Category.MISC, 1, 0, 10);
 * </pre></blockquote>
 * Please note that all register methods in {@link GameRules} internally construct and register the rule.
 *
 * <p>To register a game rule in a custom category, {@link GameRuleRegistry#register(String, GameRule, CustomGameRuleCategory)} should be used.
 * Alternatively, cast the {@link GameRule} to {@link RuleCategoryExtensions} and call {@link RuleCategoryExtensions#fabric_setCustomCategory(CustomGameRuleCategory)}.
 *
 * @see GameRuleRegistry
 */
public final class GameRuleFactory {
	private GameRuleFactory() {
	}

	// BOOLEAN
	public static GameRule<Boolean> createBooleanRule(boolean defaultValue) {
		return createBooleanRule(Category.MISC, defaultValue);
	}

	public static GameRule<Boolean> createBooleanRule(Category category, boolean defaultValue) {
		return create(category, GameRuleType.BOOL, BoolArgumentType.bool(), Codec.BOOL, defaultValue, FeatureSet.empty(), Visitor::visitBoolean, (bool) -> bool ? 1 : 0);
	}

	// INTEGER
	public static GameRule<Integer> createIntRule(int defaultValue, int minValue) {
		return createIntRule(Category.MISC, defaultValue, minValue);
	}

	public static GameRule<Integer> createIntRule(Category category, int defaultValue, int minValue) {
		return createIntRule(category, defaultValue, minValue, Integer.MAX_VALUE, FeatureSet.empty());
	}

	public static GameRule<Integer> createIntRule(int defaultValue, int minValue, int maxValue) {
		return createIntRule(Category.MISC, defaultValue, minValue, maxValue);
	}

	public static GameRule<Integer> createIntRule(Category category, int defaultValue, int minValue, int maxValue) {
		return createIntRule(category, defaultValue, minValue, maxValue, FeatureSet.empty());
	}

	public static GameRule<Integer> createIntRule(int defaultValue, int minValue, int maxValue, FeatureSet featureSet) {
		return createIntRule(Category.MISC, defaultValue, minValue, maxValue, featureSet);
	}

	public static GameRule<Integer> createIntRule(Category category, int defaultValue, int minValue, int maxValue, FeatureSet featureSet) {
		return create(category, GameRuleType.INT, IntegerArgumentType.integer(minValue, maxValue), Codec.intRange(minValue, maxValue), defaultValue, featureSet, Visitor::visitInt, (integer) -> integer);
	}

	// DOUBLE
	public static GameRule<Double> createDoubleRule(double defaultValue, double minValue) {
		return createDoubleRule(Category.MISC, defaultValue, minValue);
	}

	public static GameRule<Double> createDoubleRule(Category category, double defaultValue, double minValue) {
		return createDoubleRule(category, defaultValue, minValue, Double.MAX_VALUE, FeatureSet.empty());
	}

	public static GameRule<Double> createDoubleRule(double defaultValue, double minValue, double maxValue) {
		return createDoubleRule(Category.MISC, defaultValue, minValue, maxValue);
	}

	public static GameRule<Double> createDoubleRule(Category category, double defaultValue, double minValue, double maxValue) {
		return createDoubleRule(category, defaultValue, minValue, maxValue, FeatureSet.empty());
	}

	public static GameRule<Double> createDoubleRule(double defaultValue, double minValue, double maxValue, FeatureSet featureSet) {
		return createDoubleRule(Category.MISC, defaultValue, minValue, maxValue, featureSet);
	}

	public static GameRule<Double> createDoubleRule(Category category, double defaultValue, double minValue, double maxValue, FeatureSet featureSet) {
		return create(category, FabricGameRuleType.DOUBLE, DoubleArgumentType.doubleArg(minValue, maxValue), Codec.doubleRange(minValue, maxValue), defaultValue, featureSet, GameRuleFactory::visitDouble, (value) -> Double.compare(value, 0.0D));
	}

	// ENUM
	public static <E extends Enum<E>> GameRule<E> createEnumRule(E defaultValue, Codec<E> codec) {
		return createEnumRule(Category.MISC, defaultValue, codec);
	}

	public static <E extends Enum<E>> GameRule<E> createEnumRule(Category category, E defaultValue, Codec<E> codec) {
		return createEnumRule(category, defaultValue, defaultValue.getDeclaringClass().getEnumConstants(), codec, FeatureSet.empty());
	}

	public static <E extends Enum<E>> GameRule<E> createEnumRule(E defaultValue, E[] supportedValues, Codec<E> codec) {
		return createEnumRule(Category.MISC, defaultValue, supportedValues, codec, FeatureSet.empty());
	}

	public static <E extends Enum<E>> GameRule<E> createEnumRule(Category category, E defaultValue, E[] supportedValues, Codec<E> codec) {
		return createEnumRule(category, defaultValue, supportedValues, codec, FeatureSet.empty());
	}

	public static <E extends Enum<E>> GameRule<E> createEnumRule(Category category, E defaultValue, E[] supportedValues, Codec<E> codec, FeatureSet featureSet) {
		checkNotNull(defaultValue, "Default rule value cannot be null");
		checkNotNull(supportedValues, "Supported Values cannot be null");

		if (supportedValues.length == 0) {
			throw new IllegalArgumentException("Cannot register an enum rule where no values are supported");
		}

		GameRule<E> enumRule = create(category,
				FabricGameRuleType.ENUM,
				null, // passing in null here is actually fine because we mixin everywhere this is used so an NPE should never occur
				codec,
				defaultValue,
				featureSet,
				GameRuleFactory::visitEnum,
				(value -> {
					// For now we are gonna use the ordinal as the command result. Could be changed or set to relate to something else entirely.
					return value.ordinal();
				}
			));

		((RuleTypeExtensions) (Object) enumRule).fabric_setSupportedEnumValues(supportedValues);

		return enumRule;
	}

	public static <T> GameRule<T> create(Category category, GameRuleType type, ArgumentType<T> argumentType, Codec<T> codec, T defaultValue, FeatureSet featureSet, GameRules.Acceptor<T> acceptor, ToIntFunction<T> commandResultSupplier) {
		return new GameRule<>(category, type, argumentType, acceptor, codec, commandResultSupplier, defaultValue, featureSet);
	}

	public static <T> GameRule<T> create(Category category, FabricGameRuleType type, ArgumentType<T> argumentType, Codec<T> codec, T defaultValue, FeatureSet featureSet, GameRules.Acceptor<T> acceptor, ToIntFunction<T> commandResultSupplier) {
		GameRule<T> rule = new GameRule<>(category, GameRuleType.INT, argumentType, acceptor, codec, commandResultSupplier, defaultValue, featureSet);
		((RuleTypeExtensions) (Object) rule).fabric_setType(type);
		return rule;
	}

	// RULE VISITORS - INTERNAL

	private static void visitDouble(Visitor visitor, GameRule<Double> rule) {
		if (visitor instanceof FabricGameRuleVisitor) {
			((FabricGameRuleVisitor) visitor).visitDouble(rule);
		}
	}

	private static <E extends Enum<E>> void visitEnum(Visitor visitor, GameRule<E> rule) {
		if (visitor instanceof FabricGameRuleVisitor) {
			((FabricGameRuleVisitor) visitor).visitEnum(rule);
		}
	}
}
