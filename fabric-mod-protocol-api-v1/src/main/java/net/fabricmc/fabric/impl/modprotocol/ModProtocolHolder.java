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

import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.network.protocol.status.ServerStatus;

public interface ModProtocolHolder {
	static ModProtocolHolder of(ServerStatus input) {
		return (ModProtocolHolder) (Object) input;
	}

	@Nullable
	List<RemoteModProtocol> fabric$getModProtocol();
	void fabric$setModProtocol(List<RemoteModProtocol> protocol);
	boolean fabric$getAlwaysCompatible();
	void fabric$setAlwaysCompatible(boolean value);
}
