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

package net.fabricmc.fabric.impl.modprotocol;

import java.util.Optional;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.modprotocol.v1.ModProtocol;

public record ModProtocolImpl(Identifier id, String name, String version,
							Optional<String> requireOnClient,
							Optional<String> requireOnServer) implements ModProtocol {
	public boolean hasAnyRequirement() {
		return this.requireOnClient.isPresent() || this.requireOnServer.isPresent();
	}

	public RemoteModProtocol asClientbound() {
		return new RemoteModProtocol(this.id, this.name, this.version, this.requireOnClient);
	}

	public RemoteModProtocol asServerbound() {
		return new RemoteModProtocol(this.id, this.name, this.version, this.requireOnServer);
	}
}
