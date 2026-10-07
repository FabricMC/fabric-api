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
 * The Mod Protocol API, version 1.
 *
 * <p>Mod Protocol is an additional syncing system allowing mods to easily prevent players from connecting with
 * incompatible or missing mod versions, when they are fully required. It also provides a way of checking the remote
 * version of mods that opted it into this system.</p>
 *
 * <p>The mod protocol can be defined in two ways:
 * <dl>
 *     <dt>fabric.mod.json</dt>
 *     <dd>This is the simplest way to define it. Can be useful when you don't need to change it depending on mods configuration
 *     or external dependencies. It is set within custom field of that file under "fabric:mod_protocol_v1" key.
 *
 *     It can be defined in multiple ways:
 *     <dl>
 *     		<dt>"fabric:mod_protocol_v1": "1.2.x"</dt>
 *     		<dd><p>This will automatically use mods id with "mod" namespace as the protocol identifier. It makes it so the mod is required
 *     		on both client and server to match provided version predicate. The predicate itself uses the same format as dependency declarations.</p>
 *     		</dd>
 *     		<dt>"fabric:mod_protocol_v1": {
 *     		 "id: "custom:id",
 *     		 "name": "Mod Name",
 *     		 "version": "1.2.3",
 *     		 "require": "1.2.x",
 *     		 "require_client": ">=1.2.3",
 *     		 "require_server": "~1.2.0"
 *     		}</dt>
 *      	<dd><p>Full object. All fields are optional, defaulting to existing properties in fabric.mods.json or in case of require to allow all</p>
 *      	<p>"id" is the protocols identifier, which can have any namespace and path, as long as it's valid.
 *      	It is optional and defaults to an ID with "mod" namespace and path equal to mod's id.
 *      	</p>
 *     		<p>"name" is a name displayed if protocol doesn't match. It's optional and by default it uses one from mod's metadata.</p>
 *          <p>"version" is a version displayed if protocol doesn't match. It's optional and by default it uses one from mod's metadata.</p>
 *          <p>"require" controls the version requirement for both client and server, setting this is equal to setting both require_client and require_server"</p>
 *          <p>"require_client" controls the required versions for the clients, defaults to allowing all connections</p>
 *          <p>"require_server" controls the required versions for the servers, defaults to allowing all connections</p>
 *       	</dd>
 *       	<dt>"fabric:mod_protocol_v1": [{
 *       		 "id: "custom:id",
 *       		 "name": "Mod Name",
 *       		 "version": "1.2.3",
 *       		 "require": "1.2.x",
 *       		 "require_client": ">=1.2.3",
 *       		 "require_server": "~1.2.0"
 *      		}]</dt>
 *        	<dd><p>Array of full objects. Allows to define multiple versions of the protocol. The inner objects use the same format as single-full object format,
 *        	with main exception being that fields "id", "name" and "version" aren't defaulted and need to be always set</p></dd>
 *     </dl>
 *     </dd>
 *     <dt>{@link net.fabricmc.fabric.api.modprotocol.v1.ModProtocolRegistry}</dt>
 *     <dd>This is the simplest way to define it. Can be useful when you don't need to change it depending on mods configuration
 *     or external dependencies. It is set within custom field of that file under "fabric:mod_protocol_v1" key.
 *     </dd>
 * </dl>
 */
@NullMarked
@ApiStatus.Experimental
package net.fabricmc.fabric.api.modprotocol.v1;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
