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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.Identifier;

/**
 * A network addon is a simple abstraction to hold information about a player's registered channels.
 *
 * @param <H> the channel handler type
 */
public abstract class AbstractNetworkAddon<H> {
	protected final GlobalReceiverRegistry<H> receiver;
	protected final Logger logger;
	// A lock is used due to possible access on netty's event loops and game thread at same times such as during dynamic registration
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	// Sync map should be fine as there is little read write competition
	// All access to this map is guarded by the lock
	private final Map<Identifier, H> handlers = new HashMap<>();
	private final AtomicBoolean disconnected = new AtomicBoolean(); // blocks redundant disconnect notifications

	protected AbstractNetworkAddon(GlobalReceiverRegistry<H> receiver, String description) {
		this.receiver = receiver;
		this.logger = LoggerFactory.getLogger(description);
	}

	public final void lateInit() {
		this.receiver.startSession(this);
		invokeInitEvent();
	}

	protected abstract void invokeInitEvent();

	public final void endSession() {
		this.receiver.endSession(this);
	}

	@Nullable
	public H getHandler(Identifier channel) {
		Lock lock = this.lock.readLock();
		lock.lock();

		try {
			return this.handlers.get(channel);
		} finally {
			lock.unlock();
		}
	}

	private void assertNotReserved(Identifier channel) {
		if (this.isReservedChannel(channel)) {
			throw new IllegalArgumentException(String.format("Cannot (un)register handler for reserved channel with name \"%s\"", channel));
		}
	}

	public void registerChannels(Map<Identifier, H> map) {
		Lock lock = this.lock.writeLock();
		lock.lock();

		try {
			for (Map.Entry<Identifier, H> entry : map.entrySet()) {
				assertNotReserved(entry.getKey());

				boolean unique = this.handlers.putIfAbsent(entry.getKey(), entry.getValue()) == null;
				if (unique) handleRegistration(entry.getKey());
			}
		} finally {
			lock.unlock();
		}
	}

	public boolean registerChannel(Identifier channelName, H handler) {
		Objects.requireNonNull(channelName, "Channel name cannot be null");
		Objects.requireNonNull(handler, "Packet handler cannot be null");
		assertNotReserved(channelName);

		receiver.assertPayloadType(channelName);

		Lock lock = this.lock.writeLock();
		lock.lock();

		try {
			final boolean replaced = this.handlers.putIfAbsent(channelName, handler) == null;

			if (replaced) {
				this.handleRegistration(channelName);
			}

			return replaced;
		} finally {
			lock.unlock();
		}
	}

	public H unregisterChannel(Identifier channelName) {
		Objects.requireNonNull(channelName, "Channel name cannot be null");
		assertNotReserved(channelName);

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

	public Set<Identifier> getReceivableChannels() {
		Lock lock = this.lock.readLock();
		lock.lock();

		try {
			return new HashSet<>(this.handlers.keySet());
		} finally {
			lock.unlock();
		}
	}

	protected abstract void handleRegistration(Identifier channelName);

	protected abstract void handleUnregistration(Identifier channelName);

	public final void handleDisconnect() {
		if (disconnected.compareAndSet(false, true)) {
			invokeDisconnectEvent();
			endSession();
		}
	}

	protected abstract void invokeDisconnectEvent();

	/**
	 * Checks if a channel is considered a "reserved" channel.
	 * A reserved channel such as "minecraft:(un)register" has special handling and should not have any channel handlers registered for it.
	 *
	 * @param channelName the channel name
	 * @return whether the channel is reserved
	 */
	protected abstract boolean isReservedChannel(Identifier channelName);
}
