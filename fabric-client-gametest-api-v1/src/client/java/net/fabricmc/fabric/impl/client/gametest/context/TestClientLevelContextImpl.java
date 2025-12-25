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

package net.fabricmc.fabric.impl.client.gametest.context;

import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestClientLevelContext;
import net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl;
import net.fabricmc.fabric.mixin.client.gametest.ClientChunkCacheAccessor;
import net.fabricmc.fabric.mixin.client.gametest.ClientChunkCacheStorageAccessor;
import net.fabricmc.fabric.mixin.client.gametest.ClientLevelAccessor;

public class TestClientLevelContextImpl implements TestClientLevelContext {
	private final ClientGameTestContext context;

	public TestClientLevelContextImpl(ClientGameTestContext context) {
		this.context = context;
	}

	@Override
	public int waitForChunksDownload(int timeout) {
		ThreadingImpl.checkOnGametestThread("waitForChunksDownload");

		return context.waitFor(TestClientLevelContextImpl::areChunksLoaded, timeout);
	}

	@Override
	public int waitForChunksRender(boolean waitForDownload, int timeout) {
		ThreadingImpl.checkOnGametestThread("waitForChunksRender");

		return context.waitFor(client -> (!waitForDownload || areChunksLoaded(client)) && areChunksRendered(client), timeout);
	}

	private static boolean areChunksLoaded(Minecraft client) {
		int renderDistance = client.options.getEffectiveRenderDistance();
		ClientLevel level = Objects.requireNonNull(client.level);
		ClientChunkCache.Storage chunks = ((ClientChunkCacheAccessor) level.getChunkSource()).getStorage();
		ClientChunkCacheStorageAccessor chunksAccessor = (ClientChunkCacheStorageAccessor) (Object) chunks;
		int viewCenterX = chunksAccessor.getViewCenterX();
		int viewCenterZ = chunksAccessor.getViewCenterZ();

		for (int dz = -renderDistance; dz <= renderDistance; dz++) {
			for (int dx = -renderDistance; dx <= renderDistance; dx++) {
				if (level.getChunk(viewCenterX + dx, viewCenterZ + dz, ChunkStatus.FULL, false) == null) {
					return false;
				}
			}
		}

		return true;
	}

	private static boolean areChunksRendered(Minecraft client) {
		ClientLevel level = Objects.requireNonNull(client.level);
		return ((ClientLevelAccessor) level).getLightUpdateQueue().isEmpty() && client.levelRenderer.hasRenderedAllSections();
	}
}
