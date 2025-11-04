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
import net.minecraft.core.BlockPos;

/**
 * Context for a client gametest containing various helpful functions while a client world is open.
 *
 * <p>Functions in this class can only be called on the client gametest thread.
 */
@ApiStatus.NonExtendable
public interface TestClientWorldContext {
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
	Waits for all chunks that will be downloaded from the server to be downloaded. After this, methods such as
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
	 * that are present in the client world will be visible in screenshots. Fails if the chunks haven't been rendered
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
	 * that are present in the client world will be visible in screenshots. Fails if the chunks haven't been rendered
	 * (and optionally downloaded) after {@code timeout} ticks.
	 *
	 * @param waitForDownload Whether to wait for chunks to be downloaded
	 * @param timeout The number of ticks before timing out
	 * @return The number of ticks waited
	 */
	int waitForChunksRender(boolean waitForDownload, int timeout);
}
