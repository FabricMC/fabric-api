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

package net.fabricmc.fabric.test.attachment;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.io.File;
import java.io.IOException;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.ColumnPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class AttachmentTestMod implements ModInitializer {
	public static final String MOD_ID = "fabric-data-attachment-api-v1-testmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final AttachmentType<String> PERSISTENT = AttachmentRegistry.createPersistent(
			Identifier.of(MOD_ID, "persistent"),
			Codec.STRING
	);
	public static final AttachmentType<String> FEATURE_ATTACHMENT = AttachmentRegistry.create(
			Identifier.of(MOD_ID, "feature")
	);
	public static final AttachmentType<Boolean> SYNCED_WITH_ALL = AttachmentRegistry.create(
			Identifier.of(MOD_ID, "synced_all"),
			builder -> builder
					.initializer(() -> false)
					.persistent(Codec.BOOL)
					.syncWith(PacketCodecs.BOOL, AttachmentSyncPredicate.all())
	);
	public static final AttachmentType<Boolean> SYNCED_WITH_TARGET = AttachmentRegistry.create(
			Identifier.of(MOD_ID, "synced_target"),
			builder -> builder
					.initializer(() -> false)
					.persistent(Codec.BOOL)
					.syncWith(PacketCodecs.BOOL, AttachmentSyncPredicate.targetOnly())
	);
	public static final AttachmentType<Boolean> SYNCED_EXCEPT_TARGET = AttachmentRegistry.create(
			Identifier.of(MOD_ID, "synced_except_target"),
			builder -> builder
					.initializer(() -> false)
					.persistent(Codec.BOOL)
					.syncWith(PacketCodecs.BOOL, AttachmentSyncPredicate.allButTarget())
	);
	public static final AttachmentType<Boolean> SYNCED_CREATIVE_ONLY = AttachmentRegistry.create(
			Identifier.of(MOD_ID, "synced_custom"),
			builder -> builder
					.initializer(() -> false)
					.persistent(Codec.BOOL)
					.syncWith(PacketCodecs.BOOL, (target, player) -> player.isCreative())
	);
	public static final AttachmentType<ItemStack> SYNCED_ITEM = AttachmentRegistry.create(
			Identifier.of(MOD_ID, "synced_item"),
			builder -> builder
					.initializer(() -> ItemStack.EMPTY)
					.persistent(ItemStack.CODEC)
					.syncWith(ItemStack.OPTIONAL_PACKET_CODEC, AttachmentSyncPredicate.all())
	);
	public static final SimpleCommandExceptionType TARGET_NOT_FOUND = new SimpleCommandExceptionType(Text.literal("Target not found"));

	public static final ChunkPos FAR_CHUNK_POS = new ChunkPos(300, 0);

	private boolean serverStarted = false;
	public static boolean featurePlaced = false;

	@Override
	public void onInitialize() {
		Registry.register(Registries.FEATURE, Identifier.of(MOD_ID, "set_attachment"), new SetAttachmentFeature(DefaultFeatureConfig.CODEC));

		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.VEGETAL_DECORATION,
				RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(MOD_ID, "set_attachment"))
		);

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			File saveRoot = server.getSavePath(WorldSavePath.ROOT).toFile();
			File markerFile = new File(saveRoot, MOD_ID + "_MARKER");
			boolean firstLaunch;

			try {
				firstLaunch = markerFile.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			ServerWorld overworld = server.getOverworld();
			WorldChunk chunk = overworld.getChunk(0, 0);

			if (firstLaunch) {
				LOGGER.info("First launch, testing attachment by feature");

				if (featurePlaced) {
					if (!"feature".equals(chunk.getAttached(FEATURE_ATTACHMENT))) {
						throw new AssertionError("Feature did not write attachment to ProtoChunk");
					}
				} else {
					LOGGER.warn("Feature not placed, could not test writing during worldgen");
				}

				LOGGER.info("setting up persistent attachments");

				overworld.setAttached(PERSISTENT, "world_data");

				chunk.setAttached(PERSISTENT, "chunk_data");
				chunk.setAttached(SYNCED_WITH_ALL, true);

				ProtoChunk protoChunk = (ProtoChunk) overworld.getChunkManager().getChunk(FAR_CHUNK_POS.x, FAR_CHUNK_POS.z, ChunkStatus.STRUCTURE_STARTS, true);
				protoChunk.setAttached(PERSISTENT, "protochunk_data");
			} else {
				LOGGER.info("Second launch, testing persistent attachments");

				if (!"world_data".equals(overworld.getAttached(PERSISTENT))) throw new AssertionError("World attachment did not persist");
				if (!"chunk_data".equals(chunk.getAttached(PERSISTENT))) throw new AssertionError("WorldChunk attachment did not persist");

				WrapperProtoChunk wrapperProtoChunk = (WrapperProtoChunk) overworld.getChunkManager().getChunk(0, 0, ChunkStatus.EMPTY, true);
				if (!"chunk_data".equals(wrapperProtoChunk.getAttached(PERSISTENT))) throw new AssertionError("Attachment is not accessible through WrapperProtoChunk");

				Chunk farChunk = overworld.getChunkManager().getChunk(FAR_CHUNK_POS.x, FAR_CHUNK_POS.z, ChunkStatus.EMPTY, true);

				if (farChunk instanceof WrapperProtoChunk) {
					LOGGER.warn("Far chunk already generated, can't test persistence in ProtoChunk.");
				} else {
					if (!"protochunk_data".equals(farChunk.getAttached(PERSISTENT))) throw new AssertionError("ProtoChunk attachment did not persist");
				}
			}

			serverStarted = true;
		});

		// Testing hint: load far chunk by running /tp @s 4800 ~ 0
		ServerChunkEvents.CHUNK_LOAD.register(((world, chunk) -> {
			if (!chunk.getPos().equals(FAR_CHUNK_POS)) return;

			if (!serverStarted) {
				LOGGER.warn("Chunk {} loaded before server started, can't test transfer of attachments to WorldChunk", FAR_CHUNK_POS);
				return;
			}

			LOGGER.info("Loaded chunk {}, testing transfer of attachments to WorldChunk", FAR_CHUNK_POS);

			if (!"protochunk_data".equals(chunk.getAttached(PERSISTENT))) throw new AssertionError("ProtoChunk attachment was not transfered to WorldChunk");
		}));

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
				literal("attachment")
						.then(buildCommandForKind("all", "all", SYNCED_WITH_ALL))
						.then(buildCommandForKind("self_only", "only self", SYNCED_WITH_TARGET))
						.then(buildCommandForKind("others_only", "all but self", SYNCED_EXCEPT_TARGET))
						.then(buildCommandForKind("creative_only", "creative players only", SYNCED_CREATIVE_ONLY))
		));

		ServerEntityEvents.EQUIPMENT_CHANGE.register((livingEntity, equipmentSlot, previousStack, currentStack) -> {
			if (equipmentSlot == EquipmentSlot.HEAD && livingEntity instanceof ServerPlayerEntity player) {
				player.setAttached(SYNCED_ITEM, currentStack);
			}
		});
	}

	private static LiteralArgumentBuilder<ServerCommandSource> buildCommandForKind(String id, String syncedWith, AttachmentType<Boolean> type) {
		return literal(id).executes(context -> updateAttachmentFor(
				context.getSource().getPlayerOrThrow(),
				type,
				context,
				"Set self flag (synced with %s) to %%s".formatted(syncedWith)
		)).then(
				argument("target", EntityArgumentType.entity()).executes(context -> updateAttachmentFor(
						EntityArgumentType.getEntity(context, "target"),
						type,
						context,
						"Set entity flag (synced with %s) to %%s".formatted(syncedWith)
				))
		).then(argument("pos", BlockPosArgumentType.blockPos()).executes(context -> {
			BlockEntity be = context.getSource().getWorld().getBlockEntity(BlockPosArgumentType.getBlockPos(context, "pos"));

			if (be == null) {
				throw TARGET_NOT_FOUND.create();
			}

			return updateAttachmentFor(
					be,
					type,
					context,
					"Set block entity flag (synced with %s) to %%s".formatted(syncedWith)
			);
		})).then(argument("chunkPos", ColumnPosArgumentType.columnPos()).executes(context -> {
			ColumnPos pos = ColumnPosArgumentType.getColumnPos(context, "chunkpos");
			return updateAttachmentFor(
					context.getSource().getWorld().getChunk(pos.x(), pos.z(), ChunkStatus.STRUCTURE_STARTS, true),
					type,
					context,
					"Set chunk flag (synced with %s) to %%s".formatted(syncedWith)
			);
		})).then(literal("world").executes(context -> updateAttachmentFor(
				context.getSource().getWorld(),
				type,
				context,
				"Set world flag (synced with %s) to %%s".formatted(syncedWith)
		)));
	}

	private static int updateAttachmentFor(AttachmentTarget target, AttachmentType<Boolean> attachment, CommandContext<ServerCommandSource> context, String messageFormat) throws CommandSyntaxException {
		boolean current = target.getAttachedOrElse(attachment, false);
		target.setAttached(attachment, !current);
		context.getSource().sendFeedback(() -> Text.literal(messageFormat.formatted(!current)), false);
		return 1;
	}
}
