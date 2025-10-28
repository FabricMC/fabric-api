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

/**
 * The Networking API, version 1.
 *
 * <p>There are three stages of Minecraft networking, all of which are supported in this API:
 * <dl>
 *     <dt>LOGIN</dt>
 *     <dd>This is the initial stage, before the player logs into the world. If using a proxy server,
 *     the packets in this stage may be intercepted and discarded by the proxy. <strong>Most of the pre-1.20.2
 *     uses of this event should be replaced with the CONFIGURATION stage.</strong>
 *     Related events are found at {@link net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents},
 *     and related methods are found at {@link net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking}.
 *     </dd>
 *     <dt>CONFIGURATION</dt>
 *     <dd>This is the stage after LOGIN. The player is authenticated, but still hasn't joined the
 *     world at this point. Servers can use this phase to send configurations or verify client's mod
 *     versions. Note that some server mods allow players in the PLAY stage to re-enter this stage,
 *     for example when a player chooses a minigame server in a lobby.
 *     Related events are found at {@link net.fabricmc.fabric.api.networking.v1.S2CConfigurationChannelEvents}
 *     {@link net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents}, and related methods are found at
 *     {@link net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking}.
 *     </dd>
 *     <dt>PLAY</dt>
 *     <dd>This is the stage after CONFIGURATION, where gameplay-related packets are sent and received.
 *     The player has joined the world and is playing the game. Related events are found at
 *     {@link net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents}
 *     and {@link net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents}, and related methods are found at
 *     {@link net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking}.</dd>
 * </dl>
 *
 * <p>In addition, this API includes helpers for {@linkplain
 * net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry registering custom packet payloads} and {@linkplain
 * net.fabricmc.fabric.api.networking.v1.PlayerLookup player lookups}.
 */

@NullMarked
package net.fabricmc.fabric.api.networking.v1;

import org.jspecify.annotations.NullMarked;
