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

package net.fabricmc.fabric.test.networking.clickeventtest;

import static net.minecraft.server.command.CommandManager.literal;

import java.util.function.Consumer;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.dialog.type.Dialog;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.ShowDialogS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerConfigurationTask;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.CustomClickActionEvents;
import net.fabricmc.fabric.api.networking.v1.CustomClickEventContext;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.test.networking.NetworkingTestmods;

public class CustomClickActionsTest implements ModInitializer {
	private static final RegistryKey<Dialog> PLAY_TEST_DIALOG = RegistryKey.of(RegistryKeys.DIALOG, NetworkingTestmods.id("play_custom_click_event"));
	private static final RegistryKey<Dialog> CONFIGURATION_TEST_DIALOG = RegistryKey.of(RegistryKeys.DIALOG, NetworkingTestmods.id("configuration_custom_click_event"));
	private boolean showDialogDuringConfiguration = false;

	private void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("networktestcommand")
				.then(literal("testPlayClickAction").executes(ctx -> {
					ServerPlayerEntity player = ctx.getSource().getPlayer();

					if (player != null) {
						RegistryEntry<Dialog> testDialog = ctx.getSource()
								.getRegistryManager()
								.getOrThrow(RegistryKeys.DIALOG)
								.getOrThrow(PLAY_TEST_DIALOG);
						player.openDialog(testDialog);
					}

					return Command.SINGLE_SUCCESS;
				}))
				.then(literal("testConfigurationClickAction").executes(ctx -> {
					showDialogDuringConfiguration = true;
					ServerPlayNetworking.reconfigure(ctx.getSource().getPlayer());
					return Command.SINGLE_SUCCESS;
				}))
		);
	}

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			this.registerCommand(dispatcher);
		});

		ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
			if (showDialogDuringConfiguration) {
				RegistryEntry<Dialog> testDialog = server.getRegistryManager()
						.getOrThrow(RegistryKeys.DIALOG)
						.getOrThrow(CONFIGURATION_TEST_DIALOG);

				// important: use a task to prevent this dialog from being quickly skipped over
				handler.addTask(new TestDialogConfigurationTask(testDialog));
			}
		});

		CustomClickActionEvents.customClickActionReceivedEvent(NetworkingTestmods.id("test_event")).register(
				context -> {
					switch (context) {
						case CustomClickEventContext.Configuration configuration -> {
							String payloadString = context.payload()
									.map(NbtElement::toString)
									.orElse("no payload");
							NetworkingTestmods.LOGGER.info("Received configuration event with payload: {}", payloadString);

							// important: make sure to complete the task to continue to the game
							configuration.handler().completeTask(TestDialogConfigurationTask.KEY);

							showDialogDuringConfiguration = false;
						}
						case CustomClickEventContext.Play play -> {
							String payloadString = context.payload()
									.map(NbtElement::toString)
									.orElse("no payload");
							Text message = Text.translatable("key.fabric-networking-api-v1-testmod.customClick.play.received", payloadString);
							play.handler().getPlayer().sendMessage(message);
						}
					}
				}
		);
	}

	public record TestDialogConfigurationTask(RegistryEntry<Dialog> dialog) implements ServerPlayerConfigurationTask {
		public static final Key KEY = new Key(Identifier.of(NetworkingTestmods.ID, "configure_dialog").toString());

		@Override
		public void sendPacket(Consumer<Packet<?>> sender) {
			var packet = new ShowDialogS2CPacket(dialog);
			sender.accept(packet);
		}

		@Override
		public Key getKey() {
			return KEY;
		}
	}
}
