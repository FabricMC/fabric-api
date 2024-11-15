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

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;

public sealed interface AttachmentTargetInfo<T> {
	int MAX_SIZE_IN_BYTES = Byte.BYTES + Long.BYTES;
	PacketCodec<ByteBuf, AttachmentTargetInfo<?>> PACKET_CODEC = PacketCodecs.BYTE.dispatch(
			AttachmentTargetInfo::getId, Type::packetCodecFromId
	);

	Type<T> getType();

	default byte getId() {
		return getType().id;
	}

	AttachmentTarget getTarget(World world);

	record Type<T>(byte id, PacketCodec<ByteBuf, ? extends AttachmentTargetInfo<T>> packetCodec) {
		static Byte2ObjectMap<Type<?>> TYPES = new Byte2ObjectArrayMap<>();
		static Type<BlockEntity> BLOCK_ENTITY = new Type<>((byte) 0, BlockEntityTarget.PACKET_CODEC);
		static Type<Entity> ENTITY = new Type<>((byte) 1, EntityTarget.PACKET_CODEC);
		static Type<Chunk> CHUNK = new Type<>((byte) 2, ChunkTarget.PACKET_CODEC);
		static Type<World> WORLD = new Type<>((byte) 3, WorldTarget.PACKET_CODEC);

		public Type {
			TYPES.put(id, this);
		}

		static PacketCodec<ByteBuf, ? extends AttachmentTargetInfo<?>> packetCodecFromId(byte id) {
			return TYPES.get(id).packetCodec;
		}
	}

	record BlockEntityTarget(BlockPos pos) implements AttachmentTargetInfo<BlockEntity> {
		static final PacketCodec<ByteBuf, BlockEntityTarget> PACKET_CODEC = PacketCodec.tuple(
				BlockPos.PACKET_CODEC, BlockEntityTarget::pos,
				BlockEntityTarget::new
		);

		@Override
		public Type<BlockEntity> getType() {
			return Type.BLOCK_ENTITY;
		}

		@Override
		public AttachmentTarget getTarget(World world) {
			return world.getBlockEntity(pos);
		}
	}

	record EntityTarget(int networkId) implements AttachmentTargetInfo<Entity> {
		static final PacketCodec<ByteBuf, EntityTarget> PACKET_CODEC = PacketCodec.tuple(
				PacketCodecs.VAR_INT, EntityTarget::networkId,
				EntityTarget::new
		);

		@Override
		public Type<Entity> getType() {
			return Type.ENTITY;
		}

		@Override
		public AttachmentTarget getTarget(World world) {
			return world.getEntityById(networkId);
		}
	}

	record ChunkTarget(ChunkPos pos) implements AttachmentTargetInfo<Chunk> {
		static final PacketCodec<ByteBuf, ChunkTarget> PACKET_CODEC = PacketCodecs.VAR_LONG
				.xmap(ChunkPos::new, ChunkPos::toLong)
				.xmap(ChunkTarget::new, ChunkTarget::pos);

		@Override
		public Type<Chunk> getType() {
			return Type.CHUNK;
		}

		@Override
		public AttachmentTarget getTarget(World world) {
			return world.getChunk(pos.x, pos.z);
		}
	}

	final class WorldTarget implements AttachmentTargetInfo<World> {
		public static final WorldTarget INSTANCE = new WorldTarget();
		static final PacketCodec<ByteBuf, WorldTarget> PACKET_CODEC = PacketCodec.unit(INSTANCE);

		private WorldTarget() {
		}

		@Override
		public Type<World> getType() {
			return Type.WORLD;
		}

		@Override
		public AttachmentTarget getTarget(World world) {
			return world;
		}
	}
}
