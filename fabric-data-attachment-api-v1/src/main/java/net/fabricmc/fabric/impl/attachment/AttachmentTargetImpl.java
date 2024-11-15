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

import java.util.Map;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentChange;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentTargetInfo;
import net.fabricmc.fabric.impl.attachment.sync.s2c.AttachmentSyncPayloadS2C;

public interface AttachmentTargetImpl extends AttachmentTarget {
	/**
	 * Copies attachments from the original to the target. This is used when a ProtoChunk is converted to a
	 * WorldChunk, and when an entity is respawned and a new instance is created. For entity respawns, it is
	 * triggered on player respawn, entity conversion, return from the End, or cross-world entity teleportation.
	 * In the first two cases, only the attachments with {@link AttachmentType#copyOnDeath()} will be transferred.
	 */
	@SuppressWarnings("unchecked")
	static void transfer(AttachmentTarget original, AttachmentTarget target, boolean isDeath) {
		Map<AttachmentType<?>, ?> attachments = ((AttachmentTargetImpl) original).fabric_getAttachments();

		if (attachments == null) {
			return;
		}

		for (Map.Entry<AttachmentType<?>, ?> entry : attachments.entrySet()) {
			AttachmentType<Object> type = (AttachmentType<Object>) entry.getKey();

			if (!isDeath || type.copyOnDeath()) {
				target.setAttached(type, entry.getValue());
			}
		}
	}

	@Nullable
	default Map<AttachmentType<?>, ?> fabric_getAttachments() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	default void fabric_writeAttachmentsToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	default void fabric_readAttachmentsFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	default boolean fabric_hasPersistentAttachments() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	default AttachmentTargetInfo<?> fabric_getSyncTargetInfo() {
		// this only makes sense for server objects
		throw new UnsupportedOperationException("Sync target info was not retrieved on server!");
	}

	/*
	 * Computes changes that should be communicated to newcomers (i.e. clients that start tracking this target)
	 */
	default void fabric_computeInitialSyncChanges(ServerPlayerEntity player, Consumer<AttachmentChange> changeOutput) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	default void fabric_syncChange(AttachmentType<?> type, AttachmentSyncPayloadS2C payload) {
	}

	default void fabric_markChanged(AttachmentType<?> type) {
	}

	default boolean fabric_shouldTryToSync() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}
}
