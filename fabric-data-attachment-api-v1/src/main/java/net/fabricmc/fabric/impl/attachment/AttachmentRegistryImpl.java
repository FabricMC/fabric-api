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

package net.fabricmc.fabric.impl.attachment;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBufUtil;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentSync;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentTargetInfo;
import net.fabricmc.fabric.impl.attachment.sync.s2c.AttachmentSyncPayloadS2C;

public final class AttachmentRegistryImpl {
	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-data-attachment-api-v1");
	private static final Map<Identifier, AttachmentType<?>> attachmentRegistry = new HashMap<>();
	private static final Set<Identifier> syncableAttachments = new HashSet<>();
	private static final Set<Identifier> syncableView = Collections.unmodifiableSet(syncableAttachments);
	private static int currentMaxPayloadSize = AttachmentSync.INITIAL_MAX_ATTACHMENT_SYNC_PAYLOAD_SIZE;

	public static <A> void register(Identifier id, AttachmentType<A> attachmentType) {
		AttachmentType<?> existing = attachmentRegistry.put(id, attachmentType);

		if (existing != null) {
			LOGGER.warn("Encountered duplicate type registration for id {}", id);

			// Prevent duplicate registration from incorrectly overriding a synced type with a non-synced one or vice-versa
			if (existing.isSynced() && !attachmentType.isSynced()) {
				syncableAttachments.remove(id);
			} else if (!existing.isSynced() && attachmentType.isSynced()) {
				syncableAttachments.add(id);
			}
		} else if (attachmentType.isSynced()) {
			syncableAttachments.add(id);
		}
	}

	@Nullable
	public static AttachmentType<?> get(Identifier id) {
		return attachmentRegistry.get(id);
	}

	public static Set<Identifier> getSyncableAttachments() {
		return syncableView;
	}

	public static <A> AttachmentRegistry.Builder<A> builder() {
		return new BuilderImpl<>();
	}

	public static class BuilderImpl<A> implements AttachmentRegistry.Builder<A> {
		@Nullable
		private Supplier<A> defaultInitializer = null;
		@Nullable
		private Codec<A> persistenceCodec = null;
		@Nullable
		private StreamCodec<? super RegistryFriendlyByteBuf, A> packetCodec = null;
		@Nullable
		private AttachmentSyncPredicate syncPredicate = null;
		private boolean copyOnDeath = false;
		private int maxSyncBytes = -1;

		@Override
		public AttachmentRegistry.Builder<A> persistent(Codec<A> codec) {
			Objects.requireNonNull(codec, "codec cannot be null");

			this.persistenceCodec = codec;
			return this;
		}

		@Override
		public AttachmentRegistry.Builder<A> copyOnDeath() {
			this.copyOnDeath = true;
			return this;
		}

		@Override
		public AttachmentRegistry.Builder<A> initializer(Supplier<A> initializer) {
			Objects.requireNonNull(initializer, "initializer cannot be null");

			this.defaultInitializer = initializer;
			return this;
		}

		@Override
		public AttachmentRegistry.Builder<A> syncWith(StreamCodec<? super RegistryFriendlyByteBuf, A> packetCodec, AttachmentSyncPredicate syncPredicate) {
			Objects.requireNonNull(packetCodec, "packet codec cannot be null");
			Objects.requireNonNull(syncPredicate, "sync predicate cannot be null");

			this.packetCodec = packetCodec;
			this.syncPredicate = syncPredicate;
			return this;
		}

		@Override
		public AttachmentRegistry.Builder<A> syncWith(StreamCodec<? super RegistryFriendlyByteBuf, A> packetCodec, AttachmentSyncPredicate syncPredicate, int maxSyncBytes) {
			if (maxSyncBytes <= 0) {
				throw new IllegalArgumentException("max sync bytes must be positive");
			}

			syncWith(packetCodec, syncPredicate);

			this.maxSyncBytes = maxSyncBytes;
			return this;
		}

		@Override
		public AttachmentType<A> buildAndRegister(Identifier id) {
			Objects.requireNonNull(id, "identifier cannot be null");

			if (syncPredicate != null) {
				int identifierBytes = ByteBufUtil.utf8MaxBytes(id.toString());
				int maxPaddingBytes = AttachmentTargetInfo.MAX_SIZE_IN_BYTES + VarInt.getByteSize(identifierBytes) + identifierBytes + 5 * 2;

				if (maxSyncBytes == -1) { // If no custom limit set, then calculate default limit based on id size of the attachment
					maxSyncBytes = AttachmentSync.INITIAL_MAX_ATTACHMENT_SYNC_PAYLOAD_SIZE - maxPaddingBytes;
				}

				int maxPayloadBytes = maxSyncBytes + maxPaddingBytes;

				// Prevent overflow
				if (maxPayloadBytes < 0) {
					maxPayloadBytes = Integer.MAX_VALUE;
					maxSyncBytes = Integer.MAX_VALUE - maxPaddingBytes;
				}

				if (maxPayloadBytes > currentMaxPayloadSize) {
					currentMaxPayloadSize = maxPayloadBytes;
					PayloadTypeRegistry.playS2C().modifyLargePayloadMaxSize(AttachmentSyncPayloadS2C.ID, currentMaxPayloadSize);
				}
			}

			var attachment = new AttachmentTypeImpl<>(
					id,
					defaultInitializer,
					persistenceCodec,
					packetCodec,
					syncPredicate,
					maxSyncBytes,
					copyOnDeath
			);
			register(id, attachment);
			return attachment;
		}
	}
}
