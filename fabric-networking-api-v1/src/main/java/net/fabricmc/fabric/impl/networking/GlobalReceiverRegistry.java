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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.network.NetworkPhase;
import net.minecraft.network.NetworkSide;
import net.minecraft.util.Identifier;

public final class GlobalReceiverRegistry<H> {
	public static final int DEFAULT_CHANNEL_NAME_MAX_LENGTH = 128;
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalReceiverRegistry.class);

	private final NetworkSide side;
	private final NetworkPhase phase;
	@Nullable
	private final PayloadTypeRegistryImpl<?> payloadTypeRegistry;

	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Map<Identifier, H> handlers = new HashMap<>();
	private final Set<AbstractNetworkAddon<H>> trackedAddons = new HashSet<>();

	public GlobalReceiverRegistry(NetworkSide side, NetworkPhase phase, @Nullable PayloadTypeRegistryImpl<?> payloadTypeRegistry) {
		this.side = side;
		this.phase = phase;
		this.payloadTypeRegistry = payloadTypeRegistry;

		if (payloadTypeRegistry != null) {
			if (phase != payloadTypeRegistry.getPhase()) {
				throw new IllegalStateException();
			}

			if (side != payloadTypeRegistry.getSide()) {
				throw new IllegalStateException();
			}
		}
	}

	@Nullable
	public H getHandler(Identifier channelName) {
		Lock lock = this.lock.readLock();
		lock.lock();

		try {
			return this.handlers.get(channelName);
		} finally {
			lock.unlock();
		}
	}

	public boolean registerGlobalReceiver(Identifier channelName, H handler) {
		Objects.requireNonNull(channelName, "Channel name cannot be null");
		Objects.requireNonNull(handler, "Channel handler cannot be null");

		if (NetworkingImpl.isReservedCommonChannel(channelName)) {
			throw new IllegalArgumentException(String.format("Cannot register handler for reserved channel with name \"%s\"", channelName));
		}

		assertPayloadType(channelName);

		Lock lock = this.lock.writeLock();
		lock.lock();

		try {
			final boolean replaced = this.handlers.putIfAbsent(channelName, handler) == null;

			if (replaced) {
				this.handleRegistration(channelName, handler);
			}

			return replaced;
		} finally {
			lock.unlock();
		}
	}

	@Nullable
	public H unregisterGlobalReceiver(Identifier channelName) {
		Objects.requireNonNull(channelName, "Channel name cannot be null");

		if (NetworkingImpl.isReservedCommonChannel(channelName)) {
			throw new IllegalArgumentException(String.format("Cannot unregister packet handler for reserved channel with name \"%s\"", channelName));
		}

		Lock lock = this.lock.writeLock();
		lock.lock();

		try {
			final H removed = this.handlers.remove(channelName);

			if (removed != null) {
				this.handleUnregistration(channelName);
			}

			return removed;
		} finally {
			lock.unlock();
		}
	}

	public Map<Identifier, H> getHandlers() {
		Lock lock = this.lock.writeLock();
		lock.lock();

		try {
			return new HashMap<>(this.handlers);
		} finally {
			lock.unlock();
		}
	}

	public Set<Identifier> getChannels() {
		Lock lock = this.lock.readLock();
		lock.lock();

		try {
			return new HashSet<>(this.handlers.keySet());
		} finally {
			lock.unlock();
		}
	}

	// State tracking methods

	public void startSession(AbstractNetworkAddon<H> addon) {
		Lock lock = this.lock.writeLock();
		lock.lock();

		try {
			if (this.trackedAddons.add(addon)) {
				addon.registerChannels(handlers);
			}

			this.logTrackedAddonSize();
		} finally {
			lock.unlock();
		}
	}

	public void endSession(AbstractNetworkAddon<H> addon) {
		Lock lock = this.lock.writeLock();
		lock.lock();

		try {
			this.logTrackedAddonSize();
			this.trackedAddons.remove(addon);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * In practice, trackedAddons should never contain more than the number of players.
	 */
	private void logTrackedAddonSize() {
		if (LOGGER.isTraceEnabled() && this.trackedAddons.size() > 1) {
			LOGGER.trace("{} receiver registry tracks {} addon instances", phase.getId(), trackedAddons.size());
		}
	}

	private void handleRegistration(Identifier channelName, H handler) {
		Lock lock = this.lock.writeLock();
		lock.lock();

		try {
			this.logTrackedAddonSize();

			for (AbstractNetworkAddon<H> addon : this.trackedAddons) {
				addon.registerChannel(channelName, handler);
			}
		} finally {
			lock.unlock();
		}
	}

	private void handleUnregistration(Identifier channelName) {
		Lock lock = this.lock.writeLock();
		lock.lock();

		try {
			this.logTrackedAddonSize();

			for (AbstractNetworkAddon<H> addon : this.trackedAddons) {
				addon.unregisterChannel(channelName);
			}
		} finally {
			lock.unlock();
		}
	}

	public void assertPayloadType(Identifier channelName) {
		if (payloadTypeRegistry == null) {
			return;
		}

		if (payloadTypeRegistry.get(channelName) == null) {
			throw new IllegalArgumentException(String.format("Cannot register handler as no payload type has been registered with name \"%s\" for %s %s", channelName, side, phase));
		}

		if (channelName.toString().length() > DEFAULT_CHANNEL_NAME_MAX_LENGTH) {
			throw new IllegalArgumentException(String.format("Cannot register handler for channel with name \"%s\" as it exceeds the maximum length of 128 characters", channelName));
		}
	}

	public NetworkPhase getPhase() {
		return phase;
	}
}
