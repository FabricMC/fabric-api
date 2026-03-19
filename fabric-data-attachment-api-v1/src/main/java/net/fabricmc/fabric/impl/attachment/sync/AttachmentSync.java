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

import io.netty.buffer.ByteBufUtil;

import net.minecraft.network.VarInt;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ConfigurationTask;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.impl.attachment.AttachmentEntrypoint;
import net.fabricmc.fabric.impl.attachment.AttachmentRegistryImpl;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.fabricmc.fabric.impl.attachment.sync.clientbound.ClientboundAttachmentSyncPayload;
import net.fabricmc.fabric.impl.attachment.sync.clientbound.ClientboundRequestAcceptedAttachmentsPayload;
import net.fabricmc.fabric.impl.attachment.sync.serverbound.ServerboundAcceptedAttachmentsPayload;
import net.fabricmc.fabric.mixin.attachment.ClientboundCustomPayloadPacketAccessor;

public class AttachmentSync implements ModInitializer {
	public static final int MAX_IDENTIFIER_SIZE = 256;
	public static final int MAX_PADDING_SIZE_IN_BYTES = AttachmentTargetInfo.MAX_SIZE_IN_BYTES + MAX_IDENTIFIER_SIZE;
	public static final int DEFAULT_MAX_DATA_SIZE;
	public static final int DEFAULT_ATTACHMENT_SYNC_PACKET_SIZE;
	private static final PacketContext.Key<Set<Identifier>> SUPPORTED_ATTACHMENTS_KEY = PacketContext.key(Identifier.fromNamespaceAndPath("fabric", "supported_attachments"));

	static {
		// ensure no splitting by default
		int identifierSize = ByteBufUtil.utf8MaxBytes(ClientboundAttachmentSyncPayload.PACKET_ID.toString());
		int networkingApiPaddingSize = VarInt.getByteSize(identifierSize) + identifierSize + 5 * 2;
		DEFAULT_MAX_DATA_SIZE = ClientboundCustomPayloadPacketAccessor.getMaxPayloadSize() - MAX_PADDING_SIZE_IN_BYTES - networkingApiPaddingSize;
		DEFAULT_ATTACHMENT_SYNC_PACKET_SIZE = MAX_PADDING_SIZE_IN_BYTES + DEFAULT_MAX_DATA_SIZE;
	}

	public static ServerboundAcceptedAttachmentsPayload createResponsePayload() {
		return new ServerboundAcceptedAttachmentsPayload(AttachmentRegistryImpl.getSyncableAttachments());
	}

	public static void trySync(AttachmentChange change, ServerPlayer player) {
		if (player.connection == null) {
			return;
		}

		Set<Identifier> supported = player.connection.getPacketContext().orElse(SUPPORTED_ATTACHMENTS_KEY, Set.of());

		if (supported.contains(change.type().identifier())) {
			ServerPlayNetworking.send(player, new ClientboundAttachmentSyncPayload(change));
		}
	}

	public static void trySync(List<AttachmentChange> changes, ServerPlayer player) {
		if (changes.size() == 1) {
			trySync(changes.getFirst(), player);
			return;
		}

		Set<Identifier> supported = player.connection.getPacketContext().orElse(SUPPORTED_ATTACHMENTS_KEY, Set.of());

		List<Packet<? super ClientGamePacketListener>> syncableChanges = new ArrayList<>();
		changes.forEach(change -> {
			if (supported.contains(change.type().identifier())) {
				syncableChanges.add(ServerPlayNetworking.createClientboundPacket(new ClientboundAttachmentSyncPayload(change)));
			}
		});

		if (!syncableChanges.isEmpty()) {
			ServerPlayNetworking.getSender(player).sendPacket(new ClientboundBundlePacket(syncableChanges));
		}
	}

	private static Set<Identifier> decodeResponsePayload(
			ServerboundAcceptedAttachmentsPayload payload) {
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
		PayloadTypeRegistry.serverboundConfiguration()
				.register(ServerboundAcceptedAttachmentsPayload.ID, ServerboundAcceptedAttachmentsPayload.CODEC);
		PayloadTypeRegistry.clientboundConfiguration()
				.register(ClientboundRequestAcceptedAttachmentsPayload.ID, ClientboundRequestAcceptedAttachmentsPayload.CODEC);

		ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
			if (ServerConfigurationNetworking.canSend(handler, ClientboundRequestAcceptedAttachmentsPayload.PACKET_ID)) {
				handler.addTask(new AttachmentSyncTask());
			} else {
				AttachmentEntrypoint.LOGGER.debug(
						"Couldn't send attachment configuration packet to client, as the client cannot receive the payload."
				);
			}
		});

		ServerConfigurationNetworking.registerGlobalReceiver(
				ServerboundAcceptedAttachmentsPayload.ID, (payload, context) -> {
					Set<Identifier> supportedAttachments = decodeResponsePayload(payload);
					context.packetListener().getPacketContext().set(SUPPORTED_ATTACHMENTS_KEY, supportedAttachments);

					context.packetListener().completeTask(AttachmentSyncTask.KEY);
				});

		// Play
		PayloadTypeRegistry.clientboundPlay().registerLarge(
				ClientboundAttachmentSyncPayload.TYPE, ClientboundAttachmentSyncPayload.CODEC, AttachmentRegistryImpl::getMaxSyncPacketSize);

		ServerPlayerEvents.JOIN.register((player) -> {
			List<AttachmentChange> changes = new ArrayList<>();
			// sync global attachments
			((AttachmentTargetImpl) player.level().globalAttachments()).fabric_computeInitialSyncChanges(player, changes::add);
			// sync level attachments
			((AttachmentTargetImpl) player.level()).fabric_computeInitialSyncChanges(player, changes::add);
			// sync player's own persistent attachments that couldn't be synced earlier
			((AttachmentTargetImpl) player).fabric_computeInitialSyncChanges(player, changes::add);

			if (!changes.isEmpty()) {
				trySync(changes, player);
			}
		});

		ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) -> {
			// sync new level's attachments
			// no conflict with previous one because the client level is recreated every time
			List<AttachmentChange> changes = new ArrayList<>();
			((AttachmentTargetImpl) destination).fabric_computeInitialSyncChanges(player, changes::add);

			if (!changes.isEmpty()) {
				trySync(changes, player);
			}
		});

		EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) -> {
			List<AttachmentChange> changes = new ArrayList<>();
			((AttachmentTargetImpl) trackedEntity).fabric_computeInitialSyncChanges(player, changes::add);

			if (!changes.isEmpty()) {
				trySync(changes, player);
			}
		});
	}

	private record AttachmentSyncTask() implements ConfigurationTask {
		public static final Type KEY = new Type(
				ClientboundRequestAcceptedAttachmentsPayload.PACKET_ID.toString());

		@Override
		public void start(Consumer<Packet<?>> sender) {
			sender.accept(ServerConfigurationNetworking.createClientboundPacket(
					ClientboundRequestAcceptedAttachmentsPayload.INSTANCE));
		}

		@Override
		public Type type() {
			return KEY;
		}
	}
}
