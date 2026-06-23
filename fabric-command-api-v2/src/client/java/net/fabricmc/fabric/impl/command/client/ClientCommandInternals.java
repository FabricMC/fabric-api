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

package net.fabricmc.fabric.impl.command.client;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.AmbiguityConsumer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.util.profiling.Profiler;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.mixin.command.HelpCommandAccessor;
import net.fabricmc.fabric.mixin.command.client.ClientPacketListenerAccessor;

public final class ClientCommandInternals {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientCommandInternals.class);
	private static final String API_COMMAND_NAME = "fabric-command-api-v2:client";
	private static final String SHORT_API_COMMAND_NAME = "fcc";
	private static @Nullable CommandDispatcher<FabricClientCommandSource> activeDispatcher;

	public static void setActiveDispatcher(@Nullable CommandDispatcher<FabricClientCommandSource> dispatcher) {
		ClientCommandInternals.activeDispatcher = dispatcher;
	}

	public static @Nullable CommandDispatcher<FabricClientCommandSource> getActiveDispatcher() {
		return activeDispatcher;
	}

	/**
	 * Executes a client-sided command. Callers should ensure that this is only called
	 * on slash-prefixed messages and the slash needs to be removed before calling.
	 *
	 * <p>If the command is ran "unattended" (e.g. from a text component click event) and the command requires the attended permissions, a confirmation screen will be displayed before executing the command.
	 *
	 * @param command the command with slash removed
	 * @param source the command source to execute the command with
	 * @param restrictedSource a command source with more restricted permissions than {@code source} to check if the command requires confirmation. Null when command is ran directly with user input.
	 * @return true if the command should not be sent to the server, false otherwise
	 */
	public static boolean executeCommand(String command, FabricClientCommandSource source, @Nullable FabricClientCommandSource restrictedSource) {
		Profiler.get().push(command);

		try {
			if (restrictedSource != null) {
				if (requiresConfirmation(command, source, restrictedSource)) {
					openSendConfirmationWindow(command, "multiplayer.confirm_command.permissions_required", () -> {
						executeCommand(command, source, null);
					});

					return true;
				}
			}

			// TODO: Check for server commands before executing.
			//   This requires parsing the command, checking if they match a server command
			//   and then executing the command with the parse results.
			activeDispatcher.execute(command, source);
			return true;
		} catch (CommandSyntaxException e) {
			boolean ignored = isIgnoredException(e.getType());

			if (ignored) {
				LOGGER.debug("Syntax exception for client-sided command '{}'", command, e);
				return false;
			}

			LOGGER.warn("Syntax exception for client-sided command '{}'", command, e);
			source.sendError(getErrorMessage(e));
			return true;
		} catch (Exception e) {
			LOGGER.warn("Error while executing client-sided command '{}'", command, e);
			source.sendError(Component.nullToEmpty(e.getMessage()));
			return true;
		} finally {
			Profiler.get().pop();
		}
	}

	private static boolean requiresConfirmation(String command, FabricClientCommandSource source, FabricClientCommandSource restrictedSource) {
		ParseResults<FabricClientCommandSource> parseResults = activeDispatcher.parse(command, source);

		if (!ClientPacketListenerAccessor.invokeIsValidCommand(parseResults)) {
			// not a valid command, no need to confirm
			return false;
		}

		parseResults = activeDispatcher.parse(command, restrictedSource);

		if (!ClientPacketListenerAccessor.invokeIsValidCommand(parseResults)) {
			// We failed to parse the command with the restricted permissions, thus it means that the command requires user confirmation before being executed.
			return true;
		}

		return false;
	}

	private static void openSendConfirmationWindow(final String command, final String messageKey, final Runnable onAccept) {
		Minecraft minecraft = Minecraft.getInstance();
		Screen currentScreen = minecraft.gui.screen();
		var confirmScreen = new ConfirmScreen(
				result -> {
					if (result) {
						onAccept.run();
					}

					minecraft.gui.setScreen(currentScreen);
				},
				Component.translatable("multiplayer.confirm_command.title"),
				Component.translatable("multiplayer.confirm_command.permissions_required", Component.literal(command).withStyle(ChatFormatting.YELLOW)),
				Component.translatable("multiplayer.confirm_command.run_command"),
				currentScreen != null ? CommonComponents.GUI_BACK : CommonComponents.GUI_CANCEL
		);
		minecraft.gui.setScreen(confirmScreen);
	}

	/**
	 * Tests whether a command syntax exception with the type
	 * should be ignored and the command sent to the server.
	 *
	 * @param type the exception type
	 * @return true if ignored, false otherwise
	 */
	private static boolean isIgnoredException(CommandExceptionType type) {
		BuiltInExceptionProvider builtins = CommandSyntaxException.BUILT_IN_EXCEPTIONS;

		// Only ignore unknown commands and node parse exceptions.
		// The argument-related dispatcher exceptions are not ignored because
		// they will only happen if the user enters a correct command.
		return type == builtins.dispatcherUnknownCommand() || type == builtins.dispatcherParseException();
	}

	// See CommandSuggestions.getExceptionMessage. That cannot be used directly as it returns an FormattedCharSequence instead of a Component.
	private static Component getErrorMessage(CommandSyntaxException e) {
		Component message = ComponentUtils.fromMessage(e.getRawMessage());
		String context = e.getContext();

		return context != null ? Component.translatable("command.context.parse_error", message, e.getCursor(), context) : message;
	}

	/**
	 * Runs final initialization tasks such as {@link CommandDispatcher#findAmbiguities(AmbiguityConsumer)}
	 * on the command dispatcher. Also registers a {@code /fcc help} command if there are other commands present.
	 */
	public static void finalizeInit() {
		if (!activeDispatcher.getRoot().getChildren().isEmpty()) {
			// Register an API command if there are other commands;
			// these helpers are not needed if there are no client commands
			LiteralArgumentBuilder<FabricClientCommandSource> help = literal("help");
			help.executes(ClientCommandInternals::executeRootHelp);
			help.then(argument("command", StringArgumentType.greedyString()).executes(ClientCommandInternals::executeArgumentHelp));

			CommandNode<FabricClientCommandSource> mainNode = activeDispatcher.register(literal(API_COMMAND_NAME).then(help));
			activeDispatcher.register(literal(SHORT_API_COMMAND_NAME).redirect(mainNode));
		}

		// noinspection CodeBlock2Expr
		activeDispatcher.findAmbiguities((parent, child, sibling, inputs) -> {
			LOGGER.warn("Ambiguity between arguments {} and {} with inputs: {}", activeDispatcher.getPath(child), activeDispatcher.getPath(sibling), inputs);
		});
	}

	private static int executeRootHelp(CommandContext<FabricClientCommandSource> context) {
		return executeHelp(activeDispatcher.getRoot(), context);
	}

	private static int executeArgumentHelp(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ParseResults<FabricClientCommandSource> parseResults = activeDispatcher.parse(StringArgumentType.getString(context, "command"), context.getSource());
		List<ParsedCommandNode<FabricClientCommandSource>> nodes = parseResults.getContext().getNodes();

		if (nodes.isEmpty()) {
			throw HelpCommandAccessor.getFailedException().create();
		}

		return executeHelp(Iterables.getLast(nodes).getNode(), context);
	}

	private static int executeHelp(CommandNode<FabricClientCommandSource> startNode, CommandContext<FabricClientCommandSource> context) {
		Map<CommandNode<FabricClientCommandSource>, String> commands = activeDispatcher.getSmartUsage(startNode, context.getSource());

		for (String command : commands.values()) {
			context.getSource().sendFeedback(Component.literal("/" + command));
		}

		return commands.size();
	}

	public static void addCommands(CommandDispatcher<FabricClientCommandSource> target, FabricClientCommandSource source) {
		Map<CommandNode<FabricClientCommandSource>, CommandNode<FabricClientCommandSource>> nodes = new HashMap<>();
		nodes.put(activeDispatcher.getRoot(), target.getRoot());
		copyChildren(activeDispatcher.getRoot(), target.getRoot(), source, nodes);
	}

	/**
	 * Copies the child commands from root to newRoot, filtered by {@code child.canUse(source)}.
	 * Mimics vanilla's Commands.fillUsableCommands.
	 *
	 * @param root           the root command node
	 * @param newRoot        the new root command node
	 * @param source         the command source
	 * @param nodes          a mutable map from original command nodes to their copies, used for redirects;
	 *                       should contain a mapping from root to newRoot
	 */
	private static void copyChildren(
			CommandNode<FabricClientCommandSource> root,
			CommandNode<FabricClientCommandSource> newRoot,
			FabricClientCommandSource source,
			Map<CommandNode<FabricClientCommandSource>, CommandNode<FabricClientCommandSource>> nodes
	) {
		for (CommandNode<FabricClientCommandSource> child : root.getChildren()) {
			if (!child.canUse(source)) continue;

			ArgumentBuilder<FabricClientCommandSource, ?> builder = child.createBuilder();

			// Reset the unnecessary non-completion stuff from the builder
			builder.requires(s -> true); // This is checked with the if check above.

			if (builder.getCommand() != null) {
				builder.executes(context -> 0);
			}

			// Set up redirects
			if (builder.getRedirect() != null) {
				builder.redirect(nodes.get(builder.getRedirect()));
			}

			CommandNode<FabricClientCommandSource> result = builder.build();
			nodes.put(child, result);
			newRoot.addChild(result);

			if (!child.getChildren().isEmpty()) {
				copyChildren(child, result, source, nodes);
			}
		}
	}

	public interface LastReceivedCommandsPacketAccessor {
		@Nullable ClientboundCommandsPacket fabric_api$getLastReceivedCommandsPacket();
	}
}
