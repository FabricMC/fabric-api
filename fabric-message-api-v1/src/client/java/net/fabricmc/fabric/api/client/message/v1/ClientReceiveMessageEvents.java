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
import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;

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
	public static final Event<AllowChat> ALLOW_CHAT = EventFactory.createArrayBacked(AllowChat.class, listeners -> (message, playerChatMessage, sender, boundChatType, timeStamp) -> {
		boolean allow = true;

		for (AllowChat listener : listeners) {
			allow &= listener.allowReceiveChatMessage(message, playerChatMessage, sender, boundChatType, timeStamp);
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
	 * To toggle overlay, return false and call either
	 * {@link net.minecraft.client.player.LocalPlayer#sendSystemMessage(Component) LocalPlayer.sendSystemMessage(message)} or
	 * {@link net.minecraft.client.player.LocalPlayer#sendOverlayMessage(Component) LocalPlayer.sendOverlayMessage(message)}.
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
	 * and manually add the new message to the chat hud using {@link ChatComponent#addPlayerMessage(Component, net.minecraft.network.chat.MessageSignature, net.minecraft.client.gui.components.GuiMessageTag)}
	 */
	public static final Event<Chat> CHAT = EventFactory.createArrayBacked(Chat.class, listeners -> (message, playerChatMessage, sender, boundChatType, timeStamp) -> {
		for (Chat listener : listeners) {
			listener.onReceiveChatMessage(message, playerChatMessage, sender, boundChatType, timeStamp);
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
	public static final Event<ChatCanceled> CHAT_CANCELED = EventFactory.createArrayBacked(ChatCanceled.class, listeners -> (message, playerChatMessage, sender, boundChatType, timeStamp) -> {
		for (ChatCanceled listener : listeners) {
			listener.onReceiveChatMessageCanceled(message, playerChatMessage, sender, boundChatType, timeStamp);
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

	/**
	 * An event triggered when the client receives a title from the server, before the HUD is updated.
	 * Mods can use this to block the title.
	 *
	 * <p>All listeners are called. If any listener returns {@code false},
	 * {@link #TITLE_CANCELED} is triggered instead of {@link #MODIFY_TITLE} and {@link #TITLE}.
	 */
	public static final Event<AllowTitle> ALLOW_TITLE = EventFactory.createArrayBacked(AllowTitle.class, listeners -> title -> {
		boolean allow = true;

		for (AllowTitle listener : listeners) {
			allow &= listener.allowReceiveTitle(title);
		}

		return allow;
	});

	/**
	 * An event triggered to modify a title received from the server, before the HUD is updated.
	 * Is not called when {@linkplain #ALLOW_TITLE the title is blocked}.
	 * Each listener receives the title returned by the previous listener.
	 * Use {@link #TITLE} if not modifying the title.
	 */
	public static final Event<ModifyTitle> MODIFY_TITLE = EventFactory.createArrayBacked(ModifyTitle.class, listeners -> title -> {
		for (ModifyTitle listener : listeners) {
			title = listener.modifyReceivedTitle(title);
		}

		return title;
	});

	/**
	 * An event triggered when the client receives a title from the server, before the HUD is updated.
	 * Receives the result of {@link #MODIFY_TITLE} and is not called when
	 * {@linkplain #ALLOW_TITLE the title is blocked}.
	 * Mods can use this to listen to the title.
	 */
	public static final Event<Title> TITLE = EventFactory.createArrayBacked(Title.class, listeners -> title -> {
		for (Title listener : listeners) {
			listener.onReceiveTitle(title);
		}
	});

	/**
	 * An event triggered when receiving a title is canceled with {@link #ALLOW_TITLE}.
	 * Receives the original title from the server.
	 */
	public static final Event<TitleCanceled> TITLE_CANCELED = EventFactory.createArrayBacked(TitleCanceled.class, listeners -> title -> {
		for (TitleCanceled listener : listeners) {
			listener.onReceiveTitleCanceled(title);
		}
	});

	/**
	 * An event triggered when the client receives a subtitle from the server, before the HUD is updated.
	 * Mods can use this to block the subtitle.
	 *
	 * <p>All listeners are called. If any listener returns {@code false},
	 * {@link #SUBTITLE_CANCELED} is triggered instead of {@link #MODIFY_SUBTITLE} and {@link #SUBTITLE}.
	 */
	public static final Event<AllowSubtitle> ALLOW_SUBTITLE = EventFactory.createArrayBacked(AllowSubtitle.class, listeners -> subtitle -> {
		boolean allow = true;

		for (AllowSubtitle listener : listeners) {
			allow &= listener.allowReceiveSubtitle(subtitle);
		}

		return allow;
	});

	/**
	 * An event triggered to modify a subtitle received from the server, before the HUD is updated.
	 * Is not called when {@linkplain #ALLOW_SUBTITLE the subtitle is blocked}.
	 * Each listener receives the subtitle returned by the previous listener.
	 * Use {@link #SUBTITLE} if not modifying the subtitle.
	 */
	public static final Event<ModifySubtitle> MODIFY_SUBTITLE = EventFactory.createArrayBacked(ModifySubtitle.class, listeners -> subtitle -> {
		for (ModifySubtitle listener : listeners) {
			subtitle = listener.modifyReceivedSubtitle(subtitle);
		}

		return subtitle;
	});

	/**
	 * An event triggered when the client receives a subtitle from the server, before the HUD is updated.
	 * Receives the result of {@link #MODIFY_SUBTITLE} and is not called when
	 * {@linkplain #ALLOW_SUBTITLE the subtitle is blocked}.
	 * Mods can use this to listen to the subtitle.
	 */
	public static final Event<Subtitle> SUBTITLE = EventFactory.createArrayBacked(Subtitle.class, listeners -> subtitle -> {
		for (Subtitle listener : listeners) {
			listener.onReceiveSubtitle(subtitle);
		}
	});

	/**
	 * An event triggered when receiving a subtitle is canceled with {@link #ALLOW_SUBTITLE}.
	 * Receives the original subtitle from the server.
	 */
	public static final Event<SubtitleCanceled> SUBTITLE_CANCELED = EventFactory.createArrayBacked(SubtitleCanceled.class, listeners -> subtitle -> {
		for (SubtitleCanceled listener : listeners) {
			listener.onReceiveSubtitleCanceled(subtitle);
		}
	});

	/**
	 * An event triggered when the server requests that titles and subtitles be cleared,
	 * before the HUD is updated. This is not triggered when a title expires or is cleared locally.
	 */
	public static final Event<ClearTitles> CLEAR_TITLES = EventFactory.createArrayBacked(ClearTitles.class, listeners -> resetTimes -> {
		for (ClearTitles listener : listeners) {
			listener.onClearTitles(resetTimes);
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
		 * @param playerChatMessage      the signed message received from the server (nullable)
		 * @param sender             the sender of the message (nullable)
		 * @param boundChatType             the parameters of the message
		 * @param timeStamp the timestamp when the message was received
		 * @return {@code true} if the message should be displayed, otherwise {@code false}
		 */
		boolean allowReceiveChatMessage(Component message, @Nullable PlayerChatMessage playerChatMessage, @Nullable GameProfile sender, ChatType.Bound boundChatType, Instant timeStamp);
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
		 * To toggle overlay, return false and call either
		 * {@link net.minecraft.client.player.LocalPlayer#sendSystemMessage(Component) LocalPlayer.sendSystemMessage(message)} or
		 * {@link net.minecraft.client.player.LocalPlayer#sendOverlayMessage(Component) LocalPlayer.sendOverlayMessage(message)}.
		 *
		 * @param message the message received from the server
		 * @param overlay whether the message will be displayed in the action bar
		 * @return {@code true} if the message should be displayed, otherwise {@code false}
		 */
		boolean allowReceiveGameMessage(Component message, boolean overlay);
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
		Component modifyReceivedGameMessage(Component message, boolean overlay);
	}

	@FunctionalInterface
	public interface Chat {
		/**
		 * Called when the client receives a chat message,
		 * which is any message sent by a player. Is not called when
		 * {@linkplain #ALLOW_CHAT chat messages are blocked}.
		 *
		 * @param message             the message received from the server
		 * @param playerChatMessage   the player chat message received from the server (nullable)
		 * @param sender              the sender of the message (nullable)
		 * @param boundChatType       the parameters of the message
		 * @param timeStamp           the timestamp when the message was received
		 */
		void onReceiveChatMessage(Component message, @Nullable PlayerChatMessage playerChatMessage, @Nullable GameProfile sender, ChatType.Bound boundChatType, Instant timeStamp);
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
		void onReceiveGameMessage(Component message, boolean overlay);
	}

	@FunctionalInterface
	public interface ChatCanceled {
		/**
		 * Called when receiving a chat message is canceled with {@link #ALLOW_CHAT}.
		 *
		 * @param message            the message received from the server
		 * @param playerChatMessage  the signed message received from the server (nullable)
		 * @param sender             the sender of the message (nullable)
		 * @param boundChatType      the parameters of the message
		 * @param timeStamp          the timestamp when the message was received
		 */
		void onReceiveChatMessageCanceled(Component message, @Nullable PlayerChatMessage playerChatMessage, @Nullable GameProfile sender, ChatType.Bound boundChatType, Instant timeStamp);
	}

	@FunctionalInterface
	public interface GameCanceled {
		/**
		 * Called when receiving a game message is canceled with {@link #ALLOW_GAME}.
		 *
		 * @param message the message received from the server
		 * @param overlay whether the message would have been displayed in the action bar
		 */
		void onReceiveGameMessageCanceled(Component message, boolean overlay);
	}

	@FunctionalInterface
	public interface AllowTitle {
		/**
		 * Called when the client receives a title from the server, before the HUD is updated.
		 * Returning {@code false} prevents the title from being set and triggers {@link #TITLE_CANCELED}.
		 * The previously displayed title is left unchanged.
		 *
		 * @param title the title received from the server
		 * @return {@code true} if the title should be set, otherwise {@code false}
		 */
		boolean allowReceiveTitle(Component title);
	}

	@FunctionalInterface
	public interface ModifyTitle {
		/**
		 * Called to modify a title received from the server, unless {@link #ALLOW_TITLE} blocks it.
		 *
		 * @param title the title, as modified by previous listeners
		 * @return the modified title, or the original {@code title} if not modified
		 */
		Component modifyReceivedTitle(Component title);
	}

	@FunctionalInterface
	public interface Title {
		/**
		 * Called before a title received from the server is set in the HUD,
		 * unless {@link #ALLOW_TITLE} blocks it.
		 *
		 * @param title the title, as modified by {@link #MODIFY_TITLE}
		 */
		void onReceiveTitle(Component title);
	}

	@FunctionalInterface
	public interface TitleCanceled {
		/**
		 * Called when receiving a title is canceled with {@link #ALLOW_TITLE}.
		 *
		 * @param title the original title received from the server
		 */
		void onReceiveTitleCanceled(Component title);
	}

	@FunctionalInterface
	public interface AllowSubtitle {
		/**
		 * Called when the client receives a subtitle from the server, before the HUD is updated.
		 * Returning {@code false} prevents the subtitle from being set and triggers {@link #SUBTITLE_CANCELED}.
		 * The previously displayed subtitle is left unchanged.
		 *
		 * @param subtitle the subtitle received from the server
		 * @return {@code true} if the subtitle should be set, otherwise {@code false}
		 */
		boolean allowReceiveSubtitle(Component subtitle);
	}

	@FunctionalInterface
	public interface ModifySubtitle {
		/**
		 * Called to modify a subtitle received from the server, unless {@link #ALLOW_SUBTITLE} blocks it.
		 *
		 * @param subtitle the subtitle, as modified by previous listeners
		 * @return the modified subtitle, or the original {@code subtitle} if not modified
		 */
		Component modifyReceivedSubtitle(Component subtitle);
	}

	@FunctionalInterface
	public interface Subtitle {
		/**
		 * Called before a subtitle received from the server is set in the HUD,
		 * unless {@link #ALLOW_SUBTITLE} blocks it.
		 *
		 * @param subtitle the subtitle, as modified by {@link #MODIFY_SUBTITLE}
		 */
		void onReceiveSubtitle(Component subtitle);
	}

	@FunctionalInterface
	public interface SubtitleCanceled {
		/**
		 * Called when receiving a subtitle is canceled with {@link #ALLOW_SUBTITLE}.
		 *
		 * @param subtitle the original subtitle received from the server
		 */
		void onReceiveSubtitleCanceled(Component subtitle);
	}

	@FunctionalInterface
	public interface ClearTitles {
		/**
		 * Called before a server request clears the HUD's title and subtitle.
		 *
		 * @param resetTimes whether the title fade-in, stay and fade-out times will also be reset to their defaults
		 */
		void onClearTitles(boolean resetTimes);
	}
}
