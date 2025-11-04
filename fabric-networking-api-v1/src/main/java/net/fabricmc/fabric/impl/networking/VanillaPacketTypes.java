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

package net.fabricmc.fabric.impl.networking;

import java.util.ArrayList;

import org.jspecify.annotations.Nullable;

import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.game.GameProtocols;

public record VanillaPacketTypes(PacketType<?>[] ids) {
	public static final VanillaPacketTypes PLAY_S2C = of(GameProtocols.CLIENTBOUND_TEMPLATE);
	public static final VanillaPacketTypes PLAY_C2S = of(GameProtocols.SERVERBOUND_TEMPLATE);
	public static final VanillaPacketTypes CONFIGURATION_S2C = of(ConfigurationProtocols.CLIENTBOUND_TEMPLATE);
	public static final VanillaPacketTypes CONFIGURATION_C2S = of(ConfigurationProtocols.SERVERBOUND_TEMPLATE);

	@Nullable
	public PacketType<?> get(int id) {
		return id > 0 && id < this.ids.length ? this.ids[id] : null;
	}

	private static VanillaPacketTypes of(ProtocolInfo.DetailsProvider factory) {
		var list = new ArrayList<PacketType<?>>();

		// See NetworkStateBuilder#createState for reference.
		factory.details().listPackets((type, i) -> list.add(type));

		return new VanillaPacketTypes(list.toArray(PacketType[]::new));
	}

	public static VanillaPacketTypes get(ProtocolInfo<?> state) {
		return switch (state.id()) {
		case CONFIGURATION -> state.flow() == PacketFlow.CLIENTBOUND ? CONFIGURATION_S2C : CONFIGURATION_C2S;
		case PLAY -> state.flow() == PacketFlow.CLIENTBOUND ? PLAY_S2C : PLAY_C2S;
		default -> throw new IllegalArgumentException("Not implemented for " + state.id() + "!");
		};
	}
}
