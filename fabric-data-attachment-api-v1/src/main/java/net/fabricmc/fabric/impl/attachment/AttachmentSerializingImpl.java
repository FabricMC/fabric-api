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

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public class AttachmentSerializingImpl {
	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-data-attachment-api-v1");

	@SuppressWarnings("unchecked")
	public static void serializeAttachmentData(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup, @Nullable IdentityHashMap<AttachmentType<?>, ?> attachments) {
		if (attachments == null || attachments.isEmpty()) {
			return;
		}

		var compound = new NbtCompound();

		for (Map.Entry<AttachmentType<?>, ?> entry : attachments.entrySet()) {
			AttachmentType<?> type = entry.getKey();
			Codec<Object> codec = (Codec<Object>) type.persistenceCodec();

			if (codec != null) {
				RegistryOps<NbtElement> registryOps = wrapperLookup.getOps(NbtOps.INSTANCE);
				codec.encodeStart(registryOps, entry.getValue())
						.ifError(partial -> {
							LOGGER.warn("Couldn't serialize attachment {}, skipping. Error:", type.identifier());
							LOGGER.warn(partial.message());
						})
						.ifSuccess(serialized -> compound.put(type.identifier().toString(), serialized));
			}
		}

		nbt.put(AttachmentTarget.NBT_ATTACHMENT_KEY, compound);
	}

	@Nullable
	public static IdentityHashMap<AttachmentType<?>, Object> deserializeAttachmentData(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
		if (nbt.contains(AttachmentTarget.NBT_ATTACHMENT_KEY, NbtElement.COMPOUND_TYPE)) {
			var attachments = new IdentityHashMap<AttachmentType<?>, Object>();
			NbtCompound compound = nbt.getCompound(AttachmentTarget.NBT_ATTACHMENT_KEY);

			for (String key : compound.getKeys()) {
				AttachmentType<?> type = AttachmentRegistryImpl.get(Identifier.of(key));

				if (type == null) {
					LOGGER.warn("Unknown attachment type {} found when deserializing, skipping", key);
					continue;
				}

				Codec<?> codec = type.persistenceCodec();

				if (codec != null) {
					RegistryOps<NbtElement> registryOps = wrapperLookup.getOps(NbtOps.INSTANCE);
					codec.parse(registryOps, compound.get(key))
							.ifError(partial -> {
								LOGGER.warn("Couldn't deserialize attachment {}, skipping. Error:", type.identifier());
								LOGGER.warn(partial.message());
							})
							.ifSuccess(
									deserialized -> attachments.put(type, deserialized)
							);
				}
			}

			if (attachments.isEmpty()) {
				return null;
			}

			return attachments;
		}

		return null;
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
