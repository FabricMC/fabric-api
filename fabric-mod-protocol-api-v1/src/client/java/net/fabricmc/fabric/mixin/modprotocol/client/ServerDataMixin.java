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

package net.fabricmc.fabric.mixin.modprotocol.client;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.multiplayer.ServerData;

import net.fabricmc.fabric.impl.modprotocol.ModProtocolManager;
import net.fabricmc.fabric.impl.modprotocol.RemoteModProtocol;
import net.fabricmc.fabric.impl.modprotocol.client.FabricServerData;

@Mixin(ServerData.class)
public class ServerDataMixin implements FabricServerData {
	@Unique
	private ModProtocolManager.@Nullable ValidationResult validationResult;
	@Unique
	private List<RemoteModProtocol> modProtocols = List.of();

	public ServerDataMixin(ModProtocolManager.@Nullable ValidationResult validationResult) {
		this.validationResult = validationResult;
	}

	@Override
	public List<RemoteModProtocol> fabric$getModProtocols() {
		return this.modProtocols;
	}

	@Override
	public ModProtocolManager.@Nullable ValidationResult fabric$getValidationResult() {
		return this.validationResult;
	}

	@Override
	public void fabric$setModProtocolsState(List<RemoteModProtocol> protocols, ModProtocolManager.@Nullable ValidationResult validationResult) {
		this.modProtocols = protocols;
		this.validationResult = validationResult;
	}
}
