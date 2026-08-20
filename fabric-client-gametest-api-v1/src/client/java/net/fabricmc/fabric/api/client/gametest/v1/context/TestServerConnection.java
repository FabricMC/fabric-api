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

package net.fabricmc.fabric.api.client.gametest.v1.context;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.SharedConstants;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LinearInterpolationHandler;

/**
 * Context for a client gametest containing various helpful functions while a connection to a server is open.
 *
 * <p>Unless otherwise specified, methods in this class can only be called on the client gametest thread.
 */
@ApiStatus.NonExtendable
public interface TestServerConnection {
	/**
	 * The default timeout in ticks to wait for chunks to load/render (1 minute).
	 */
	int DEFAULT_CHUNK_LOAD_TIMEOUT = SharedConstants.TICKS_PER_MINUTE;

	/**
	 * Waits for all chunks that will be downloaded from the server to be downloaded. Fails if the chunks haven't been
	 * downloaded after {@link #DEFAULT_CHUNK_LOAD_TIMEOUT} ticks. See {@link #waitForChunksDownload(int)} for details.
	 *
	 * @return The number of ticks waited
	 */
	default int waitForChunksDownload() {
		return waitForChunksDownload(DEFAULT_CHUNK_LOAD_TIMEOUT);
	}

	/**
	 * Waits for all chunks that will be downloaded from the server to be downloaded. After this, methods such as
	 * {@link ClientLevel#getChunk(int, int)} and {@link ClientLevel#getBlockState(BlockPos)} will return the expected
	 * value. However, the chunks may not yet be rendered and may not appear in screenshots, if you need this, use
	 * {@link #waitForChunksRender(int)} instead. Fails if the chunks haven't been downloaded after {@code timeout}
	 * ticks.
	 *
	 * @param timeout The number of ticks before timing out
	 * @return The number of ticks waited
	 */
	int waitForChunksDownload(int timeout);

	/**
	 * Waits for all chunks to be downloaded and rendered. After this, all chunks that will ever be visible are visible
	 * in screenshots. Fails if the chunks haven't been downloaded and rendered after
	 * {@link #DEFAULT_CHUNK_LOAD_TIMEOUT} ticks.
	 *
	 * @return The number of ticks waited
	 */
	default int waitForChunksRender() {
		return waitForChunksRender(DEFAULT_CHUNK_LOAD_TIMEOUT);
	}

	/**
	 * Waits for all chunks to be downloaded and rendered. After this, all chunks that will ever be visible are visible
	 * in screenshots. Fails if the chunks haven't been downloaded and rendered after {@code timeout} ticks.
	 *
	 * @param timeout The number of ticks before timing out
	 * @return The number of ticks waited
	 */
	default int waitForChunksRender(int timeout) {
		return waitForChunksRender(true, timeout);
	}

	/**
	 * Waits for all chunks to be rendered, optionally waiting for chunks to be downloaded first. After this, all chunks
	 * that are present in the client level will be visible in screenshots. Fails if the chunks haven't been rendered
	 * (and optionally downloaded) after {@link #DEFAULT_CHUNK_LOAD_TIMEOUT} ticks.
	 *
	 * @param waitForDownload Whether to wait for chunks to be downloaded
	 * @return The number of ticks waited
	 */
	default int waitForChunksRender(boolean waitForDownload) {
		return waitForChunksRender(waitForDownload, DEFAULT_CHUNK_LOAD_TIMEOUT);
	}

	/**
	 * Waits for all chunks to be rendered, optionally waiting for chunks to be downloaded first. After this, all chunks
	 * that are present in the client level will be visible in screenshots. Fails if the chunks haven't been rendered
	 * (and optionally downloaded) after {@code timeout} ticks.
	 *
	 * @param waitForDownload Whether to wait for chunks to be downloaded
	 * @param timeout The number of ticks before timing out
	 * @return The number of ticks waited
	 */
	int waitForChunksRender(boolean waitForDownload, int timeout);

	/**
	 * Waits for all packets that have already been sent on the server to be received and processed by the client.
	 *
	 * <p>Note that the server batches some updates, sending them later in the tick, so in some cases a wait may need
	 * to be added before calling this method to ensure the packets are sent. Notable examples include:
	 *
	 * <ul>
	 *     <li>Block changes, which require a call to {@link ClientGameTestContext#waitTick()} before calling this method.</li>
	 *     <li>Entity updates, which are batched less frequently. You can call {@link #waitForClientboundEntityUpdates} instead
	 *         of this method to handle this case.</li>
	 * </ul>
	 *
	 * <p>It may be tempting to call {@link ClientGameTestContext#waitTick()} instead of this method. This often appears to work,
	 * especially in singleplayer, since packets can often take less than a tick to arrive. However it is not 100% reliable and
	 * will produce flaky tests. For a similar reason, forgetting to call {@link ClientGameTestContext#waitTick()} before this
	 * method for a block change often works anyway, since the packets sent by the server batching block updates can still arrive
	 * before control is returned to the client gametest thread, however this is not guaranteed.
	 */
	void waitForClientboundPackets();

	/**
	 * Waits for all packets that have already been sent on the client to be received and processed by the server.
	 */
	void waitForServerboundPackets();

	/**
	 * Waits for updates to entities of the specified types on the server to be sent, and received and processed by
	 * the client. This waits the maximum of all the update intervals of the specified entity types, then waits for
	 * the packets to be received.
	 *
	 * <p>Some entities interpolate on the client when they are moved, rather than snapping immediately to the right
	 * position. If you encounter issues with this, you may need to wait for the interpolation to finish after calling
	 * this method. Living entities interpolate for {@link LinearInterpolationHandler#DEFAULT_INTERPOLATION_STEPS} ticks.
	 *
	 * @param entityType The entity type to wait for
	 * @param moreEntityTypes Additional entity types to wait for
	 */
	void waitForClientboundEntityUpdates(EntityType<?> entityType, EntityType<?>... moreEntityTypes);

	/**
	 * Gets the client player.
	 *
	 * <p>This method can only be called from the render (client) thread.
	 *
	 * @return The client player
	 */
	LocalPlayer getClientPlayer();

	/**
	 * Gets the server player corresponding to the connected client.
	 *
	 * <p>This method can only be called from the server thread.
	 *
	 * @return The server player
	 */
	ServerPlayer getServerPlayer();

	/**
	 * Gets the client level.
	 *
	 * <p>This method can only be called from the render (client) thread.
	 *
	 * @return The client level
	 */
	ClientLevel getClientLevel();

	/**
	 * Gets the server level of the same dimension as the client level.
	 *
	 * <p>This method can only be called from the server thread.
	 *
	 * @return The server level
	 */
	ServerLevel getServerLevel();
}
