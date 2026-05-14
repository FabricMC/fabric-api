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

package net.fabricmc.fabric.test.permission;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TextComponentTagVisitor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.permission.v1.MutablePermissionContext;
import net.fabricmc.fabric.api.permission.v1.PermissionContext;
import net.fabricmc.fabric.api.permission.v1.PermissionEvents;
import net.fabricmc.fabric.api.permission.v1.PermissionNode;
import net.fabricmc.fabric.api.permission.v1.PermissionPredicates;
import net.fabricmc.fabric.test.permission.example.PermissionMap;

public class PermissionTestMod implements ModInitializer, PermissionEvents.OnRequest, PermissionEvents.PrepareOfflinePlayer {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final PermissionContext.Key<Object> OBJECT_KEY = PermissionContext.key(Identifier.fromNamespaceAndPath("test", "object_key"));

	private static final PermissionNode<Boolean> ON_STONE = PermissionNode.of(Identifier.fromNamespaceAndPath("test", "on_stone"));
	private static final PermissionNode<Boolean> IS_ENTITY = PermissionNode.of(Identifier.fromNamespaceAndPath("test", "is_entity"));
	private static final PermissionNode<Boolean> ABOVE_SEA = PermissionNode.of(Identifier.fromNamespaceAndPath("test", "above_sea"));
	private static final PermissionNode<Integer> MAGIC = PermissionNode.ofInteger(Identifier.fromNamespaceAndPath("test", "magic"));

	private final PermissionMap globalPermissionMap = new PermissionMap();

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register(this::registerCommands);
		PermissionEvents.ON_REQUEST.register(this);
		PermissionEvents.PREPARE_OFFLINE_PLAYER.register(this);

		this.runBasicTest();
		ServerLifecycleEvents.SERVER_STARTED.register(this::runServerTest);
	}

	private void runBasicTest() {
		int value = RandomSource.createThreadLocalInstance().nextInt();

		this.globalPermissionMap.set(MAGIC.key(), value);

		PermissionContext context = PermissionContext.create(UUID.randomUUID(), PermissionContext.Type.OTHER, PermissionLevel.ADMINS);

		int valueMainCheck = context.checkPermission(MAGIC, value + 1);

		if (valueMainCheck != value) {
			throw new IllegalStateException("Permission check failed! valueMainCheck != value, d=" + (valueMainCheck - value));
		}
	}

	private void runServerTest(MinecraftServer server) {
		PermissionContext.offlinePlayer(NameAndId.createOffline("TinyPotato"), server).thenAcceptAsync(context -> {
			if (context.get(OBJECT_KEY) == null) {
				throw new IllegalStateException("Context wasn't modified correctly!");
			}

			int value = RandomSource.createThreadLocalInstance().nextInt();
			this.globalPermissionMap.set(MAGIC.key(), value);

			int valueMainCheck = context.checkPermission(MAGIC, value + 1);

			if (valueMainCheck != value) {
				throw new IllegalStateException("Permission check failed! valueMainCheck != value, d=" + (valueMainCheck - value));
			}
		}, server);
	}

	private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {
		dispatcher.register(literal("permissions")
				.then(
						literal("set").then(argument("permission", IdentifierArgument.id()).then(argument("value", NbtTagArgument.nbtTag()).executes(this::setPermissionValue)))
				)
				.then(
						literal("get").then(argument("permission", IdentifierArgument.id()).executes(this::getPermissionValue))
				)
				.then(
						literal("check_bool").then(argument("permission", IdentifierArgument.id()).executes(this::checkPermissionValue))
				)
				.then(
						literal("on_stone_command").requires(PermissionPredicates.require(ON_STONE, false)).executes(this::onStoneCommand)
				)
		);
	}

	private int setPermissionValue(CommandContext<CommandSourceStack> context) {
		Identifier id = IdentifierArgument.getId(context, "permission");
		Tag value = NbtTagArgument.getNbtTag(context, "value");

		if (context.getSource().getPlayer() instanceof ServerPlayer player) {
			context.getSource().getServer().getPlayerList().sendPlayerPermissionLevel(player);
		}

		this.globalPermissionMap.set(id, value);
		return 1;
	}

	private int getPermissionValue(CommandContext<CommandSourceStack> context) {
		Identifier id = IdentifierArgument.getId(context, "permission");
		Tag value = this.globalPermissionMap.getRaw(id);

		context.getSource().sendSystemMessage(value != null ? new TextComponentTagVisitor("", TextComponentTagVisitor.RichStyling.INSTANCE).visit(value) : Component.literal("<null>"));
		return 1;
	}

	private int checkPermissionValue(CommandContext<CommandSourceStack> context) {
		Identifier id = IdentifierArgument.getId(context, "permission");

		context.getSource().sendSystemMessage(Component.literal(context.getSource().checkPermission(id).getSerializedName()));
		return 1;
	}

	private int onStoneCommand(CommandContext<CommandSourceStack> context) {
		context.getSource().sendSystemMessage(Component.literal("You got the stone permission"));
		return 1;
	}

	@SuppressWarnings("unchecked")
	@Override
	public @Nullable <T> T handlePermissionRequest(PermissionContext context, PermissionNode<T> permission) {
		Level level = context.get(PermissionContext.LEVEL);
		BlockPos blockPos = context.get(PermissionContext.BLOCK_POSITION);
		Entity entity = context.get(PermissionContext.ENTITY);

		if (permission.codec() == Codec.BOOL) {
			if (permission.equals(ON_STONE) && level != null && blockPos != null) {
				return permission.cast(level.getBlockState(blockPos.below()).is(Blocks.STONE));
			}

			if (permission.equals(IS_ENTITY)) {
				return permission.cast(entity != null);
			}

			if (permission.equals(ABOVE_SEA) && blockPos != null && level != null) {
				return permission.cast(level.getSeaLevel() < blockPos.getY());
			}
		}

		// This isn't needed since PermissionMap uses codec, but it's done to make sure it works™
		return permission.cast(this.globalPermissionMap.get(permission.key(), permission.codec()));
	}

	@Override
	public @NonNull CompletableFuture<@Nullable Consumer<MutablePermissionContext>> prepareOfflinePlayer(PermissionContext context, MinecraftServer server) {
		LOGGER.info("Preparing for offline player check for {} (also known as {})!", context.uuid(), context.get(PermissionContext.NAME));
		return CompletableFuture.completedFuture(mutCtx -> {
			mutCtx.set(OBJECT_KEY, new Object());
			LOGGER.info("Modified context for {}!", mutCtx.uuid());
		});
	}
}
