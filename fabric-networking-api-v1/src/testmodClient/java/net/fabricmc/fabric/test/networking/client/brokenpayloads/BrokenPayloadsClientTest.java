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

package net.fabricmc.fabric.test.networking.client.brokenpayloads;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.test.networking.brokenpayloads.BrokenOnDecode;
import net.fabricmc.fabric.test.networking.brokenpayloads.BrokenOnEncode;
import net.fabricmc.fabric.test.networking.brokenpayloads.ServerboundSendBrokenOnDecodeCustomPacketToClient;
import net.fabricmc.fabric.test.networking.brokenpayloads.ServerboundSendBrokenOnEncodeCustomPacketToClient;

public class BrokenPayloadsClientTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommands.literal("clientbound_broken_on_decode_test").executes(context -> {
				context.getSource().getPlayer().connection.send(ClientPlayNetworking.createServerboundPacket(new ServerboundSendBrokenOnDecodeCustomPacketToClient()));
				return 1;
			}));
			dispatcher.register(ClientCommands.literal("clientbound_broken_on_encode_test").executes(context -> {
				context.getSource().getPlayer().connection.send(ClientPlayNetworking.createServerboundPacket(new ServerboundSendBrokenOnEncodeCustomPacketToClient()));
				return 1;
			}));
			dispatcher.register(ClientCommands.literal("serverbound_broken_on_decode_test").executes(context -> {
				context.getSource().getPlayer().connection.send(ClientPlayNetworking.createServerboundPacket(new BrokenOnDecode()));
				return 1;
			}));
			dispatcher.register(ClientCommands.literal("serverbound_broken_on_encode_test").executes(context -> {
				context.getSource().getPlayer().connection.send(ClientPlayNetworking.createServerboundPacket(new BrokenOnEncode()));
				return 1;
			}));
		});
	}
}
