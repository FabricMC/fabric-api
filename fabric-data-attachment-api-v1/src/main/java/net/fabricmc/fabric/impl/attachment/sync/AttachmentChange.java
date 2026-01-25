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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;
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
	private static final boolean DISCONNECT_ON_UNKNOWN_TARGETS = System.getProperty("fabric.attachment.disconnect_on_unknown_targets") != null;
	private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentChange.class);

	@SuppressWarnings("unchecked")
	public static AttachmentChange create(AttachmentTargetInfo<?> targetInfo, AttachmentType<?> type, @Nullable Object value, RegistryAccess registryAccess) {
		StreamCodec<? super RegistryFriendlyByteBuf, Object> codec = (StreamCodec<? super RegistryFriendlyByteBuf, Object>) ((AttachmentTypeImpl<?>) type).streamCodec();
		Objects.requireNonNull(codec, "attachment stream codec cannot be null");
		Objects.requireNonNull(registryAccess, "registry access cannot be null");

		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(FriendlyByteBufs.create(), registryAccess);

		if (value != null) {
			buf.writeBoolean(true);
			codec.encode(buf, value);
		} else {
			buf.writeBoolean(false);
		}

		// buf.array() returns the backing array directly, which often contains unused space
		byte[] encoded = new byte[buf.readableBytes()];
		buf.readBytes(encoded);
		int maxDataSize = ((AttachmentTypeImpl<?>) type).maxSyncSize();

		if (encoded.length > maxDataSize) {
			throw new IllegalArgumentException("Data for attachment '%s' was too big (%d bytes, over maximum %d)".formatted(
					type.identifier(),
					encoded.length,
					maxDataSize
			));
		}

		return new AttachmentChange(targetInfo, type, encoded);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public Object decodeValue(RegistryAccess registryAccess) {
		StreamCodec<? super RegistryFriendlyByteBuf, Object> codec = (StreamCodec<? super RegistryFriendlyByteBuf, Object>) ((AttachmentTypeImpl<?>) type).streamCodec();
		Objects.requireNonNull(codec, "codec was null");
		Objects.requireNonNull(registryAccess, "registry access cannot be null");

		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.copiedBuffer(data), registryAccess);

		if (!buf.readBoolean()) {
			return null;
		}

		return codec.decode(buf);
	}

	public void tryApply(Level level) throws AttachmentSyncException {
		AttachmentTarget target = targetInfo.getTarget(level);
		Object value = decodeValue(level.registryAccess());

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
		return new AttachmentChange(newTargetInfo, this.type, this.data);
	}
}
