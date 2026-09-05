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

package net.fabricmc.fabric.test.networking.brokenpayloads;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class BrokenPayloadsTest implements ModInitializer {
	@Override
	public void onInitialize() {
		PayloadTypeRegistry.serverboundPlay().register(ServerboundSendBrokenOnDecodeCustomPacketToClient.TYPE, ServerboundSendBrokenOnDecodeCustomPacketToClient.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ServerboundSendBrokenOnEncodeCustomPacketToClient.TYPE, ServerboundSendBrokenOnEncodeCustomPacketToClient.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(BrokenOnDecode.TYPE, BrokenOnDecode.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(BrokenOnEncode.TYPE, BrokenOnEncode.STREAM_CODEC);

		PayloadTypeRegistry.clientboundPlay().register(BrokenOnDecode.TYPE, BrokenOnDecode.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(BrokenOnEncode.TYPE, BrokenOnEncode.STREAM_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(ServerboundSendBrokenOnDecodeCustomPacketToClient.TYPE, (_, context) -> {
			context.responseSender().sendPacket(new BrokenOnDecode());
		});
		ServerPlayNetworking.registerGlobalReceiver(ServerboundSendBrokenOnEncodeCustomPacketToClient.TYPE, (_, context) -> {
			context.responseSender().sendPacket(new BrokenOnEncode());
		});
	}
}
