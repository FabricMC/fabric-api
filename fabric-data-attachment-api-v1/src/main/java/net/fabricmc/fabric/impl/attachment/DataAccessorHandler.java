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

import net.minecraft.world.level.storage.ValueInput;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

/**
 * Replacement logic to handle applying attachments when using the /data command.
 * This applies the changes using the high level APIs, ensuring that the changes are correctly synced to the client.
 */
public class DataAccessorHandler {
	public static final ScopedValue<Void> APPLYING_DATA_CHANGE = ScopedValue.newInstance();

	public static void applyDataChanges(AttachmentTarget target, ValueInput data, Runnable applyData) {
		AttachmentTargetImpl targetImpl = (AttachmentTargetImpl) target;

		Map<AttachmentType<?>, ?> oldAttachments = targetImpl.fabric_getAttachments();
		ScopedValue.where(APPLYING_DATA_CHANGE, null).run(applyData);

		if (oldAttachments != targetImpl.fabric_getAttachments()) {
			throw new AssertionError("Attachment data changed during data change application.");
		}

		IdentityHashMap<AttachmentType<?>, Object> newAttachments = AttachmentSerializingImpl.deserializeAttachmentData(data);

		if (oldAttachments == null && newAttachments == null) {
			// No attachments before or after, nothing to do
			return;
		} else if (oldAttachments != null && (newAttachments == null || newAttachments.isEmpty())) {
			// Clear all attachments - copy keys to avoid ConcurrentModificationException
			oldAttachments.keySet().stream()
					.filter(AttachmentType::isPersistent)
					.toList()
					.forEach(target::removeAttached);
			return;
		}

		// Update the new attachments
		newAttachments.forEach((attachmentType, o) -> target.setAttached((AttachmentType) attachmentType, o));

		// Remove all of the removed attachments - copy keys to avoid ConcurrentModificationException
		if (oldAttachments != null) {
			oldAttachments.keySet().stream()
					.filter(AttachmentType::isPersistent)
					.filter(attachmentType -> !newAttachments.containsKey(attachmentType))
					.toList()
					.forEach(target::removeAttached);
		}
	}
}
