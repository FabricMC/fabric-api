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

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.world.PersistentState;

/**
 * Backing storage for server-side world attachments.
 * Thanks to custom {@link #isDirty()} logic, the file is only written if something needs to be persisted.
 */
public class AttachmentPersistentState extends PersistentState {
	private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentPersistentState.class);
	public static final String ID = "fabric_attachments";
	private final AttachmentTargetImpl worldTarget;
	private final boolean wasSerialized;

	public AttachmentPersistentState(ServerWorld world) {
		this.worldTarget = (AttachmentTargetImpl) world;
		this.wasSerialized = worldTarget.fabric_hasPersistentAttachments();
	}

	// TODO 1.21.5 look at making this more idiomatic
	public static Codec<AttachmentPersistentState> codec(ServerWorld world) {
		final ErrorReporter.Context reporterContext = () -> "AttachmentPersistentState @ " + world.getRegistryKey().getValue();

		return Codec.of(new Encoder<>() {
			@Override
			public <T> DataResult<T> encode(AttachmentPersistentState input, DynamicOps<T> ops, T prefix) {
				try (ErrorReporter.Logging reporter = new ErrorReporter.Logging(reporterContext, LOGGER)) {
					NbtWriteView writeView = NbtWriteView.create(reporter);
					((AttachmentTargetImpl) world).fabric_writeAttachmentsToNbt(writeView);
					return DataResult.success(NbtOps.INSTANCE.convertTo(ops, writeView.getNbt()));
				}
			}
		}, new Decoder<>() {
			@Override
			public <T> DataResult<Pair<AttachmentPersistentState, T>> decode(DynamicOps<T> ops, T input) {
				try (ErrorReporter.Logging reporter = new ErrorReporter.Logging(reporterContext, LOGGER)) {
					ReadView readView = NbtReadView.get(reporter, world.getRegistryManager(), (NbtCompound) ops.convertTo(NbtOps.INSTANCE, input));
					((AttachmentTargetImpl) world).fabric_readAttachmentsFromNbt(readView);
					return DataResult.success(Pair.of(new AttachmentPersistentState(world), ops.empty()));
				}
			}
		});
	}

	@Override
	public boolean isDirty() {
		// Only write data if there are attachments, or if we previously wrote data.
		return wasSerialized || worldTarget.fabric_hasPersistentAttachments();
	}
}
