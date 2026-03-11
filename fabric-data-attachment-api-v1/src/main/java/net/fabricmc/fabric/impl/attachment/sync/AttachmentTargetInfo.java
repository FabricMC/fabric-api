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

package net.fabricmc.fabric.impl.attachment.sync;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import org.jspecify.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.GlobalAttachments;

public sealed interface AttachmentTargetInfo<T> {
	int MAX_SIZE_IN_BYTES = Byte.BYTES + Long.BYTES;
	StreamCodec<ByteBuf, AttachmentTargetInfo<?>> PACKET_CODEC = ByteBufCodecs.BYTE.dispatch(
			AttachmentTargetInfo::getId, Type::streamCodecFromId
	);

	Type<T> getType();

	default byte getId() {
		return getType().id;
	}

	@Nullable
	AttachmentTarget getTarget(Level level);

	void appendDebugInformation(MutableComponent component);

	record Type<T>(byte id, StreamCodec<ByteBuf, ? extends AttachmentTargetInfo<T>> streamCodec) {
		static Byte2ObjectMap<Type<?>> TYPES = new Byte2ObjectArrayMap<>();
		static Type<BlockEntity> BLOCK_ENTITY = new Type<>((byte) 0, BlockEntityTarget.PACKET_CODEC);
		static Type<Entity> ENTITY = new Type<>((byte) 1, EntityTarget.PACKET_CODEC);
		static Type<ChunkAccess> CHUNK = new Type<>((byte) 2, ChunkTarget.PACKET_CODEC);
		static Type<Level> WORLD = new Type<>((byte) 3, LevelTarget.PACKET_CODEC);
		static Type<GlobalAttachments> GLOBAL = new Type<>((byte) 4, GlobalTarget.PACKET_CODEC);

		public Type {
			TYPES.put(id, this);
		}

		static StreamCodec<ByteBuf, ? extends AttachmentTargetInfo<?>> streamCodecFromId(byte id) {
			return TYPES.get(id).streamCodec;
		}
	}

	record BlockEntityTarget(BlockPos pos) implements AttachmentTargetInfo<BlockEntity> {
		static final StreamCodec<ByteBuf, BlockEntityTarget> PACKET_CODEC = StreamCodec.composite(
				BlockPos.STREAM_CODEC, BlockEntityTarget::pos,
				BlockEntityTarget::new
		);

		@Override
		public Type<BlockEntity> getType() {
			return Type.BLOCK_ENTITY;
		}

		@Override
		public AttachmentTarget getTarget(Level level) {
			return level.getBlockEntity(pos);
		}

		@Override
		public void appendDebugInformation(MutableComponent component) {
			component
					.append(Component.translatable(
							"fabric-data-attachment-api-v1.unknown-target.target-type",
							Component.translatable("fabric-data-attachment-api-v1.unknown-target.target-type.block-entity").withStyle(ChatFormatting.YELLOW)
					))
					.append(CommonComponents.NEW_LINE);
			component
					.append(Component.translatable(
							"fabric-data-attachment-api-v1.unknown-target.block-entity-position",
							Component.literal(pos.toShortString()).withStyle(ChatFormatting.YELLOW)
					))
					.append(CommonComponents.NEW_LINE);
		}
	}

	record EntityTarget(int networkId) implements AttachmentTargetInfo<Entity> {
		static final StreamCodec<ByteBuf, EntityTarget> PACKET_CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT, EntityTarget::networkId,
				EntityTarget::new
		);

		@Override
		public Type<Entity> getType() {
			return Type.ENTITY;
		}

		@Override
		public AttachmentTarget getTarget(Level level) {
			return level.getEntity(networkId);
		}

		@Override
		public void appendDebugInformation(MutableComponent component) {
			component
					.append(Component.translatable(
							"fabric-data-attachment-api-v1.unknown-target.target-type",
							Component.translatable("fabric-data-attachment-api-v1.unknown-target.target-type.entity").withStyle(ChatFormatting.YELLOW)
					))
					.append(CommonComponents.NEW_LINE);
			component
					.append(Component.translatable(
							"fabric-data-attachment-api-v1.unknown-target.entity-network-id",
							Component.literal(String.valueOf(networkId)).withStyle(ChatFormatting.YELLOW)
					))
					.append(CommonComponents.NEW_LINE);
		}
	}

	record ChunkTarget(ChunkPos pos) implements AttachmentTargetInfo<ChunkAccess> {
		static final StreamCodec<ByteBuf, ChunkTarget> PACKET_CODEC = ByteBufCodecs.VAR_LONG
				.map(ChunkPos::unpack, ChunkPos::pack)
				.map(ChunkTarget::new, ChunkTarget::pos);

		@Override
		public Type<ChunkAccess> getType() {
			return Type.CHUNK;
		}

		@Override
		public AttachmentTarget getTarget(Level level) {
			return level.getChunk(pos.x(), pos.z(), ChunkStatus.FULL, false);
		}

		@Override
		public void appendDebugInformation(MutableComponent component) {
			component
					.append(Component.translatable(
							"fabric-data-attachment-api-v1.unknown-target.target-type",
							Component.translatable("fabric-data-attachment-api-v1.unknown-target.target-type.chunk").withStyle(ChatFormatting.YELLOW)
					))
					.append(CommonComponents.NEW_LINE);
			component
					.append(Component.translatable(
							"fabric-data-attachment-api-v1.unknown-target.chunk-position",
							Component.literal(pos.x() + ", " + pos.z()).withStyle(ChatFormatting.YELLOW)
					))
					.append(CommonComponents.NEW_LINE);
		}
	}

	final class LevelTarget implements AttachmentTargetInfo<Level> {
		public static final LevelTarget INSTANCE = new LevelTarget();
		static final StreamCodec<ByteBuf, LevelTarget> PACKET_CODEC = StreamCodec.unit(INSTANCE);

		private LevelTarget() {
		}

		@Override
		public Type<Level> getType() {
			return Type.WORLD;
		}

		@Override
		public AttachmentTarget getTarget(Level level) {
			return level;
		}

		@Override
		public void appendDebugInformation(MutableComponent component) {
			component
					.append(Component.translatable(
							"fabric-data-attachment-api-v1.unknown-target.target-type",
							Component.translatable("fabric-data-attachment-api-v1.unknown-target.target-type.level").withStyle(ChatFormatting.YELLOW)
					))
					.append(CommonComponents.NEW_LINE);
		}
	}

	final class GlobalTarget implements AttachmentTargetInfo<GlobalAttachments> {
		public static final GlobalTarget INSTANCE = new GlobalTarget();
		static final StreamCodec<ByteBuf, GlobalTarget> PACKET_CODEC = StreamCodec.unit(INSTANCE);

		private GlobalTarget() {
		}

		@Override
		public Type<GlobalAttachments> getType() {
			return Type.GLOBAL;
		}

		@Override
		public AttachmentTarget getTarget(Level level) {
			return level.globalAttachments();
		}

		@Override
		public void appendDebugInformation(MutableComponent component) {
			component
					.append(Component.translatable(
							"fabric-data-attachment-api-v1.unknown-target.target-type",
							Component.translatable("fabric-data-attachment-api-v1.unknown-target.target-type.global").withStyle(ChatFormatting.YELLOW)
					))
					.append(CommonComponents.NEW_LINE);
		}
	}
}
