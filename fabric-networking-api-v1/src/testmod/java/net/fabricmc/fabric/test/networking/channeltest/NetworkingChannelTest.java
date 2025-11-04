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

package net.fabricmc.fabric.test.networking.channeltest;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.EntityArgument.getPlayer;
import static net.minecraft.commands.arguments.EntityArgument.player;
import static net.minecraft.commands.arguments.IdentifierArgument.getId;
import static net.minecraft.commands.arguments.IdentifierArgument.id;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;

public final class NetworkingChannelTest implements ModInitializer {
	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			final LiteralCommandNode<CommandSourceStack> channelTestCommand = literal("network_channel_test").build();

			// Info
			{
				final LiteralCommandNode<CommandSourceStack> info = literal("info")
						.executes(context -> infoCommand(context, context.getSource().getPlayer()))
						.build();

				final ArgumentCommandNode<CommandSourceStack, EntitySelector> player = argument("player", player())
						.executes(context -> infoCommand(context, getPlayer(context, "player")))
						.build();

				info.addChild(player);
				channelTestCommand.addChild(info);
			}

			// Register
			{
				final LiteralCommandNode<CommandSourceStack> register = literal("register")
						.then(argument("channel", id())
								.executes(context -> registerChannel(context, context.getSource().getPlayer())))
						.build();

				channelTestCommand.addChild(register);
			}

			// Unregister
			{
				final LiteralCommandNode<CommandSourceStack> unregister = literal("unregister")
						.then(argument("channel", id()).suggests(NetworkingChannelTest::suggestReceivableChannels)
								.executes(context -> unregisterChannel(context, context.getSource().getPlayer())))
						.build();

				channelTestCommand.addChild(unregister);
			}

			dispatcher.getRoot().addChild(channelTestCommand);
		});
	}

	private static CompletableFuture<Suggestions> suggestReceivableChannels(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
		final ServerPlayer player = context.getSource().getPlayer();

		return SharedSuggestionProvider.suggestResource(ServerPlayNetworking.getReceived(player), builder);
	}

	private static int registerChannel(CommandContext<CommandSourceStack> context, ServerPlayer executor) throws CommandSyntaxException {
		final Identifier channel = getId(context, "channel");

		if (ServerPlayNetworking.getReceived(executor).contains(channel)) {
			throw new SimpleCommandExceptionType(Component.literal(String.format("Cannot register channel %s twice for server player", channel))).create();
		}

		CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, ? extends CustomPacketPayload> payloadType = PayloadTypeRegistryImpl.PLAY_C2S.get(channel);

		if (payloadType != null) {
			ServerPlayNetworking.registerReceiver(executor.connection, payloadType.type(), (payload, ctx) -> {
				System.out.printf("Received packet on channel %s%n", payloadType.type().id());
			});
			context.getSource().sendSuccess(() -> Component.literal(String.format("Registered channel %s for %s", channel, executor.getDisplayName())), false);
			return 1;
		} else {
			throw new SimpleCommandExceptionType(Component.literal("Unknown channel id")).create();
		}
	}

	private static int unregisterChannel(CommandContext<CommandSourceStack> context, ServerPlayer player) throws CommandSyntaxException {
		final Identifier channel = getId(context, "channel");

		if (!ServerPlayNetworking.getReceived(player).contains(channel)) {
			throw new SimpleCommandExceptionType(Component.literal("Cannot unregister channel the server player entity cannot receive packets on")).create();
		}

		ServerPlayNetworking.unregisterReceiver(player.connection, channel);
		context.getSource().sendSuccess(() -> Component.literal(String.format("Unregistered channel %s for %s", getId(context, "channel"), player.getDisplayName())), false);

		return 1;
	}

	private static int infoCommand(CommandContext<CommandSourceStack> context, ServerPlayer player) {
		// TODO:

		return 1;
	}
}
