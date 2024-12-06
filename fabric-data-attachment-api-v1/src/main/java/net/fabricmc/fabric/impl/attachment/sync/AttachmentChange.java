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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.attachment.AttachmentRegistryImpl;
import net.fabricmc.fabric.impl.attachment.AttachmentTypeImpl;
import net.fabricmc.fabric.impl.attachment.sync.s2c.AttachmentSyncPayloadS2C;
import net.fabricmc.fabric.mixin.attachment.CustomPayloadS2CPacketAccessor;
import net.fabricmc.fabric.mixin.attachment.VarIntsAccessor;
import net.fabricmc.fabric.mixin.networking.accessor.ServerCommonNetworkHandlerAccessor;

public record AttachmentChange(AttachmentTargetInfo<?> targetInfo, AttachmentType<?> type, byte[] data) {
	public static final PacketCodec<PacketByteBuf, AttachmentChange> PACKET_CODEC = PacketCodec.tuple(
			AttachmentTargetInfo.PACKET_CODEC, AttachmentChange::targetInfo,
			Identifier.PACKET_CODEC.xmap(
					id -> Objects.requireNonNull(AttachmentRegistryImpl.get(id)),
					AttachmentType::identifier
			), AttachmentChange::type,
			PacketCodecs.BYTE_ARRAY, AttachmentChange::data,
			AttachmentChange::new
	);
	private static final int MAX_PADDING_SIZE_IN_BYTES = AttachmentTargetInfo.MAX_SIZE_IN_BYTES + AttachmentSync.MAX_IDENTIFIER_SIZE;
	private static final int MAX_DATA_SIZE_IN_BYTES = CustomPayloadS2CPacketAccessor.getMaxPayloadSize() - MAX_PADDING_SIZE_IN_BYTES;

	@SuppressWarnings("unchecked")
	public static AttachmentChange create(AttachmentTargetInfo<?> targetInfo, AttachmentType<?> type, @Nullable Object value, DynamicRegistryManager dynamicRegistryManager) {
		PacketCodec<? super RegistryByteBuf, Object> codec = (PacketCodec<? super RegistryByteBuf, Object>) ((AttachmentTypeImpl<?>) type).packetCodec();
		Objects.requireNonNull(codec, "attachment packet codec cannot be null");
		Objects.requireNonNull(dynamicRegistryManager, "dynamic registry manager cannot be null");

		RegistryByteBuf buf = new RegistryByteBuf(PacketByteBufs.create(), dynamicRegistryManager);

		if (value != null) {
			buf.writeBoolean(true);
			codec.encode(buf, value);
		} else {
			buf.writeBoolean(false);
		}

		byte[] encoded = buf.array();

		if (encoded.length > MAX_DATA_SIZE_IN_BYTES) {
			throw new IllegalArgumentException("Data for attachment '%s' was too big (%d bytes, over maximum %d)".formatted(
					type.identifier(),
					encoded.length,
					MAX_DATA_SIZE_IN_BYTES
			));
		}

		return new AttachmentChange(targetInfo, type, encoded);
	}

	public static void partitionAndSendPackets(List<AttachmentChange> changes, ServerPlayerEntity player) {
		Set<Identifier> supported = ((SupportedAttachmentsClientConnection) ((ServerCommonNetworkHandlerAccessor) player.networkHandler).getConnection())
				.fabric_getSupportedAttachments();
		// sort by size to better partition packets
		changes.sort(Comparator.comparingInt(c -> c.data().length));
		List<AttachmentChange> packetChanges = new ArrayList<>();
		int maxVarIntSize = VarIntsAccessor.getMaxByteSize();
		int byteSize = maxVarIntSize;

		for (AttachmentChange change : changes) {
			if (!supported.contains(change.type.identifier())) {
				continue;
			}

			int size = MAX_PADDING_SIZE_IN_BYTES + change.data.length;

			if (byteSize + size > MAX_DATA_SIZE_IN_BYTES) {
				ServerPlayNetworking.send(player, new AttachmentSyncPayloadS2C(packetChanges));
				packetChanges.clear();
				byteSize = maxVarIntSize;
			}

			packetChanges.add(change);
			byteSize += size;
		}

		if (!packetChanges.isEmpty()) {
			ServerPlayNetworking.send(player, new AttachmentSyncPayloadS2C(packetChanges));
		}
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public Object decodeValue(DynamicRegistryManager dynamicRegistryManager) {
		PacketCodec<? super RegistryByteBuf, Object> codec = (PacketCodec<? super RegistryByteBuf, Object>) ((AttachmentTypeImpl<?>) type).packetCodec();
		Objects.requireNonNull(codec, "codec was null");
		Objects.requireNonNull(dynamicRegistryManager, "dynamic registry manager cannot be null");

		RegistryByteBuf buf = new RegistryByteBuf(Unpooled.copiedBuffer(data), dynamicRegistryManager);

		if (!buf.readBoolean()) {
			return null;
		}

		return codec.decode(buf);
	}

	public void apply(World world) {
		targetInfo.getTarget(world).setAttached((AttachmentType<Object>) type, decodeValue(world.getRegistryManager()));
	}
}
