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

package net.fabricmc.fabric.test.networking.client.splitter;

import com.mojang.brigadier.Command;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.test.networking.splitter.NetworkingSplitterTest;

public class NetworkingSplitterClientTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(NetworkingSplitterTest.LargePayload.TYPE, (payload, context) -> {
			NetworkingSplitterTest.validateLargePacketData(payload.index(), payload.data(), "client");
			context.responseSender().sendPacket(payload);
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(
				ClientCommands.literal("clientnetworktestcommand")
						.then(ClientCommands.literal("large_creative_only").executes(context -> {
							ClientPlayNetworking.send(new NetworkingSplitterTest.LargeCreativeOnlyPayload(1, NetworkingSplitterTest.RANDOM_DATA[1]));
							return Command.SINGLE_SUCCESS;
						}))));
	}
}
