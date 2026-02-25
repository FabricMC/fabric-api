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

import org.jspecify.annotations.Nullable;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.attachment.v1.GlobalAttachments;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentChange;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentSync;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentTargetInfo;

public class GlobalAttachmentsImpl implements GlobalAttachments, AttachmentTargetImpl {
	@Nullable
	private final MinecraftServer server;

	public GlobalAttachmentsImpl(@Nullable MinecraftServer server) {
		this.server = server;
	}

	@Override
	public void fabric_syncChange(AttachmentType<?> type, AttachmentChange change) {
		if (server != null) {
			// We don't use PlayerLookup.all() because when a player respawns,
			// there is a brief period where said player will not be in the server player list.
			// If a global attachment is set then, the respawning player will never receive the update.
			server.getConnection().getConnections().forEach(connection -> {
				// if packet listener is not ServerGamePacketListenerImpl, then player is not in PLAY phase yet
				// initial sync will handle it
				if (connection.getPacketListener() instanceof ServerGamePacketListenerImpl serverGamePacketListener) {
					if (((AttachmentTypeImpl<?>) type).syncPredicate().test(this, serverGamePacketListener.player)) {
						AttachmentSync.trySync(change, serverGamePacketListener.player);
					}
				}
			});
		}
	}

	@Override
	public boolean fabric_shouldTryToSync() {
		return server != null;
	}

	@Override
	public AttachmentTargetInfo<?> fabric_getSyncTargetInfo() {
		return AttachmentTargetInfo.GlobalTarget.INSTANCE;
	}

	@Override
	public RegistryAccess fabric_getRegistryAccess() {
		if (server != null) {
			return server.registryAccess();
		}

		// only used for deserializing on the server and syncing, so should not be possible to get here.
		throw new UnsupportedOperationException("GlobalAttachments does not have a registry access on the client side.");
	}
}
