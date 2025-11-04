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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleEvents;

public class GameRulesTestMod implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameRulesTestMod.class);
	public static final Direction[] CARDINAL_DIRECTIONS = Arrays.stream(Direction.values()).filter(direction -> direction != Direction.UP && direction != Direction.DOWN).toArray(Direction[]::new);
	public static final CustomGameRuleCategory GREEN_CATEGORY = new CustomGameRuleCategory(Identifier.fromNamespaceAndPath("fabric", "green"), Component.literal("This One is Green").withStyle(style -> style.withBold(true).withColor(ChatFormatting.DARK_GREEN)));
	public static final CustomGameRuleCategory RED_CATEGORY = new CustomGameRuleCategory(Identifier.fromNamespaceAndPath("fabric", "red"), Component.literal("This One is Red").withStyle(style -> style.withBold(true).withColor(ChatFormatting.DARK_RED)));

	// Bounded, Integer, Double and Float rules
	public static final GameRule<Integer> POSITIVE_ONLY_TEST_INT = GameRuleBuilder.forInteger(2)
			.minValue(0)
			.buildAndRegister(id("positive_only_test_integer"));
	public static final GameRule<Double> ONE_TO_TEN_DOUBLE = GameRuleBuilder.forDouble(1.0D)
			.range(1.0D, 10.0D)
			.buildAndRegister(id("one_to_ten_double"));

	// Test enum rule, with only some supported values.
	public static final GameRule<Direction> CARDINAL_DIRECTION_ENUM_RULE = GameRuleBuilder.forEnum(Direction.NORTH)
			.supportedValues(CARDINAL_DIRECTIONS)
			.buildAndRegister(id("cardinal_direction"));

	// Rules in custom categories
	public static final GameRule<Boolean> RED_BOOLEAN = GameRuleBuilder.forBoolean(true)
			.category(RED_CATEGORY)
			.buildAndRegister(Identifier.fromNamespaceAndPath("fabric", "red_boolean"));
	public static final GameRule<Boolean> GREEN_BOOLEAN = GameRuleBuilder.forBoolean(false)
			.category(GREEN_CATEGORY)
			.buildAndRegister(id("green_boolean"));

	// An enum rule with no "toString" logic
	public static final GameRule<TestEnum> RED_ENUM = GameRuleBuilder.forEnum(TestEnum.SCISSORS)
			.category(RED_CATEGORY)
			.buildAndRegister(id("red_enum"));

	public static final AtomicBoolean FIRE_DAMAGE_CHANGED = new AtomicBoolean(false);

	private static Identifier id(String name) {
		return Identifier.withDefaultNamespace(name); // TODO replace once MC-303846 is fixed
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Loaded GameRules test mod.");

		// Validate the EnumRule has registered its commands
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			RootCommandNode<CommandSourceStack> dispatcher = server.getCommands().getDispatcher().getRoot();
			// Find the GameRule node
			CommandNode<CommandSourceStack> gamerule = dispatcher.getChild("gamerule");

			if (gamerule == null) {
				throw new AssertionError("Failed to find GameRule command node on server's command dispatcher");
			}

			// Find the literal corresponding to our enum rule, using cardinal directions here.
			CommandNode<CommandSourceStack> cardinalDirection = gamerule.getChild("cardinal_direction");

			if (cardinalDirection == null) {
				throw new AssertionError("Failed to find \"cardinal_direction\" literal node corresponding a rule.");
			}

			// Verify we have a query command set.
			if (cardinalDirection.getCommand() == null) {
				throw new AssertionError("Expected to find a query command on \"cardinal_direction\" command node, but it was not present");
			}

			Collection<CommandNode<CommandSourceStack>> children = cardinalDirection.getChildren();

			// There should only be 4 child nodes.
			if (children.size() != 4) {
				throw new AssertionError(String.format("Expected only 4 child nodes on \"cardinal_direction\" command node, but %s were found", children.size()));
			}

			// All children should be literals
			children.stream().filter(node -> !(node instanceof LiteralCommandNode)).findAny().ifPresent(node -> {
				throw new AssertionError(String.format("Found non-literal child node on \"cardinal_direction\" command node %s", node));
			});

			// Verify we have all the correct nodes
			for (CommandNode<CommandSourceStack> child : children) {
				LiteralCommandNode<CommandSourceStack> node = (LiteralCommandNode<CommandSourceStack>) child;
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

		GameRuleEvents.changeCallback(GameRules.FIRE_DAMAGE).register(
				(value, server) -> FIRE_DAMAGE_CHANGED.set(true)
		);
	}
}
