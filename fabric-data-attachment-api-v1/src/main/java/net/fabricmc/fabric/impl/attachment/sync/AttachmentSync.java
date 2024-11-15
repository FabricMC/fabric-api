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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerConfigurationTask;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.attachment.AttachmentEntrypoint;
import net.fabricmc.fabric.impl.attachment.AttachmentRegistryImpl;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.fabricmc.fabric.impl.attachment.sync.c2s.AcceptedAttachmentsPayloadC2S;
import net.fabricmc.fabric.impl.attachment.sync.s2c.AttachmentSyncPayloadS2C;
import net.fabricmc.fabric.impl.attachment.sync.s2c.RequestAcceptedAttachmentsPayloadS2C;
import net.fabricmc.fabric.mixin.networking.accessor.ServerCommonNetworkHandlerAccessor;

public class AttachmentSync implements ModInitializer {
	public static final int MAX_IDENTIFIER_SIZE = 256;

	public static AcceptedAttachmentsPayloadC2S createResponsePayload() {
		return new AcceptedAttachmentsPayloadC2S(AttachmentRegistryImpl.getSyncableAttachments());
	}

	public static void trySync(AttachmentSyncPayloadS2C payload, ServerPlayerEntity player) {
		if (!payload.attachments().isEmpty()) {
			ServerPlayNetworking.send(player, payload);
		}
	}

	private static Set<Identifier> decodeResponsePayload(AcceptedAttachmentsPayloadC2S payload) {
		Set<Identifier> atts = payload.acceptedAttachments();
		Set<Identifier> syncable = AttachmentRegistryImpl.getSyncableAttachments();
		atts.retainAll(syncable);

		if (atts.size() < syncable.size()) {
			// Client doesn't support all
			AttachmentEntrypoint.LOGGER.warn(
					"Client does not support the syncable attachments {}",
					syncable.stream().filter(id -> !atts.contains(id)).map(Identifier::toString).collect(Collectors.joining(", "))
			);
		}

		return atts;
	}

	@Override
	public void onInitialize() {
		// Config
		PayloadTypeRegistry.configurationC2S()
				.register(AcceptedAttachmentsPayloadC2S.ID, AcceptedAttachmentsPayloadC2S.CODEC);
		PayloadTypeRegistry.configurationS2C()
				.register(RequestAcceptedAttachmentsPayloadS2C.ID, RequestAcceptedAttachmentsPayloadS2C.CODEC);

		ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
			if (ServerConfigurationNetworking.canSend(handler, RequestAcceptedAttachmentsPayloadS2C.PACKET_ID)) {
				handler.addTask(new AttachmentSyncTask());
			} else {
				AttachmentEntrypoint.LOGGER.debug(
						"Couldn't send attachment configuration packet to client, as the client cannot receive the payload."
				);
			}
		});

		ServerConfigurationNetworking.registerGlobalReceiver(AcceptedAttachmentsPayloadC2S.ID, (payload, context) -> {
			Set<Identifier> supportedAttachments = decodeResponsePayload(payload);
			ClientConnection connection = ((ServerCommonNetworkHandlerAccessor) context.networkHandler()).getConnection();
			((SupportedAttachmentsClientConnection) connection).fabric_setSupportedAttachments(supportedAttachments);

			context.networkHandler().completeTask(AttachmentSyncTask.KEY);
		});

		// Play
		PayloadTypeRegistry.playS2C().register(AttachmentSyncPayloadS2C.ID, AttachmentSyncPayloadS2C.CODEC);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.player;
			List<AttachmentChange> changes = new ArrayList<>();
			// sync world attachments
			((AttachmentTargetImpl) player.getServerWorld()).fabric_computeInitialSyncChanges(player, changes::add);
			// sync player's own persistent attachments that couldn't be synced earlier
			((AttachmentTargetImpl) player).fabric_computeInitialSyncChanges(player, changes::add);

			if (!changes.isEmpty()) {
				AttachmentChange.partitionAndSendPackets(changes, player);
			}
		});

		// entity tracking handled in EntityTrackerEntryMixin instead, see comment
	}

	private record AttachmentSyncTask() implements ServerPlayerConfigurationTask {
		public static final Key KEY = new Key(RequestAcceptedAttachmentsPayloadS2C.PACKET_ID.toString());

		@Override
		public void sendPacket(Consumer<Packet<?>> sender) {
			sender.accept(ServerConfigurationNetworking.createS2CPacket(RequestAcceptedAttachmentsPayloadS2C.INSTANCE));
		}

		@Override
		public Key getKey() {
			return KEY;
		}
	}
}
