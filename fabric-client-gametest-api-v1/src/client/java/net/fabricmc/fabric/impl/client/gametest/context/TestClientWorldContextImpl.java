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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.chunk.ChunkStatus;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestClientWorldContext;
import net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl;
import net.fabricmc.fabric.mixin.client.gametest.ClientChunkManagerAccessor;
import net.fabricmc.fabric.mixin.client.gametest.ClientChunkManagerClientChunkMapAccessor;
import net.fabricmc.fabric.mixin.client.gametest.ClientWorldAccessor;

public class TestClientWorldContextImpl implements TestClientWorldContext {
	private final ClientGameTestContext context;

	public TestClientWorldContextImpl(ClientGameTestContext context) {
		this.context = context;
	}

	@Override
	public int waitForChunksDownload(int timeout) {
		ThreadingImpl.checkOnGametestThread("waitForChunksDownload");

		return context.waitFor(TestClientWorldContextImpl::areChunksLoaded, timeout);
	}

	@Override
	public int waitForChunksRender(boolean waitForDownload, int timeout) {
		ThreadingImpl.checkOnGametestThread("waitForChunksRender");

		return context.waitFor(client -> (!waitForDownload || areChunksLoaded(client)) && areChunksRendered(client), timeout);
	}

	private static boolean areChunksLoaded(MinecraftClient client) {
		int viewDistance = client.options.getClampedViewDistance();
		ClientWorld world = Objects.requireNonNull(client.world);
		ClientChunkManager.ClientChunkMap chunks = ((ClientChunkManagerAccessor) world.getChunkManager()).getChunks();
		ClientChunkManagerClientChunkMapAccessor chunksAccessor = (ClientChunkManagerClientChunkMapAccessor) (Object) chunks;
		int centerChunkX = chunksAccessor.getCenterChunkX();
		int centerChunkZ = chunksAccessor.getCenterChunkZ();

		for (int dz = -viewDistance; dz <= viewDistance; dz++) {
			for (int dx = -viewDistance; dx <= viewDistance; dx++) {
				if (world.getChunk(centerChunkX + dx, centerChunkZ + dz, ChunkStatus.FULL, false) == null) {
					return false;
				}
			}
		}

		return true;
	}

	private static boolean areChunksRendered(MinecraftClient client) {
		ClientWorld world = Objects.requireNonNull(client.world);
		return ((ClientWorldAccessor) world).getChunkUpdaters().isEmpty() && client.worldRenderer.isTerrainRenderComplete();
	}
}
