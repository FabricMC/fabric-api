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
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.modprotocol.ModProtocolImpl;
import net.fabricmc.fabric.impl.modprotocol.ModProtocolManager;

/**
 * Utility methods allowing to lookup or register new protocols.
 *
 * <p>While lookup can be done anytime, registration is only possible before registries are frozen.</p>
 */
public final class ModProtocolRegistry {
	private ModProtocolRegistry() { }

	/**
	 * Allows to get a locally registered protocol.
	 * @param identifier protocol's id
	 * @return the requested ModProtocol or {@code null} if not found
	 */
	@Nullable
	public static ModProtocol get(Identifier identifier) {
		return ModProtocolManager.LOCAL_MOD_PROTOCOLS_BY_ID.get(identifier);
	}

	/**
	 * @return All registered protocols
	 */
	public static Collection<ModProtocol> getAll() {
		return Collections.unmodifiableCollection(ModProtocolManager.LOCAL_MOD_PROTOCOLS);
	}

	/**
	 * Registers new mod protocol with its own unique settings.
	 *
	 * @param identifier the identifier of protocol
	 * @param name display name in protocol, shown if it's missing
	 * @param version display version of the protocol, shown if it's missing
	 * @param requireOnClient defines a predicate to check version requirement on client
	 * @param requireOnServer defines a predicate to check version requirement on server
	 * @return registered Mod Protocol
	 */
	public static ModProtocol register(Identifier identifier, String name, String version, Optional<String> requireOnClient, Optional<String> requireOnServer) {
		return ModProtocolManager.add(null, new ModProtocolImpl(identifier, name, version, requireOnClient, requireOnServer));
	}

	/**
	 * Registers new mod protocol with its own unique settings.
	 *
	 * @param identifier the identifier of protocol
	 * @param name display name in protocol, shown if it's missing
	 * @param version display version of the protocol, shown if it's missing
	 * @param require defines a predicate to check version requirement
	 * @return registered Mod Protocol
	 */
	public static ModProtocol register(Identifier identifier, String name, String version, Optional<String> require) {
		return ModProtocolManager.add(null, new ModProtocolImpl(identifier, name, version, require, require));
	}

	/**
	 * Allows to customize priority of namespaces when displaying missing protocols.
	 * @param firstNamespace namespace that should display first
	 * @param secondNamespace namespace that should display second
	 * @return true if change occurred, false if it didn't
	 */
	public static boolean addDisplayOrdering(String firstNamespace, String secondNamespace) {
		return ModProtocolManager.registerOrder(firstNamespace, secondNamespace);
	}
}
