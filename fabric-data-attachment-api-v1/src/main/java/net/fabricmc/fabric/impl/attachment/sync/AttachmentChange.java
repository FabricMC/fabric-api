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

import java.util.Objects;

import io.netty.buffer.Unpooled;
import org.jspecify.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.impl.attachment.AttachmentRegistryImpl;
import net.fabricmc.fabric.impl.attachment.AttachmentTypeImpl;

public record AttachmentChange(AttachmentTargetInfo<?> targetInfo, AttachmentType<?> type, byte[] data) {
	public static final StreamCodec<FriendlyByteBuf, AttachmentChange> PACKET_CODEC = StreamCodec.composite(
			AttachmentTargetInfo.PACKET_CODEC, AttachmentChange::targetInfo,
			Identifier.STREAM_CODEC.map(
					id -> Objects.requireNonNull(AttachmentRegistryImpl.get(id)),
					AttachmentType::identifier
			), AttachmentChange::type,
			ByteBufCodecs.BYTE_ARRAY, AttachmentChange::data,
			AttachmentChange::new
	);

	@SuppressWarnings("unchecked")
	public static AttachmentChange create(AttachmentTargetInfo<?> targetInfo, AttachmentType<?> type, @Nullable Object value, RegistryAccess dynamicRegistryManager) {
		StreamCodec<? super RegistryFriendlyByteBuf, Object> codec = (StreamCodec<? super RegistryFriendlyByteBuf, Object>) ((AttachmentTypeImpl<?>) type).packetCodec();
		Objects.requireNonNull(codec, "attachment packet codec cannot be null");
		Objects.requireNonNull(dynamicRegistryManager, "dynamic registry manager cannot be null");

		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(PacketByteBufs.create(), dynamicRegistryManager);

		if (value != null) {
			buf.writeBoolean(true);
			codec.encode(buf, value);
		} else {
			buf.writeBoolean(false);
		}

		byte[] encoded = new byte[buf.readableBytes()]; // buf.array() will return the backing array directly, which may contain unused space
		buf.readBytes(encoded);
		int maxSyncBytes = ((AttachmentTypeImpl<?>) type).maxSyncBytes();

		if (encoded.length > maxSyncBytes) {
			throw new IllegalArgumentException("Data for attachment '%s' was too big (%d bytes, over maximum %d). This limit can be configured during attachment registration.".formatted(
					type.identifier(),
					encoded.length,
					maxSyncBytes
			));
		}

		return new AttachmentChange(targetInfo, type, encoded);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public Object decodeValue(RegistryAccess dynamicRegistryManager) {
		StreamCodec<? super RegistryFriendlyByteBuf, Object> codec = (StreamCodec<? super RegistryFriendlyByteBuf, Object>) ((AttachmentTypeImpl<?>) type).packetCodec();
		Objects.requireNonNull(codec, "codec was null");
		Objects.requireNonNull(dynamicRegistryManager, "dynamic registry manager cannot be null");

		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.copiedBuffer(data), dynamicRegistryManager);

		if (!buf.readBoolean()) {
			return null;
		}

		return codec.decode(buf);
	}

	public void tryApply(Level world) throws AttachmentSyncException {
		AttachmentTarget target = targetInfo.getTarget(world);
		Object value = decodeValue(world.registryAccess());

		if (target == null) {
			final MutableComponent errorMessageText = Component.empty();
			errorMessageText
					.append(Component.translatable("fabric-data-attachment-api-v1.unknown-target.title").withStyle(ChatFormatting.RED))
					.append(CommonComponents.NEW_LINE);
			errorMessageText.append(CommonComponents.NEW_LINE);

			errorMessageText
					.append(Component.translatable(
							"fabric-data-attachment-api-v1.unknown-target.attachment-identifier",
							Component.literal(String.valueOf(type.identifier())).withStyle(ChatFormatting.YELLOW))
					)
					.append(CommonComponents.NEW_LINE);
			errorMessageText
					.append(Component.translatable(
							"fabric-data-attachment-api-v1.unknown-target.world",
							Component.literal(String.valueOf(world.dimension().identifier())).withStyle(ChatFormatting.YELLOW)
					))
					.append(CommonComponents.NEW_LINE);
			targetInfo.appendDebugInformation(errorMessageText);

			throw new AttachmentSyncException(errorMessageText);
		}

		target.setAttached((AttachmentType<Object>) type, value);
	}
}
