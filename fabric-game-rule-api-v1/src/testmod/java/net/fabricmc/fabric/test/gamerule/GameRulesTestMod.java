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

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRules;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;

public class GameRulesTestMod implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameRulesTestMod.class);
	private static final Direction[] CARDINAL_DIRECTIONS = Arrays.stream(Direction.values()).filter(direction -> direction != Direction.UP && direction != Direction.DOWN).toArray(Direction[]::new);
	public static final CustomGameRuleCategory GREEN_CATEGORY = new CustomGameRuleCategory(Identifier.of("fabric", "green"), Text.literal("This One is Green").styled(style -> style.withBold(true).withColor(Formatting.DARK_GREEN)));
	public static final CustomGameRuleCategory RED_CATEGORY = new CustomGameRuleCategory(Identifier.of("fabric", "red"), Text.literal("This One is Red").styled(style -> style.withBold(true).withColor(Formatting.DARK_RED)));

	// Bounded, Integer, Double and Float rules
	public static final GameRule<Integer> POSITIVE_ONLY_TEST_INT = register("positive_only_test_integer", GameRuleFactory.createIntRule(2, 0));
	public static final GameRule<Double> ONE_TO_TEN_DOUBLE = register("one_to_ten_double", GameRuleFactory.createDoubleRule(1.0D, 1.0D, 10.0D));

	// Test enum rule, with only some supported values.
	public static final GameRule<Direction> CARDINAL_DIRECTION_ENUM = register("cardinal_direction", GameRuleFactory.createEnumRule(Direction.NORTH, CARDINAL_DIRECTIONS, createEnumCodec(Direction.class)));

	// Rules in custom categories
	public static final GameRule<Boolean> RED_BOOLEAN = register("red_boolean", RED_CATEGORY, GameRuleFactory.createBooleanRule(true));
	public static final GameRule<Boolean> GREEN_BOOLEAN = register("green_boolean", GREEN_CATEGORY, GameRuleFactory.createBooleanRule(false));

	// An enum rule with no "toString" logic
	public static final GameRule<TestEnum> RED_ENUM = register("red_enum", RED_CATEGORY, GameRuleFactory.createEnumRule(TestEnum.SCISSORS, createEnumCodec(TestEnum.class)));

	public static final AtomicBoolean FIRE_DAMAGE_CHANGED = new AtomicBoolean(false);

	private static <T> GameRule<T> register(String name, GameRule<T> rule) {
		return GameRuleRegistry.register(name, rule);
	}

	private static <T> GameRule<T> register(String name, CustomGameRuleCategory category, GameRule<T> rule) {
		return GameRuleRegistry.register(name, rule, category);
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Loading GameRules test mod.");

		// Test a vanilla rule
		if (!GameRuleRegistry.hasRegistration("keep_inventory")) {
			throw new AssertionError("Expected to find \"keep_inventory\" already registered, but it was not detected as registered");
		}

		// Test our own rule
		if (!GameRuleRegistry.hasRegistration("red_enum")) {
			throw new AssertionError("Expected to find \"red_enum\" already registered, but it was not detected as registered");
		}

		LOGGER.info("Loaded GameRules test mod.");

		// Validate the EnumRule has registered it's commands
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			RootCommandNode<ServerCommandSource> dispatcher = server.getCommandManager().getDispatcher().getRoot();
			// Find the GameRule node
			CommandNode<ServerCommandSource> gamerule = dispatcher.getChild("gamerule");

			if (gamerule == null) {
				throw new AssertionError("Failed to find GameRule command node on server's command dispatcher");
			}

			// Find the literal corresponding to our enum rule, using cardinal directions here.
			CommandNode<ServerCommandSource> cardinalDirection = gamerule.getChild("cardinal_direction");

			if (cardinalDirection == null) {
				throw new AssertionError("Failed to find \"cardinal_direction\" literal node corresponding a rule.");
			}

			// Verify we have a query command set.
			if (cardinalDirection.getCommand() == null) {
				throw new AssertionError("Expected to find a query command on \"cardinal_direction\" command node, but it was not present");
			}

			Collection<CommandNode<ServerCommandSource>> children = cardinalDirection.getChildren();

			// There should only be 4 child nodes.
			if (children.size() != 4) {
				throw new AssertionError(String.format("Expected only 4 child nodes on \"cardinal_direction\" command node, but %s were found", children.size()));
			}

			// All children should be literals
			children.stream().filter(node -> !(node instanceof LiteralCommandNode)).findAny().ifPresent(node -> {
				throw new AssertionError(String.format("Found non-literal child node on \"cardinal_direction\" command node %s", node));
			});

			// Verify we have all the correct nodes
			for (CommandNode<ServerCommandSource> child : children) {
				LiteralCommandNode<ServerCommandSource> node = (LiteralCommandNode<ServerCommandSource>) child;
				String name = node.getName();
				switch (name) {
				case "north":
				case "south":
				case "east":
				case "west":
					continue;
				default:
					throw new AssertionError(String.format("Found unexpected literal name. Found %s but only \"north, south, east, west\" are allowed", name));
				}
			}

			children.stream().filter(node -> node.getCommand() == null).findAny().ifPresent(node -> {
				throw new AssertionError(String.format("Found child node with no command literal name. %s", node));
			});

			LOGGER.info("GameRule command checks have passed. Try giving the enum rules a test.");
		});

		GameRuleEvents.CHANGED_CALLBACK.register((rule, value, server) -> {
			GameRuleRegistry.LOGGER.info("A rule was changed! Rule was {}", rule.getSimplifiedPath());
			if (rule.equals(GameRules.FIRE_DAMAGE)) {
				FIRE_DAMAGE_CHANGED.set(true);
			}
		});
	}

	public static <E extends Enum<E>> Codec<E> createEnumCodec(Class<E> clazz) {
		return Codec.STRING.comapFlatMap(string -> {
			try {
				return DataResult.success(Enum.valueOf(clazz, string));
			} catch (IllegalArgumentException exception) {
				return DataResult.error(() -> string + " is not a valid value for enum + " + clazz);
			}
		}, Enum::name);
	}
}
