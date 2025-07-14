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

		CustomClickActionEvents.playClickActionEvent(NetworkingTestmods.id("play_event")).register(
				context -> {
					NbtElement payload = context.payload();
					String payloadString = payload != null ? payload.toString() : "no payload";
					Text message = Text.translatable("key.fabric-networking-api-v1-testmod.customClick.play.received", payloadString);
					context.handler().getPlayer().sendMessage(message);
				}
		);

		ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
			if (showDialogDuringConfiguration) {
				RegistryEntry<Dialog> testDialog = server.getRegistryManager()
						.getOrThrow(RegistryKeys.DIALOG)
						.getOrThrow(CONFIGURATION_TEST_DIALOG);

				// important: use a task to prevent this dialog from being quickly skipped over
				handler.addTask(new TestDialogConfigurationTask(testDialog));
			}
		});

		CustomClickActionEvents.configurationClickActionEvent(NetworkingTestmods.id("configuration_event")).register(
				context -> {
					NbtElement payload = context.payload();
					String payloadString = payload != null ? payload.toString() : "no payload";
					NetworkingTestmods.LOGGER.info("Received configuration event with payload: {}", payloadString);

					// important: make sure to complete the task to continue to the game
					context.handler().completeTask(TestDialogConfigurationTask.KEY);

					showDialogDuringConfiguration = false;
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
