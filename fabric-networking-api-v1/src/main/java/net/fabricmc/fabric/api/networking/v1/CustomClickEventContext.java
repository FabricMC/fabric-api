package net.fabricmc.fabric.api.networking.v1;

import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerCommonNetworkHandler;

import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Contains data about a {@linkplain net.minecraft.text.ClickEvent.Custom custom click event} when one is received on
 * the server.
 */
public sealed interface CustomClickEventContext permits CustomClickEventContext.Play, CustomClickEventContext.Configuration {
	/**
	 * The handler responsible for the event
	 */
	ServerCommonNetworkHandler handler();

	/**
	 * The payload received with this event. If no payload is received, then this payload will be {@code null}.
	 */
	@Nullable
	NbtElement payload();

	/**
	 * The context data when a custom click event is received during the PLAY phase on the server.
	 */
	@ApiStatus.NonExtendable
	non-sealed interface Play extends CustomClickEventContext {
		/**
		 * The play handler responsible for the event
		 */
		ServerPlayNetworkHandler handler();
	}

	/**
	 * The context data when a custom click event is received during the CONFIGURATION phase on the server.
	 */
	@ApiStatus.NonExtendable
	non-sealed interface Configuration extends CustomClickEventContext {
		/**
		 * The configuration handler responsible for the event
		 */
		ServerConfigurationNetworkHandler handler();
	}
}
