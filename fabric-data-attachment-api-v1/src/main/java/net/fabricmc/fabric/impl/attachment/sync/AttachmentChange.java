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

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;
import net.fabricmc.fabric.impl.attachment.AttachmentRegistryImpl;
import net.fabricmc.fabric.impl.attachment.AttachmentTypeImpl;

public record AttachmentChange(AttachmentTargetInfo<?> targetInfo, AttachmentType<?> type, @Nullable Object value) {
	public static final StreamCodec<RegistryFriendlyByteBuf, AttachmentChange> PACKET_CODEC = StreamCodec.ofMember(AttachmentChange::encodePacket, AttachmentChange::decodePacket);

	private static final boolean DISCONNECT_ON_UNKNOWN_TARGETS = System.getProperty("fabric.attachment.disconnect_on_unknown_targets") != null;
	private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentChange.class);

	private void encodePacket(RegistryFriendlyByteBuf buf) {
		AttachmentTypeImpl<?> type = (AttachmentTypeImpl<?>) this.type;

		AttachmentTargetInfo.PACKET_CODEC.encode(buf, this.targetInfo());
		Identifier.STREAM_CODEC.encode(buf, Objects.requireNonNull(this.type.identifier()));

		if (this.value == null) {
			// Todo: Legacy Format, writeVarInt should be removed for 26.2
			buf.writeVarInt(1);
			buf.writeBoolean(false);
			return;
		}

		//noinspection unchecked
		StreamCodec<? super RegistryFriendlyByteBuf, Object> codec = (StreamCodec<? super RegistryFriendlyByteBuf, Object>) type.streamCodec();
		Objects.requireNonNull(codec, "attachment stream codec cannot be null");

		// Todo: Legacy Format, buf2 should be removed and replaced with buf for 26.2
		// Todo: int currentLength = buf.readableBytes();
		RegistryFriendlyByteBuf buf2 = new RegistryFriendlyByteBuf(FriendlyByteBufs.create(), buf.registryAccess());

		buf2.writeBoolean(true);
		codec.encode(buf2, value);

		// Todo: Legacy Format, `buf2.readableBytes()` should be replaced with `buf.readableBytes() - currentLength` for 26.2
		validateMaxPayloadSize(type, buf2.readableBytes());

		// Todo: Legacy Format, remove buf2 for 26.2
		buf.writeVarInt(buf2.readableBytes());
		buf.writeBytes(buf2);
	}

	private static AttachmentChange decodePacket(RegistryFriendlyByteBuf buf) {
		AttachmentTargetInfo<?> target = AttachmentTargetInfo.PACKET_CODEC.decode(buf);
		AttachmentTypeImpl<?> type = (AttachmentTypeImpl<?>) AttachmentRegistryImpl.get(Identifier.STREAM_CODEC.decode(buf));
		Objects.requireNonNull(type, "attachment type cannot be null");

		// Todo: Legacy Format, readVarInt should be replaced with buf.readableBytes() for 26.2
		int size = buf.readVarInt();
		validateMaxPayloadSize(type, size);

		if (!buf.readBoolean()) {
			return new AttachmentChange(target, type, null);
		}

		//noinspection unchecked
		StreamCodec<? super RegistryFriendlyByteBuf, Object> codec = (StreamCodec<? super RegistryFriendlyByteBuf, Object>) type.streamCodec();
		Objects.requireNonNull(codec, "attachment stream codec cannot be null");

		return new AttachmentChange(target, type, codec.decode(buf));
	}

	private static void validateMaxPayloadSize(AttachmentTypeImpl<?> type, int length) {
		int maxDataSize = type.maxSyncSize();

		if (length > maxDataSize) {
			throw new IllegalArgumentException("Data for attachment '%s' was too big (%d bytes, over maximum %d)".formatted(
					type.identifier(),
					length,
					maxDataSize
			));
		}
	}

	public void tryApply(Level level) throws AttachmentSyncException {
		AttachmentTarget target = targetInfo.getTarget(level);

		if (target == null) {
			final MutableComponent errorMessageComponent = Component.empty();
			errorMessageComponent
					.append(Component.translatable("fabric-data-attachment-api-v1.unknown-target.title").withStyle(ChatFormatting.RED))
					.append(CommonComponents.NEW_LINE);
			errorMessageComponent.append(CommonComponents.NEW_LINE);

			errorMessageComponent
					.append(Component.translatable(
							"fabric-data-attachment-api-v1.unknown-target.attachment-identifier",
							Component.literal(String.valueOf(type.identifier())).withStyle(ChatFormatting.YELLOW))
					)
					.append(CommonComponents.NEW_LINE);
			errorMessageComponent
					.append(Component.translatable(
							"fabric-data-attachment-api-v1.unknown-target.level",
							Component.literal(String.valueOf(level.dimension().identifier())).withStyle(ChatFormatting.YELLOW)
					))
					.append(CommonComponents.NEW_LINE);
			targetInfo.appendDebugInformation(errorMessageComponent);

			if (DISCONNECT_ON_UNKNOWN_TARGETS) {
				throw new AttachmentSyncException(errorMessageComponent);
			}

			LOGGER.warn(errorMessageComponent.getString().trim());
			return;
		}

		target.setAttached((AttachmentType<Object>) type, value);
	}

	public AttachmentChange withNewTarget(AttachmentTargetInfo<?> newTargetInfo) {
		return new AttachmentChange(newTargetInfo, this.type, this.value);
	}
}
