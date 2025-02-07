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

package net.fabricmc.fabric.api.event.registry;

import java.util.List;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Unmodifiable;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryLoader;

import net.fabricmc.fabric.impl.registry.sync.DynamicRegistriesImpl;

/**
 * Contains methods for registering and accessing dynamic {@linkplain Registry registries}.
 *
 * <h2>Basic usage</h2>
 * Custom dynamic registries can be registered with {@link #register(RegistryKey, Codec)}. These registries will not be
 * <a href="#sync">synced to the client</a>.
 *
 * <p>The list of all dynamic registries, whether from vanilla or mods, can be accessed using
 * {@link #getDynamicRegistries()}.
 *
 * <p>Tags for the entries of a custom registry must be placed in
 * {@code /tags/<registry namespace>/<registry path>/}. For example, the tags for the example
 * registry below would be placed in {@code /tags/my_mod/my_data/}.
 *
 * <h2 id="sync">Synchronization</h2>
 * Dynamic registries are not synchronized to the client by default.
 * To register a <em>synced dynamic registry</em>, you can replace the {@link #register} call
 * with a call to {@link #registerSynced(RegistryKey, Codec, SyncOption...)}.
 *
 * <p>If you want to use a different codec for syncing, e.g. to skip unnecessary data,
 * you can use the overload with two codecs: {@link #registerSynced(RegistryKey, Codec, Codec, SyncOption...)}.
 *
 * <p>Synced dynamic registries can also be prevented from syncing if they have no entries.
 * This is useful for compatibility with clients that might not have your dynamic registry.
 * This behavior can be enabled by passing the {@link SyncOption#SKIP_WHEN_EMPTY} flag to {@code registerSynced}.
 *
 * <h2>Examples</h2>
 * {@snippet :
 * // @link region substring=RegistryKey target=RegistryKey
 * // @link region substring=ofRegistry target="RegistryKey#ofRegistry"
 * // @link region substring=Identifier target="net.minecraft.util.Identifier#Identifier(String, String)"
 * public static final RegistryKey<Registry<MyData>> MY_DATA_KEY = RegistryKey.ofRegistry(Identifier.of("my_mod", "my_data"));
 * // @end @end @end
 *
 * // Option 1: Register a non-synced registry
 * // @link substring=register target="#register":
 * DynamicRegistries.register(MY_DATA_KEY, MyData.CODEC);
 *
 * // Option 2a: Register a synced registry
 * // @link substring=registerSynced target="#registerSynced(RegistryKey, Codec, SyncOption...)":
 * DynamicRegistries.registerSynced(MY_DATA_KEY, MyData.CODEC);
 *
 * // Option 2b: Register a synced registry with a different network codec
 * // @link substring=registerSynced target="#registerSynced(RegistryKey, Codec, Codec, SyncOption...)":
 * DynamicRegistries.registerSynced(MY_DATA_KEY, MyData.CODEC, MyData.NETWORK_CODEC);
 * }
 */
public final class DynamicRegistries {
	private DynamicRegistries() {
	}

	/**
	 * Returns an unmodifiable list of all dynamic registries, including modded ones.
	 *
	 * <p>The list will not reflect any changes caused by later registrations.
	 *
	 * @return an unmodifiable list of all dynamic registries
	 */
	public static @Unmodifiable List<RegistryLoader.Entry<?>> getDynamicRegistries() {
		return DynamicRegistriesImpl.getDynamicRegistries();
	}

	/**
	 * Registers a non-synced dynamic registry.
	 *
	 * <p>The entries of the registry will be loaded from data packs at the file path
	 * {@code data/<entry namespace>/<registry namespace>/<registry path>/<entry path>.json}.
	 *
	 * @param key   the unique key of the registry
	 * @param codec the codec used to load registry entries from data packs
	 * @param <T>   the entry type of the registry
	 */
	public static <T> void register(RegistryKey<? extends Registry<T>> key, Codec<T> codec) {
		DynamicRegistriesImpl.register(key, codec);
	}

	/**
	 * Registers a synced dynamic registry.
	 *
	 * <p>The entries of the registry will be loaded from data packs at the file path
	 * {@code data/<entry namespace>/<registry namespace>/<registry path>/<entry path>.json}.
	 *
	 * <p>The registry will be synced from the server to players' clients using the same codec
	 * that is used to load the registry.
	 *
	 * <p>If the object contained in the registry is complex and contains a lot of data
	 * that is not relevant on the client, another codec for networking can be specified with
	 * {@link #registerSynced(RegistryKey, Codec, Codec, SyncOption...)}.
	 *
	 * @param key     the unique key of the registry
	 * @param codec   the codec used to load registry entries from data packs and the network
	 * @param options options to configure syncing
	 * @param <T>   the entry type of the registry
	 */
	public static <T> void registerSynced(RegistryKey<? extends Registry<T>> key, Codec<T> codec, SyncOption... options) {
		registerSynced(key, codec, codec, options);
	}

	/**
	 * Registers a synced dynamic registry.
	 *
	 * <p>The entries of the registry will be loaded from data packs at the file path
	 * {@code data/<entry namespace>/<registry namespace>/<registry path>/<entry path>.json}
	 *
	 * <p>The registry will be synced from the server to players' clients using the given client codec.
	 * The client codec is also used for writing synced entries on the server, so it cannot depend on
	 * client-only code.
	 *
	 * <p>The client codec must be able to read data encoded by the server codec.
	 * This is because client-sided registry entries are sometimes read directly from a data pack using the client codec.
	 *
	 * @param key          the unique key of the registry
	 * @param serverCodec  the codec used to load registry entries on the server
	 * @param clientCodec  the codec used to load registry entries on the client and sync them from the server
	 * @param options      options to configure syncing
	 * @param <T>          the entry type of the registry
	 */
	public static <T> void registerSynced(RegistryKey<? extends Registry<T>> key, Codec<T> serverCodec, Codec<T> clientCodec, SyncOption... options) {
		DynamicRegistriesImpl.register(key, serverCodec);
		DynamicRegistriesImpl.addSyncedRegistry(key, clientCodec, options);
	}

	/**
	 * Flags for configuring dynamic registry syncing.
	 */
	public enum SyncOption {
		/**
		 * Only synchronizes the dynamic registry if it's not empty.
		 * This is useful for compatibility with vanilla clients,
		 * or other clients that might not have the registry.
		 */
		SKIP_WHEN_EMPTY
	}
}
