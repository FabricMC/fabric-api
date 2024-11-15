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

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class RequestAcceptedAttachmentsPayloadS2C implements CustomPayload {
	public static final RequestAcceptedAttachmentsPayloadS2C INSTANCE = new RequestAcceptedAttachmentsPayloadS2C();
	public static final Identifier PACKET_ID = Identifier.of("fabric", "accepted_attachments_v1");
	public static final Id<RequestAcceptedAttachmentsPayloadS2C> ID = new Id<>(PACKET_ID);
	public static final PacketCodec<PacketByteBuf, RequestAcceptedAttachmentsPayloadS2C> CODEC = PacketCodec.unit(INSTANCE);

	private RequestAcceptedAttachmentsPayloadS2C() {
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
