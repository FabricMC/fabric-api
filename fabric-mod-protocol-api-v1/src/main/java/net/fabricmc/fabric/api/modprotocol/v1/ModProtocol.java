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

import java.util.Optional;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resources.Identifier;

/**
 * Interface representing registered ModProtocol. Can be used for further lookups.
 */
@ApiStatus.NonExtendable
public interface ModProtocol {
	/**
	 * @return Identifier associated with this Mod Protocol
	 */
	Identifier id();

	/**
	 * @return Display name of this protocol
	 */
	String name();

	/**
	 * @return Version of this protocol
	 */
	String version();

	/**
	 * @return A version predicate for client-side version requirement
	 */
	Optional<String> requireOnClient();

	/**
	 * @return A version predicate for server-side version requirement
	 */
	Optional<String> requireOnServer();
}
