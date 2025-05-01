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

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public class AttachmentSerializingImpl {
	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-data-attachment-api-v1");

	private static final Codec<AttachmentType<?>> TYPE_CODEC = Identifier.CODEC.comapFlatMap(id -> {
		AttachmentType<?> type = AttachmentRegistryImpl.get(id);
		return type == null ? DataResult.error(() -> "Found unknown attachment type " + id)
				: type.persistenceCodec() == null ? DataResult.error(() -> "Found non-permanent attachment type " + id)
				: DataResult.success(type);
	}, AttachmentType::identifier);
	private static final Codec<IdentityHashMap<AttachmentType<?>, Object>> CODEC = Codec.<AttachmentType<?>, Object>dispatchedMap(
			TYPE_CODEC,
			AttachmentType::persistenceCodec
	)
			.promotePartial(error -> LOGGER.warn("Skipping invalid attachments: {}", error))
			.xmap(
				IdentityHashMap::new,
				Function.identity()
	);

	public static void serializeAttachmentData(WriteView view, @Nullable IdentityHashMap<AttachmentType<?>, Object> attachments) {
		if (attachments == null || attachments.isEmpty()) {
			return;
		}

		IdentityHashMap<AttachmentType<?>, Object> attachmentsToSerialize = attachments.entrySet().stream()
				.filter(entry -> entry.getKey().persistenceCodec() != null)
				.collect(Collectors.toMap(
					Map.Entry::getKey,
					Map.Entry::getValue,
					(v1, v2) -> v1,
					IdentityHashMap::new
				));

		if (attachmentsToSerialize.isEmpty()) {
			return;
		}

		view.put(AttachmentTarget.NBT_ATTACHMENT_KEY, CODEC, attachmentsToSerialize);
	}

	@Nullable
	public static IdentityHashMap<AttachmentType<?>, Object> deserializeAttachmentData(@Nullable ReadView data) {
		return data == null ? null : data.read(AttachmentTarget.NBT_ATTACHMENT_KEY, CODEC).filter(m -> !m.isEmpty()).orElse(null);
	}

	public static boolean hasPersistentAttachments(@Nullable IdentityHashMap<AttachmentType<?>, ?> map) {
		if (map == null) {
			return false;
		}

		for (AttachmentType<?> type : map.keySet()) {
			if (type.isPersistent()) {
				return true;
			}
		}

		return false;
	}
}
