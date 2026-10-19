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

package net.fabricmc.fabric.api.modprotocol.v1;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.networking.v1.context.PacketContextProvider;
import net.fabricmc.fabric.impl.modprotocol.ModProtocolManager;

/**
 * Utility methods allowing to get mod versions supported by the player.
 * This will only work if the mod declares a protocol requirement.
 *
 * <p>Protocol identifier's can be any valid identifier, through by default mods defining it will use "mod" namespace and path equal to its id.
 * See {@link ModProtocolIds} for more information.</p>
 */
public final class RemoteModVersionLookup {
	private RemoteModVersionLookup() {
	}

	/**
	 * Gets protocol version supported by the player.
	 *
	 * @param player     the player
	 * @param protocolId protocol's id
	 * @return Protocol version supported by the player
	 */
	@Nullable
	public static String getRemoteVersion(PacketContextProvider player, Identifier protocolId) {
		return player.getPacketContext().orElse(ModProtocolManager.REMOTE_MOD_VERSIONS_KEY, Map.of()).get(protocolId);
	}

	/**
	 * Gets all protocols supported by the player.
	 *
	 * @param player the player
	 * @return Map of protocols supported by the player
	 */
	public static Collection<Identifier> getRemoteMods(PacketContextProvider player) {
		return Collections.unmodifiableCollection(player.getPacketContext().orElse(ModProtocolManager.REMOTE_MOD_VERSIONS_KEY, Map.of()).keySet());
	}
}
