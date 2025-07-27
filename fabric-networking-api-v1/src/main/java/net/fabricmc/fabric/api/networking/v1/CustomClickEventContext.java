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

package net.fabricmc.fabric.api.networking.v1;

import java.util.Optional;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Contains data about a {@linkplain net.minecraft.text.ClickEvent.Custom custom click event} when one is received on
 * the server. Custom click events may be received either during the PLAY or in CONFIGURATION phases. If the event is
 * received during PLAY, then a player entity will be provided.
 */
public sealed interface CustomClickEventContext permits CustomClickEventContext.Play, CustomClickEventContext.Configuration {
	/**
	 * The handler responsible for the event.
	 */
	ServerCommonNetworkHandler handler();

	/**
	 * The payload received with this event. If no payload is received, then this payload will be empty.
	 */
	Optional<NbtElement> payload();

	/**
	 * The context data when a custom click event is received during the PLAY phase on the server.
	 */
	@ApiStatus.NonExtendable
	non-sealed interface Play extends CustomClickEventContext {
		/**
		 * The play handler responsible for the event.
		 */
		@Override
		ServerPlayNetworkHandler handler();
	}

	/**
	 * The context data when a custom click event is received during the CONFIGURATION phase on the server.
	 */
	@ApiStatus.NonExtendable
	non-sealed interface Configuration extends CustomClickEventContext {
		/**
		 * The configuration handler responsible for the event.
		 */
		@Override
		ServerConfigurationNetworkHandler handler();
	}
}
