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

package net.fabricmc.fabric.api.client.message.v1;

import java.time.Instant;

import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Contains client-side events triggered when receiving messages.
 */
public final class ClientReceiveMessageEvents {
	private ClientReceiveMessageEvents() {
	}

	/**
	 * An event triggered when the client receives a chat message,
	 * which is any message sent by a player. Mods can use this to block the message.
	 *
	 * <p>If a listener returned {@code false}, the message will not be displayed,
	 * the remaining listeners will be called (if any), and
	 * {@link #CHAT_CANCELED} will be triggered instead of {@link #CHAT}.
	 */
	public static final Event<AllowChat> ALLOW_CHAT = EventFactory.createArrayBacked(AllowChat.class, listeners -> (message, signedMessage, sender, params, receptionTimestamp) -> {
		boolean allow = true;

		for (AllowChat listener : listeners) {
			allow &= listener.allowReceiveChatMessage(message, signedMessage, sender, params, receptionTimestamp);
		}

		return allow;
	});

	/**
	 * An event triggered when the client receives a game message,
	 * which is any message sent by the server.
	 * Mods can use this to block the message or toggle overlay.
	 *
	 * <p>If a listener returned {@code false}, the message will not be displayed,
	 * the remaining listeners will be called (if any), and
	 * {@link #GAME_CANCELED} will be triggered instead of {@link #MODIFY_GAME}.
	 *
	 * <p>Overlay is whether the message will be displayed in the action bar.
	 * To toggle overlay, return false and call
	 * {@link net.minecraft.client.network.ClientPlayerEntity#sendMessage(Text, boolean) ClientPlayerEntity.sendMessage(message, overlay)}.
	 */
	public static final Event<AllowGame> ALLOW_GAME = EventFactory.createArrayBacked(AllowGame.class, listeners -> (message, overlay) -> {
		boolean allow = true;

		for (AllowGame listener : listeners) {
			allow &= listener.allowReceiveGameMessage(message, overlay);
		}

		return allow;
	});

	/**
	 * An event triggered when the client receives a game message,
	 * which is any message sent by the server. Is not called when
	 * {@linkplain #ALLOW_GAME game messages are blocked}.
	 * Mods can use this to modify the message.
	 * Use {@link #GAME} if not modifying the message.
	 *
	 * <p>Overlay is whether the message will be displayed in the action bar.
	 * Use {@link #ALLOW_GAME to toggle overlay}.
	 */
	public static final Event<ModifyGame> MODIFY_GAME = EventFactory.createArrayBacked(ModifyGame.class, listeners -> (message, overlay) -> {
		for (ModifyGame listener : listeners) {
			message = listener.modifyReceivedGameMessage(message, overlay);
		}

		return message;
	});

	/**
	 * An event triggered when the client receives a chat message,
	 * which is any message sent by a player. Is not called when
	 * {@linkplain #ALLOW_CHAT chat messages are blocked}.
	 * Mods can use this to listen to the message.
	 *
	 * <p>If mods want to modify the message, they should use {@link #ALLOW_CHAT}
	 * and manually add the new message to the chat hud using {@link ChatHud#addMessage(Text)}
	 */
	public static final Event<Chat> CHAT = EventFactory.createArrayBacked(Chat.class, listeners -> (message, signedMessage, sender, params, receptionTimestamp) -> {
		for (Chat listener : listeners) {
			listener.onReceiveChatMessage(message, signedMessage, sender, params, receptionTimestamp);
		}
	});

	/**
	 * An event triggered when the client receives a game message,
	 * which is any message sent by the server. Is not called when
	 * {@linkplain #ALLOW_GAME game messages are blocked}.
	 * Mods can use this to listen to the message.
	 *
	 * <p>Overlay is whether the message will be displayed in the action bar.
	 * Use {@link #ALLOW_GAME to toggle overlay}.
	 */
	public static final Event<Game> GAME = EventFactory.createArrayBacked(Game.class, listeners -> (message, overlay) -> {
		for (Game listener : listeners) {
			listener.onReceiveGameMessage(message, overlay);
		}
	});

	/**
	 * An event triggered when receiving a chat message is canceled with {@link #ALLOW_CHAT}.
	 */
	public static final Event<ChatCanceled> CHAT_CANCELED = EventFactory.createArrayBacked(ChatCanceled.class, listeners -> (message, signedMessage, sender, params, receptionTimestamp) -> {
		for (ChatCanceled listener : listeners) {
			listener.onReceiveChatMessageCanceled(message, signedMessage, sender, params, receptionTimestamp);
		}
	});

	/**
	 * An event triggered when receiving a game message is canceled with {@link #ALLOW_GAME}.
	 *
	 * <p>Overlay is whether the message would have been displayed in the action bar.
	 */
	public static final Event<GameCanceled> GAME_CANCELED = EventFactory.createArrayBacked(GameCanceled.class, listeners -> (message, overlay) -> {
		for (GameCanceled listener : listeners) {
			listener.onReceiveGameMessageCanceled(message, overlay);
		}
	});

	@FunctionalInterface
	public interface AllowChat {
		/**
		 * Called when the client receives a chat message,
		 * which is any message sent by a player.
		 * Returning {@code false} prevents the message from being displayed, and
		 * {@link #CHAT_CANCELED} will be triggered instead of {@link #CHAT}.
		 *
		 * @param message            the message received from the server
		 * @param signedMessage      the signed message received from the server (nullable)
		 * @param sender             the sender of the message (nullable)
		 * @param params             the parameters of the message
		 * @param receptionTimestamp the timestamp when the message was received
		 * @return {@code true} if the message should be displayed, otherwise {@code false}
		 */
		boolean allowReceiveChatMessage(Text message, @Nullable SignedMessage signedMessage, @Nullable GameProfile sender, MessageType.Parameters params, Instant receptionTimestamp);
	}

	@FunctionalInterface
	public interface AllowGame {
		/**
		 * Called when the client receives a game message,
		 * which is any message sent by the server. Returning {@code false}
		 * prevents the message from being displayed, and
		 * {@link #GAME_CANCELED} will be triggered instead of {@link #MODIFY_GAME}.
		 *
		 * <p>Overlay is whether the message will be displayed in the action bar.
		 * To toggle overlay, return false and call
		 * {@link net.minecraft.client.network.ClientPlayerEntity#sendMessage(Text, boolean) ClientPlayerEntity.sendMessage(message, overlay)}.
		 *
		 * @param message the message received from the server
		 * @param overlay whether the message will be displayed in the action bar
		 * @return {@code true} if the message should be displayed, otherwise {@code false}
		 */
		boolean allowReceiveGameMessage(Text message, boolean overlay);
	}

	@FunctionalInterface
	public interface ModifyGame {
		/**
		 * Called when the client receives a game message,
		 * which is any message sent by the server. Is not called when
		 * {@linkplain #ALLOW_GAME game messages are blocked}.
		 * Use {@link #GAME} if not modifying the message.
		 *
		 * <p>Overlay is whether the message will be displayed in the action bar.
		 * Use {@link #ALLOW_GAME} to toggle overlay.
		 *
		 * @param message the message received from the server
		 * @param overlay whether the message will be displayed in the action bar
		 * @return the modified message to display or the original {@code message} if the message is not modified
		 */
		Text modifyReceivedGameMessage(Text message, boolean overlay);
	}

	@FunctionalInterface
	public interface Chat {
		/**
		 * Called when the client receives a chat message,
		 * which is any message sent by a player. Is not called when
		 * {@linkplain #ALLOW_CHAT chat messages are blocked}.
		 *
		 * @param message            the message received from the server
		 * @param signedMessage      the signed message received from the server (nullable)
		 * @param sender             the sender of the message (nullable)
		 * @param params             the parameters of the message
		 * @param receptionTimestamp the timestamp when the message was received
		 */
		void onReceiveChatMessage(Text message, @Nullable SignedMessage signedMessage, @Nullable GameProfile sender, MessageType.Parameters params, Instant receptionTimestamp);
	}

	@FunctionalInterface
	public interface Game {
		/**
		 * Called when the client receives a game message,
		 * which is any message sent by the server. Is not called when
		 * {@linkplain #ALLOW_GAME game messages are blocked}.
		 *
		 * <p>Overlay is whether the message will be displayed in the action bar.
		 * Use {@link #ALLOW_GAME} to toggle overlay.
		 *
		 * @param message the message received from the server
		 * @param overlay whether the message will be displayed in the action bar
		 */
		void onReceiveGameMessage(Text message, boolean overlay);
	}

	@FunctionalInterface
	public interface ChatCanceled {
		/**
		 * Called when receiving a chat message is canceled with {@link #ALLOW_CHAT}.
		 *
		 * @param message            the message received from the server
		 * @param signedMessage      the signed message received from the server (nullable)
		 * @param sender             the sender of the message (nullable)
		 * @param params             the parameters of the message
		 * @param receptionTimestamp the timestamp when the message was received
		 */
		void onReceiveChatMessageCanceled(Text message, @Nullable SignedMessage signedMessage, @Nullable GameProfile sender, MessageType.Parameters params, Instant receptionTimestamp);
	}

	@FunctionalInterface
	public interface GameCanceled {
		/**
		 * Called when receiving a game message is canceled with {@link #ALLOW_GAME}.
		 *
		 * @param message the message received from the server
		 * @param overlay whether the message would have been displayed in the action bar
		 */
		void onReceiveGameMessageCanceled(Text message, boolean overlay);
	}
}
