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

package net.fabricmc.fabric.test.attachment.client;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import java.util.UUID;
import java.util.function.Function;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.test.attachment.AttachmentTestMod;
import net.fabricmc.fabric.test.attachment.client.mixin.ClientWorldAccessor;
import net.fabricmc.fabric.test.attachment.client.mixin.DefaultPosArgumentAccessor;

public class AttachmentTestModClient implements ClientModInitializer {
	private static AbstractClientPlayerEntity parseClientPlayer(FabricClientCommandSource source, String name) throws CommandSyntaxException {
		for (AbstractClientPlayerEntity player : source.getWorld().getPlayers()) {
			if (name.equals(player.getName().getLiteralString())) {
				return player;
			}
		}

		throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
	}

	private static BlockPos getBlockPos(CommandContext<FabricClientCommandSource> context, String argName) {
		DefaultPosArgumentAccessor posArg = (DefaultPosArgumentAccessor) context.getArgument(argName, DefaultPosArgument.class);
		Vec3d pos = context.getSource().getPosition();
		return BlockPos.ofFloored(new Vec3d(
				posArg.getX().toAbsoluteCoordinate(pos.x),
				posArg.getY().toAbsoluteCoordinate(pos.y),
				posArg.getZ().toAbsoluteCoordinate(pos.z)
		));
	}

	private static <T extends AttachmentTarget> void displayClientAttachmentInfo(
			CommandContext<FabricClientCommandSource> context,
			T target,
			Function<T, String> nameGetter
	) {
		context.getSource().sendFeedback(
				Text.literal("Attachments for target %s:".formatted(nameGetter.apply(target)))
		);
		boolean attAll = target.getAttachedOrCreate(AttachmentTestMod.SYNCED_WITH_ALL);
		context.getSource().sendFeedback(
				Text.literal("Synced-with-all attachment: %s".formatted(attAll)).withColor(attAll ? Colors.GREEN : Colors.WHITE)
		);
		boolean attTarget = target.getAttachedOrCreate(AttachmentTestMod.SYNCED_WITH_TARGET);
		context.getSource().sendFeedback(
				Text.literal("Synced-with-target attachment: %s".formatted(attTarget))
						.withColor(attTarget ? target == MinecraftClient.getInstance().player ? Colors.GREEN : Colors.RED : Colors.WHITE)
		);
		boolean attOther = target.getAttachedOrCreate(AttachmentTestMod.SYNCED_EXCEPT_TARGET);
		context.getSource().sendFeedback(
				Text.literal("Synced-with-non-targets attachment: %s".formatted(attOther))
						.withColor(attOther ? target != MinecraftClient.getInstance().player ? Colors.GREEN : Colors.RED : Colors.WHITE)
		);
		boolean attCustom = target.getAttachedOrCreate(AttachmentTestMod.SYNCED_CREATIVE_ONLY);
		context.getSource().sendFeedback(
				Text.literal("Synced-with-creative attachment: %s".formatted(attCustom))
						.withColor(attCustom ? target instanceof PlayerEntity p && p.isCreative() ? Colors.GREEN : Colors.RED : Colors.WHITE)
		);
	}

	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess) -> dispatcher.register(
						literal("attachment_test")
								.executes(context -> {
									displayClientAttachmentInfo(
											context,
											context.getSource().getPlayer(),
											PlayerEntity::getNameForScoreboard
									);
									return 1;
								})
								.then(chain(
										context -> {
											displayClientAttachmentInfo(
													context,
													parseClientPlayer(context.getSource(), StringArgumentType.getString(context, "target")),
													PlayerEntity::getNameForScoreboard
											);
											return 1;
										},
										literal("player"),
										argument("target", StringArgumentType.word())
								))
								.then(chain(
									context -> {
										UUID uuid = context.getArgument("uuid", UUID.class);
										Entity entity = ((ClientWorldAccessor) context.getSource().getWorld())
												.invokeGetEntityLookup()
												.get(uuid);

										if (entity == null) {
											throw AttachmentTestMod.TARGET_NOT_FOUND.create();
										}

										displayClientAttachmentInfo(context, entity, e -> uuid.toString());
										return 1;
									},
									literal("entity"),
									argument("uuid", UuidArgumentType.uuid())
								))
								.then(chain(
									context -> {
										BlockPos pos = getBlockPos(context, "pos");
										BlockEntity be = context.getSource()
												.getWorld()
												.getBlockEntity(pos);

										if (be == null) {
											throw AttachmentTestMod.TARGET_NOT_FOUND.create();
										}

										displayClientAttachmentInfo(
												context,
												be,
												b -> pos.toShortString()
										);
										return 1;
									},
									literal("blockentity"),
									argument("pos", BlockPosArgumentType.blockPos())
								))
								.then(chain(
									context -> {
										BlockPos pos = getBlockPos(context, "pos");
										Chunk chunk = context.getSource().getWorld().getChunk(pos);
										displayClientAttachmentInfo(
												context,
												chunk,
												c -> c.getPos().toString()
										);
										return 1;
									},
									literal("chunk"),
									argument("pos", BlockPosArgumentType.blockPos())
								))
								.then(literal("world").executes(
										context -> {
											displayClientAttachmentInfo(
													context,
													context.getSource().getWorld(),
													w -> "world"
											);
											return 1;
										}
								))
				)
		);
	}

	@SafeVarargs
	private static ArgumentBuilder<FabricClientCommandSource, ?> chain(
			Command<FabricClientCommandSource> command,
			ArgumentBuilder<FabricClientCommandSource, ?>... nodes
	) {
		ArgumentBuilder<FabricClientCommandSource, ?> result = nodes[nodes.length - 1].executes(command);

		for (int i = nodes.length - 2; i >= 0; i--) {
			result = nodes[i].then(result);
		}

		return result;
	}
}
