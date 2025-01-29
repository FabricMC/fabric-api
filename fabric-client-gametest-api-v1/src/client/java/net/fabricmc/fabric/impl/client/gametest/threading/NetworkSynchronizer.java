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

package net.fabricmc.fabric.impl.client.gametest.threading;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.collect.ConcurrentHashMultiset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.util.Unit;
import net.minecraft.util.thread.ThreadExecutor;

import net.fabricmc.fabric.impl.client.gametest.TestSystemProperties;

/**
 * Ensures packets are always handled by the end of the task loop on the receiving thread.
 *
 * <h1>Implementation notes</h1>
 *
 * <ul>
 *     <li>A packet can be either "in-flight", which is between the time it is sent and the time it is handled on the
 *         Netty thread on the receiving side, or it can be queued for handling on the receiving main thread, which is
 *         between when it is handled on the Netty thread and it is removed from the main thread task queue.
 *         <ul>
 *             <li>Some packets are handled directly on the Netty thread and never enter the second stage. The
 *                 {@code NetworkSynchronizer} is careful not to assume that all packets must be added to the task
 *                 queue.</li>
 *         </ul>
 *     </li>
 *     <li>Once the packets are tracked in this way, the key change is that the client and server now continue running
 *         their task loops until there are no in-flight packets and no packets waiting to be handled in the vanilla
 *         task queues.</li>
 *     <li>Network synchronization can be disabled via a system property, which is useful for mods which directly
 *         interface with the Netty pipeline, which would desynchronize the in-flight packet counter.</li>
 * </ul>
 */
public final class NetworkSynchronizer {
	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-client-gametest-api-v1");

	public static final NetworkSynchronizer CLIENTBOUND = new NetworkSynchronizer();
	public static final NetworkSynchronizer SERVERBOUND = new NetworkSynchronizer();

	private final ThreadLocal<Unit> isNettyThread = new ThreadLocal<>();
	private final AtomicInteger inFlightPackets = new AtomicInteger();
	private final ConcurrentHashMultiset<RunnableBox> mainThreadPacketHandlers = ConcurrentHashMultiset.create();
	private final Lock morePacketsLock = new ReentrantLock();
	private final Condition morePacketsCondition = morePacketsLock.newCondition();
	private final AtomicBoolean invalid = new AtomicBoolean();
	private boolean isRunningNetworkTasks = false;

	public void preSendPacket() {
		if (TestSystemProperties.DISABLE_NETWORK_SYNCHRONIZER) {
			return;
		}

		inFlightPackets.incrementAndGet();
	}

	public void preNettyHandlePacket() {
		if (TestSystemProperties.DISABLE_NETWORK_SYNCHRONIZER) {
			return;
		}

		isNettyThread.set(Unit.INSTANCE);
	}

	public void postNettyHandlePacket() {
		if (TestSystemProperties.DISABLE_NETWORK_SYNCHRONIZER) {
			return;
		}

		int remainingInFlightPackets = inFlightPackets.decrementAndGet();

		if (remainingInFlightPackets < 0) {
			markInvalid();
			return;
		}

		isNettyThread.remove();

		if (remainingInFlightPackets == 0) {
			signalMorePackets();
		}
	}

	public void preTaskAdded(Runnable task) {
		if (TestSystemProperties.DISABLE_NETWORK_SYNCHRONIZER) {
			return;
		}

		if (isNettyThread.get() != null) {
			mainThreadPacketHandlers.add(new RunnableBox(task));
			signalMorePackets();
		}
	}

	public void postTaskRun(Runnable task) {
		if (TestSystemProperties.DISABLE_NETWORK_SYNCHRONIZER) {
			return;
		}

		checkInvalid();
		mainThreadPacketHandlers.remove(new RunnableBox(task));
	}

	public void waitForPacketHandlers(ThreadExecutor<?> executor) {
		if (TestSystemProperties.DISABLE_NETWORK_SYNCHRONIZER) {
			return;
		}

		while (inFlightPackets.get() > 0 || !mainThreadPacketHandlers.isEmpty()) {
			while (inFlightPackets.get() > 0 && mainThreadPacketHandlers.isEmpty()) {
				morePacketsLock.lock();

				try {
					if (!morePacketsCondition.await(10, TimeUnit.SECONDS)) {
						markInvalid();
						checkInvalid();
					}
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				} finally {
					morePacketsLock.unlock();
				}
			}

			isRunningNetworkTasks = true;

			long startTime = System.nanoTime();

			try {
				executor.runTasks(() -> {
					if (System.nanoTime() - startTime > 10_000_000_000L) {
						markInvalid();
						checkInvalid();
					}

					return mainThreadPacketHandlers.isEmpty();
				});
			} finally {
				isRunningNetworkTasks = false;
			}
		}
	}

	public void reset() {
		inFlightPackets.set(0);
		mainThreadPacketHandlers.clear();
		signalMorePackets();
	}

	public boolean isRunningNetworkTasks() {
		return isRunningNetworkTasks;
	}

	private void signalMorePackets() {
		morePacketsLock.lock();
		morePacketsCondition.signal();
		morePacketsLock.unlock();
	}

	private void markInvalid() {
		if (!invalid.getAndSet(true)) {
			LOGGER.error("Detected interfacing with packets at a lower level. Please disable network synchronization by setting the fabric.client.gametest.disableNetworkSynchronizer system property");
			signalMorePackets();
		}
	}

	private void checkInvalid() {
		if (invalid.get()) {
			throw new AssertionError("Network synchronizer in invalid state, see earlier log messages");
		}
	}

	// Wraps a runnable to always use identity hashCode and equals
	private record RunnableBox(Runnable runnable) {
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof RunnableBox(Runnable otherRunnable))) {
				return false;
			}

			return otherRunnable == this.runnable;
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(this.runnable);
		}
	}
}
