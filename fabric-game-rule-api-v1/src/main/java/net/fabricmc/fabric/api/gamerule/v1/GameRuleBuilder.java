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

import java.util.Objects;
import java.util.function.ToIntFunction;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JavaOps;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import net.minecraft.Optionull;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.minecraft.world.level.gamerules.GameRules;

import net.fabricmc.fabric.impl.gamerule.RuleCategoryExtensions;
import net.fabricmc.fabric.impl.gamerule.RuleTypeExtensions;
import net.fabricmc.fabric.impl.gamerule.rpc.FabricGameRuleType;

/**
 * A utility class containing classes and methods for building {@link GameRule}s.
 * A game rule is a persisted, per server data value which may control gameplay aspects.
 *
 * <p>To register a game rule, you can use {@link GameRuleBuilder#buildAndRegister(Identifier)}.
 * For example, to register a game rule that is an integer where the default value is 1 and the acceptable values are between 0 and 10, one would use the following:
 * <blockquote><pre>
 * public static final GameRule&lt;Integer&gt; EXAMPLE_INT_RULE = GameRuleBuilder.forInteger(1).range(0, 10).buildAndRegister(Identifier.fromNamespaceAndPath("modid", "custom_int_gamerule"));
 * </pre></blockquote>
 *
 * <p>To register a game rule in a custom category, call {@link GameRuleBuilder#category(CustomGameRuleCategory)} on the builder.
 */
@SuppressWarnings("UnusedReturnValue")
@ApiStatus.NonExtendable
public class GameRuleBuilder<T> {
	protected final T defaultValue;

	protected GameRuleCategory category = GameRuleCategory.MISC;
	@Nullable
	protected CustomGameRuleCategory fabricCategory = null;

	protected GameRuleType type = GameRuleType.INT;
	@Nullable
	protected FabricGameRuleType fabricType;

	@Nullable
	protected ArgumentType<T> argumentType;

	protected GameRules.VisitorCaller<T> acceptor;
	protected Codec<T> codec;
	protected ToIntFunction<T> commandResultSupplier;
	protected FeatureFlagSet requiredFeatures = FeatureFlagSet.of();

	protected GameRuleBuilder(T defaultValue) {
		this.defaultValue = defaultValue;
	}

	public static BooleanRuleBuilder forBoolean(boolean defaultValue) {
		return new BooleanRuleBuilder(defaultValue);
	}

	public static IntegerRuleBuilder forInteger(int defaultValue) {
		return new IntegerRuleBuilder(defaultValue);
	}

	public static DoubleRuleBuilder forDouble(double defaultValue) {
		return new DoubleRuleBuilder(defaultValue);
	}

	public static <E extends Enum<E>> EnumRuleBuilder<E> forEnum(E defaultValue) {
		return new EnumRuleBuilder<>(defaultValue);
	}

	public GameRuleBuilder<T> category(GameRuleCategory category) {
		this.category = category;
		return this;
	}

	public GameRuleBuilder<T> category(CustomGameRuleCategory category) {
		category(GameRuleCategory.MISC);
		this.fabricCategory = category;
		return this;
	}

	public GameRuleBuilder<T> codec(Codec<T> codec) {
		this.codec = codec;
		return this;
	}

	/**
	 * Specifies the ArgumentType for the builder. Please note that this is specified by default and is usually not necessary.
	 * @param argumentType the ArgumentType
	 * @return the builder, for chaining
	 */
	public GameRuleBuilder<T> argumentType(ArgumentType<T> argumentType) {
		this.argumentType = argumentType;
		return this;
	}

	public GameRuleBuilder<T> commandResultSupplier(ToIntFunction<T> commandResultSupplier) {
		this.commandResultSupplier = commandResultSupplier;
		return this;
	}

	public GameRuleBuilder<T> requiredFeatures(FeatureFlagSet requiredFeatures) {
		this.requiredFeatures = requiredFeatures;
		return this;
	}

	public GameRule<T> build() {
		Objects.requireNonNull(this.category, "GameRule category cannot be null! Consider using GameRuleCategory.MISC instead.");
		Objects.requireNonNull(this.type, "GameRule type cannot be null! Consider using GameRuleType.INT instead.");

		if (this.fabricType != FabricGameRuleType.ENUM) {
			Objects.requireNonNull(this.argumentType, "GameRule argumentType cannot be null for non-enum rules!");
		}

		Objects.requireNonNull(this.acceptor, "GameRule acceptor cannot be null!");
		Objects.requireNonNull(this.codec, "GameRule codec cannot be null!");
		Objects.requireNonNull(this.commandResultSupplier, "GameRule commandResultSupplier cannot be null!");
		Objects.requireNonNull(this.defaultValue, "GameRule defaultValue cannot be null!");
		Objects.requireNonNull(this.requiredFeatures, "GameRule requiredFeatures cannot be null! Consider using FeatureSet.empty() instead.");

		this.codec.encodeStart(JavaOps.INSTANCE, this.defaultValue).getOrThrow(error -> new IllegalStateException("Failed to serialize default value: " + error));

		GameRule<T> rule = new GameRule<>(this.category, this.type, this.argumentType, this.acceptor, this.codec, this.commandResultSupplier, this.defaultValue, this.requiredFeatures);

		if (this.fabricCategory != null) {
			((RuleCategoryExtensions) (Object) rule).fabric_setCustomCategory(this.fabricCategory);
		}

		if (this.fabricType != null) {
			((RuleTypeExtensions) (Object) rule).fabric_setType(this.fabricType);
		}

		return rule;
	}

	/**
	 * Builds and registers a GameRule.
	 * @param id the id
	 * @return the built GameRule
	 */
	public GameRule<T> buildAndRegister(Identifier id) {
		GameRule<T> rule = this.build();
		return Registry.register(BuiltInRegistries.GAME_RULE, id, rule);
	}

	// RULE VISITORS
	private static void visitDouble(GameRuleTypeVisitor visitor, GameRule<Double> rule) {
		if (visitor instanceof FabricGameRuleVisitor) {
			((FabricGameRuleVisitor) visitor).visitDouble(rule);
		}
	}

	private static <E extends Enum<E>> void visitEnum(GameRuleTypeVisitor visitor, GameRule<E> rule) {
		if (visitor instanceof FabricGameRuleVisitor) {
			((FabricGameRuleVisitor) visitor).visitEnum(rule);
		}
	}

	public static final class BooleanRuleBuilder extends GameRuleBuilder<Boolean> {
		BooleanRuleBuilder(boolean defaultValue) {
			super(defaultValue);
			this.type = GameRuleType.BOOL;
			this.acceptor = GameRuleTypeVisitor::visitBoolean;
			this.argumentType = BoolArgumentType.bool();
			this.codec = Codec.BOOL;
			this.commandResultSupplier = bool -> bool ? 1 : 0;
		}

		@Override
		public BooleanRuleBuilder category(GameRuleCategory category) {
			super.category(category);
			return this;
		}

		@Override
		public BooleanRuleBuilder category(CustomGameRuleCategory category) {
			super.category(category);
			return this;
		}

		@Override
		public BooleanRuleBuilder codec(Codec<Boolean> codec) {
			super.codec(codec);
			return this;
		}

		@Override
		public BooleanRuleBuilder argumentType(ArgumentType<Boolean> argumentType) {
			super.argumentType(argumentType);
			return this;
		}

		public BooleanRuleBuilder commandResultSupplier(ToIntFunction<Boolean> commandResultSupplier) {
			super.commandResultSupplier(commandResultSupplier);
			return this;
		}

		public BooleanRuleBuilder requiredFeatures(FeatureFlagSet requiredFeatures) {
			super.requiredFeatures(requiredFeatures);
			return this;
		}
	}

	public abstract static class NumberRuleBuilder<T extends Number> extends GameRuleBuilder<T> {
		NumberRuleBuilder(T defaultValue) {
			super(defaultValue);
		}

		public abstract NumberRuleBuilder<T> minValue(T minValue);
		public abstract NumberRuleBuilder<T> range(T minValue, T maxValue);
	}

	public static final class IntegerRuleBuilder extends NumberRuleBuilder<Integer> {
		IntegerRuleBuilder(int defaultValue) {
			super(defaultValue);
			this.type = GameRuleType.INT;
			this.acceptor = GameRuleTypeVisitor::visitInteger;
			this.argumentType = IntegerArgumentType.integer();
			this.codec = Codec.INT;
			this.commandResultSupplier = integer -> integer;
		}

		@Override
		public IntegerRuleBuilder category(GameRuleCategory category) {
			super.category(category);
			return this;
		}

		@Override
		public IntegerRuleBuilder category(CustomGameRuleCategory category) {
			super.category(category);
			return this;
		}

		@Override
		public IntegerRuleBuilder codec(Codec<Integer> codec) {
			super.codec(codec);
			return this;
		}

		@Override
		public IntegerRuleBuilder argumentType(ArgumentType<Integer> argumentType) {
			super.argumentType(argumentType);
			return this;
		}

		@Override
		public IntegerRuleBuilder commandResultSupplier(ToIntFunction<Integer> commandResultSupplier) {
			super.commandResultSupplier(commandResultSupplier);
			return this;
		}

		@Override
		public IntegerRuleBuilder requiredFeatures(FeatureFlagSet requiredFeatures) {
			super.requiredFeatures(requiredFeatures);
			return this;
		}

		@Override
		public IntegerRuleBuilder minValue(Integer minValue) {
			return range(minValue, Integer.MAX_VALUE);
		}

		@Override
		public IntegerRuleBuilder range(Integer minValue, Integer maxValue) {
			if (this.defaultValue < minValue || this.defaultValue > maxValue) {
				throw new IllegalArgumentException("Default value is out-of-bounds: " + this.defaultValue);
			}

			return this.argumentType(IntegerArgumentType.integer(minValue, maxValue)).codec(Codec.intRange(minValue, maxValue));
		}
	}

	public static final class DoubleRuleBuilder extends NumberRuleBuilder<Double> {
		DoubleRuleBuilder(double defaultValue) {
			super(defaultValue);
			this.fabricType = FabricGameRuleType.DOUBLE;
			this.acceptor = GameRuleBuilder::visitDouble;
			this.argumentType = DoubleArgumentType.doubleArg();
			this.codec = Codec.DOUBLE;
			this.commandResultSupplier = value -> Double.compare(value, 0.0D);
		}

		@Override
		public DoubleRuleBuilder category(GameRuleCategory category) {
			super.category(category);
			return this;
		}

		@Override
		public DoubleRuleBuilder category(CustomGameRuleCategory category) {
			super.category(category);
			return this;
		}

		@Override
		public DoubleRuleBuilder codec(Codec<Double> codec) {
			super.codec(codec);
			return this;
		}

		@Override
		public DoubleRuleBuilder argumentType(ArgumentType<Double> argumentType) {
			super.argumentType(argumentType);
			return this;
		}

		@Override
		public DoubleRuleBuilder commandResultSupplier(ToIntFunction<Double> commandResultSupplier) {
			super.commandResultSupplier(commandResultSupplier);
			return this;
		}

		@Override
		public DoubleRuleBuilder requiredFeatures(FeatureFlagSet requiredFeatures) {
			super.requiredFeatures(requiredFeatures);
			return this;
		}

		@Override
		public DoubleRuleBuilder minValue(Double minValue) {
			return range(minValue, Double.MAX_VALUE);
		}

		@Override
		public DoubleRuleBuilder range(Double minValue, Double maxValue) {
			if (this.defaultValue < minValue || this.defaultValue > maxValue) {
				throw new IllegalArgumentException("Default value is out-of-bounds: " + this.defaultValue);
			}

			return this.argumentType(DoubleArgumentType.doubleArg(minValue, maxValue)).codec(Codec.doubleRange(minValue, maxValue));
		}
	}

	public static final class EnumRuleBuilder<E extends Enum<E>> extends GameRuleBuilder<E> {
		private E[] supportedValues;

		EnumRuleBuilder(E defaultValue) {
			super(defaultValue);
			this.fabricType = FabricGameRuleType.ENUM;
			this.acceptor = GameRuleBuilder::visitEnum;
			this.argumentType = null;
			this.codec = createEnumCodec(defaultValue.getDeclaringClass());
			this.commandResultSupplier = value -> {
				// For now, we are going to use the ordinal as the command result. Could be changed or set to relate to something else entirely. -i509VCB
				//noinspection Convert2MethodRef
				return value.ordinal();
			};
			this.supportedValues = defaultValue.getDeclaringClass().getEnumConstants();
		}

		@Override
		public EnumRuleBuilder<E> category(GameRuleCategory category) {
			super.category(category);
			return this;
		}

		@Override
		public EnumRuleBuilder<E> category(CustomGameRuleCategory category) {
			super.category(category);
			return this;
		}

		@Override
		public EnumRuleBuilder<E> codec(Codec<E> codec) {
			super.codec(codec);
			return this;
		}

		@Override
		public EnumRuleBuilder<E> argumentType(ArgumentType<E> argumentType) {
			super.argumentType(argumentType);
			return this;
		}

		@Override
		public EnumRuleBuilder<E> commandResultSupplier(ToIntFunction<E> commandResultSupplier) {
			super.commandResultSupplier(commandResultSupplier);
			return this;
		}

		@Override
		public EnumRuleBuilder<E> requiredFeatures(FeatureFlagSet requiredFeatures) {
			super.requiredFeatures(requiredFeatures);
			return this;
		}

		@SafeVarargs
		public final EnumRuleBuilder<E> supportedValues(E... supportedValues) {
			if (Optionull.isNullOrEmpty(supportedValues)) throw new IllegalArgumentException("No values are supported!");

			if (!ArrayUtils.contains(supportedValues, this.defaultValue)) throw new IllegalArgumentException("Supported enum value must include the default " + this.defaultValue);

			this.supportedValues = supportedValues;
			return this;
		}

		@Override
		public GameRule<E> build() {
			GameRule<E> rule = super.build();

			((RuleTypeExtensions) (Object) rule).fabric_setSupportedEnumValues(this.supportedValues);

			return rule;
		}

		private static <E extends Enum<E>> Codec<E> createEnumCodec(Class<E> clazz) {
			return Codec.STRING.comapFlatMap(string -> {
				try {
					return DataResult.success(Enum.valueOf(clazz, string));
				} catch (IllegalArgumentException exception) {
					return DataResult.error(() -> string + " is not a valid value for enum + " + clazz);
				}
			}, Enum::name);
		}
	}
}
