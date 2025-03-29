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

package net.fabricmc.fabric.test.event.lifecycle;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.util.TriState;

import net.minecraft.world.chunk.ChunkStatus;

import org.slf4j.Logger;

import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class ServerChunkLifecycleTests implements ModInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();

	@Override
	public void onInitialize() {
		setupChunkGenerateTest();
		setupChunkLevelTypeChangeTest();
	}

	/**
	 * After creating an SP world and waiting for all nearby chunks to generate (logging to stop),
	 * closing the SP world and opening it again should not log any fresh generation.
	 * Moving to an unexplored area will start logging again.
	 */
	private static void setupChunkGenerateTest() {
		final Object2IntMap<Identifier> generated = new Object2IntOpenHashMap<>();

		ServerTickEvents.END_WORLD_TICK.register(world -> {
			final int count = generated.removeInt(world.getRegistryKey().getValue());

			if (count > 0) {
				LOGGER.info("Loaded {} freshly generated chunks in {} during tick #{}", count, world.getRegistryKey().getValue(), world.getServer().getTicks());
			}
		});

		ServerChunkEvents.CHUNK_GENERATE.register((world, chunk) -> {
			generated.mergeInt(world.getRegistryKey().getValue(), 1, Integer::sum);
		});
	}

	/**
	 * While the world is loading in, this will log a few times.
	 * Once all chunks within (and just outside) simulation distance have loaded in, logging stops.
	 * Moving around within the same chunk (use F3+G) should not log anything.
	 * Moving into another chunk should trigger some logs.
	 */
	private static void setupChunkLevelTypeChangeTest() {
		final Object2ObjectMap<Identifier, Object2IntMap<ChunkLevelType>> worlds = new Object2ObjectOpenHashMap<>();
		ServerChunkEvents.CHUNK_LEVEL_TYPE_CHANGE.register((world, chunkPos, oldLevelType, newLevelType) -> {
			if (Thread.currentThread() != world.getServer().getThread()) LOGGER.error("{} NOT ON SERVER THREAD", chunkPos);
			if (oldLevelType == newLevelType) LOGGER.error("{} {} SAME LEVEL TYPE", chunkPos, oldLevelType);

			if (newLevelType.isAfter(ChunkLevelType.FULL)) {
				if (world.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false) == null) LOGGER.error("{} {} BUT NOT WORLD CHUNK ACCESSIBLE", chunkPos, newLevelType);
			}
			if (newLevelType.isAfter(ChunkLevelType.BLOCK_TICKING)) {
				if (!world.shouldTickBlocksInChunk(chunkPos.toLong())) LOGGER.error("{} {} BUT NOT TICKING BLOCKS", chunkPos, newLevelType);
			}
			if (newLevelType.isAfter(ChunkLevelType.ENTITY_TICKING)) {
				//if (world.getChunkManager().chunkLoadingManager.getLevelManager().shouldTick(chunkPos.toLong()) == TriState.FALSE) LOGGER.error("{} {} BUT NOT TICKING CHUNKS", chunkPos, newLevelType);
				if (!world.shouldTickEntityAt(chunkPos.getCenterAtY(0))) LOGGER.error("{} {} BUT NOT TICKING ENTITIES", chunkPos, newLevelType);
				if (!world.canSpawnEntitiesAt(chunkPos)) LOGGER.error("{} {} BUT CANNOT SPAWN ENTITIES", chunkPos, newLevelType);
			}

			worlds.putIfAbsent(world.getRegistryKey().getValue(), new Object2IntOpenHashMap<>());
			worlds.get(world.getRegistryKey().getValue()).mergeInt(newLevelType, 1, Integer::sum);
		});

		ServerTickEvents.END_WORLD_TICK.register(world -> {
			if (world.getTime() % 20 != 0) return; // limit to 1 per second

			worlds.putIfAbsent(world.getRegistryKey().getValue(), new Object2IntOpenHashMap<>());
			Object2IntMap<ChunkLevelType> levelTypes = worlds.get(world.getRegistryKey().getValue());

			if (!levelTypes.isEmpty()) {
				StringBuilder sb = new StringBuilder(world.getRegistryKey().getValue() + " ");
				levelTypes.forEach((levelType, integer) -> sb.append(levelType).append(": ").append(integer).append(", "));
				LOGGER.info(sb.toString());
				levelTypes.clear();
			}
		});
	}
}
