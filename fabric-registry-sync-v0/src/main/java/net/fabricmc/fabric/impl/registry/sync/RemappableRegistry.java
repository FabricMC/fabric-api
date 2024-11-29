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

package net.fabricmc.fabric.impl.registry.sync;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

import net.minecraft.util.Identifier;

public interface RemappableRegistry {
	/**
	 * The mode the remapping process should take.
	 */
	enum RemapMode {
		/**
		 * Any differences (local-&gt;remote, remote-&gt;local) are allowed. This should
		 * be used when a side is authoritative (f.e. loading a world on the server).
		 */
		AUTHORITATIVE,
		/**
		 * Entries missing on the remote side are hidden on the local side, while
		 * entries missing on the local side cause an exception. This should be
		 * used when a side is remote (f.e. connecting to a remote server as a
		 * client).
		 */
		REMOTE,
	}

	void remap(Object2IntMap<Identifier> remoteIndexedEntries, RemapMode mode) throws RemapException;

	void unmap() throws RemapException;
}
