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

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;

/**
 * Backing storage for server-side level attachments.
 * Thanks to custom {@link #isDirty()} logic, the file is only written if something needs to be persisted.
 */
public class AttachmentSavedData extends SavedData {
	private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentSavedData.class);
	public static final Identifier ID = Identifier.fromNamespaceAndPath("fabric", "attachments");
	private final AttachmentTargetImpl levelTarget;
	private final boolean wasSerialized;

	public AttachmentSavedData(ServerLevel level) {
		this.levelTarget = (AttachmentTargetImpl) level;
		this.wasSerialized = levelTarget.fabric_hasPersistentAttachments();
	}

	// TODO 1.21.5 look at making this more idiomatic
	public static Codec<AttachmentSavedData> codec(ServerLevel level) {
		final ProblemReporter.PathElement reporterContext = () -> "AttachmentSavedData @ " + level.dimension().identifier();

		return Codec.of(new Encoder<>() {
			@Override
			public <T> DataResult<T> encode(AttachmentSavedData input, DynamicOps<T> ops, T prefix) {
				try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(reporterContext, LOGGER)) {
					TagValueOutput output = TagValueOutput.createWithoutContext(reporter);
					((AttachmentTargetImpl) level).fabric_writeAttachmentsToNbt(output);
					return DataResult.success(NbtOps.INSTANCE.convertTo(ops, output.buildResult()));
				}
			}
		}, new Decoder<>() {
			@Override
			public <T> DataResult<Pair<AttachmentSavedData, T>> decode(DynamicOps<T> ops, T input) {
				try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(reporterContext, LOGGER)) {
					ValueInput valueInput = TagValueInput.create(reporter, level.registryAccess(), (CompoundTag) ops.convertTo(NbtOps.INSTANCE, input));
					((AttachmentTargetImpl) level).fabric_readAttachmentsFromNbt(valueInput);
					return DataResult.success(Pair.of(new AttachmentSavedData(level), ops.empty()));
				}
			}
		});
	}

	@Override
	public boolean isDirty() {
		// Only write data if there are attachments, or if we previously wrote data.
		return wasSerialized || levelTarget.fabric_hasPersistentAttachments();
	}
}
