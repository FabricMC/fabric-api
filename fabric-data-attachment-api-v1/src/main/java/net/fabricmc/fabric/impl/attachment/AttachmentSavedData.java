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

import com.mojang.serialization.Codec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;

/**
 * Backing storage for server-side global and level attachments.
 * Thanks to custom {@link #isDirty()} logic, the file is only written if something needs to be persisted.
 */
public class AttachmentSavedData extends SavedData {
	private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentSavedData.class);
	public static final Identifier ID = Identifier.fromNamespaceAndPath("fabric", "attachments");
	private final AttachmentTargetImpl target;
	private boolean wasSerialized;

	public AttachmentSavedData(AttachmentTarget target) {
		this.target = (AttachmentTargetImpl) target;
		this.wasSerialized = this.target.fabric_hasPersistentAttachments();
	}

	public static Codec<AttachmentSavedData> codec(MinecraftServer server) {
		return codec((AttachmentTargetImpl) server.globalAttachments(), () -> "AttachmentSavedData @ global server attachments");
	}

	public static Codec<AttachmentSavedData> codec(ServerLevel level) {
		return codec((AttachmentTargetImpl) level, () -> "AttachmentSavedData @ " + level.dimension().identifier());
	}

	private static Codec<AttachmentSavedData> codec(AttachmentTargetImpl target, ProblemReporter.PathElement reporterContext) {
		return CompoundTag.CODEC.xmap(tag -> {
			try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(reporterContext, LOGGER)) {
				ValueInput input = TagValueInput.create(reporter, target.fabric_getRegistryAccess(), tag);
				target.fabric_readAttachmentsFromNbt(input);
				return new AttachmentSavedData(target);
			}
		}, state -> {
			try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(reporterContext, LOGGER)) {
				TagValueOutput output = TagValueOutput.createWithContext(reporter, state.target.fabric_getRegistryAccess());
				state.target.fabric_writeAttachmentsToNbt(output);
				state.wasSerialized = true;
				return output.buildResult();
			}
		});
	}

	@Override
	public boolean isDirty() {
		// Only write data if there are attachments, or if we previously wrote data.
		return wasSerialized || target.fabric_hasPersistentAttachments();
	}
}
