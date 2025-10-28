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

import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.state.ConfigurationStates;
import net.minecraft.network.state.NetworkState;
import net.minecraft.network.state.PlayStateFactories;

public record VanillaPacketTypes(PacketType<?>[] ids) {
	public static final VanillaPacketTypes PLAY_S2C = of(PlayStateFactories.S2C);
	public static final VanillaPacketTypes PLAY_C2S = of(PlayStateFactories.C2S);
	public static final VanillaPacketTypes CONFIGURATION_S2C = of(ConfigurationStates.S2C_FACTORY);
	public static final VanillaPacketTypes CONFIGURATION_C2S = of(ConfigurationStates.C2S_FACTORY);

	@Nullable
	public PacketType<?> get(int id) {
		return id > 0 && id < this.ids.length ? this.ids[id] : null;
	}

	private static VanillaPacketTypes of(NetworkState.Factory factory) {
		var list = new ArrayList<PacketType<?>>();

		// See NetworkStateBuilder#createState for reference.
		factory.buildUnbound().forEachPacketType((type, i) -> list.add(type));

		return new VanillaPacketTypes(list.toArray(PacketType[]::new));
	}

	public static VanillaPacketTypes get(NetworkState<?> state) {
		return switch (state.id()) {
		case CONFIGURATION -> state.side() == NetworkSide.CLIENTBOUND ? CONFIGURATION_S2C : CONFIGURATION_C2S;
		case PLAY -> state.side() == NetworkSide.CLIENTBOUND ? PLAY_S2C : PLAY_C2S;
		default -> throw new IllegalArgumentException("Not implemented for " + state.id() + "!");
		};
	}
}
