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

package net.fabricmc.fabric.impl.networking.context;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.mojang.authlib.GameProfile;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;

import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public final class PacketContextImpl implements PacketContext {
	public static final ScopedValue<PacketContext> VALUE = ScopedValue.newInstance();
	public static final Key<MinecraftServer> SERVER_INSTANCE = fabricKey("server_instance");
	public static final Key<RegistryAccess> REGISTRY_ACCESS = fabricKey("registry_access");
	public static final Key<GameProfile> GAME_PROFILE = fabricKey("game_profile");
	public static final Key<@NonNull Connection> CONNECTION = fabricKey("connection");

	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Map<Key<?>, Object> contextMap = new IdentityHashMap<>();

	public PacketContextImpl(Connection connection) {
		this.contextMap.put(CONNECTION, connection);
	}

	@Override
	public @Nullable <T> T get(ReadKey<T> key) {
		this.lock.readLock().lock();

		try {
			//noinspection unchecked,SuspiciousMethodCalls
			return (T) this.contextMap.get(key);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	@Override
	public <T> void set(Key<T> key, T value) {
		this.lock.writeLock().lock();

		if (value == null) {
			this.contextMap.remove(key);
		} else {
			this.contextMap.put(key, value);
		}

		this.lock.writeLock().unlock();
	}

	private static <T> Key<T> fabricKey(String path) {
		return PacketContext.key(Identifier.fromNamespaceAndPath("fabric", path));
	}

	public static final class KeyImpl<T> implements PacketContext.Key<T> {
		private final Identifier key;

		public KeyImpl(Identifier key) {
			this.key = key;
		}

		@Override
		public String toString() {
			return "PacketContext.Key[" + this.key + "]";
		}

		public Identifier key() {
			return this.key;
		}
	}
}
