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

package net.fabricmc.fabric.impl.attachment.sync.s2c;

import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.attachment.sync.AttachmentChange;

public record AttachmentSyncPayloadS2C(List<AttachmentChange> attachments) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, AttachmentSyncPayloadS2C> CODEC = StreamCodec.composite(
			AttachmentChange.PACKET_CODEC.apply(ByteBufCodecs.list()), AttachmentSyncPayloadS2C::attachments,
			AttachmentSyncPayloadS2C::new
	);
	public static final Identifier PACKET_ID = Identifier.fromNamespaceAndPath("fabric", "attachment_sync_v1");
	public static final Type<AttachmentSyncPayloadS2C> ID = new Type<>(PACKET_ID);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
