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
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.slf4j.Logger;

import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class ServerChunkLifecycleTests implements ModInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();
	private record ChunkLevelTypeEvent(ChunkLevelType oldLevelType, ChunkLevelType newLevelType) { }

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
		final Object2ObjectMap<Identifier, Object2IntMap<ChunkLevelType>> worldsChunkLevelEvents = new Object2ObjectOpenHashMap<>();
		final Object2ObjectMap<Identifier, Long2ObjectOpenHashMap<ChunkLevelTypeEvent>> worldsChunkLevelTypeTracker = new Object2ObjectOpenHashMap<>();

		ServerChunkEvents.CHUNK_LEVEL_TYPE_CHANGE.register((world, worldChunk, oldLevelType, newLevelType) -> {
			final Identifier worldKey = world.getRegistryKey().getValue();

			if (!world.getServer().isOnThread()) {
				world.getServer().stop(false); // make sure the server actually "crashes", the throw below will just log the error.
				throw new AssertionError("CHUNK_LEVEL_TYPE_CHANGE for " + worldKey + " NOT ON SERVER THREAD: " + oldLevelType + "->" + newLevelType);
			}

			if (worldChunk == null) {
				throw new AssertionError("CHUNK_LEVEL_TYPE_CHANGE for " + worldKey + " NULL WORLD CHUNK: " + oldLevelType + "->" + newLevelType);
			}

			final ChunkPos chunkPos = worldChunk.getPos();

			if (Math.abs(oldLevelType.ordinal() - newLevelType.ordinal()) != 1) { // check if the levelTypes are actually sequential, also ensures levelTypes are not the same
				throw new AssertionError("CHUNK_LEVEL_TYPE_CHANGE for " + worldKey + " " + chunkPos + " NOT SEQUENTIAL: " + oldLevelType + "->" + newLevelType);
			}

			ChunkLevelTypeEvent prevEvent = worldsChunkLevelTypeTracker.computeIfAbsent(worldKey, obj -> new Long2ObjectOpenHashMap<>()).computeIfAbsent(chunkPos.toLong(), l -> new ChunkLevelTypeEvent(ChunkLevelType.INACCESSIBLE, ChunkLevelType.INACCESSIBLE));

			if (prevEvent.newLevelType() != oldLevelType) { // check if newLevelType from the previous event == oldLevelType for this current event. Catches any out-of-sync firing issues.
				throw new AssertionError("CHUNK_LEVEL_TYPE_CHANGE for " + worldKey + " " + chunkPos + " PREVIOUS_EVENT: " + prevEvent.oldLevelType() + "->" + prevEvent.newLevelType() + " / CURRENT_EVENT: " + oldLevelType + "->" + newLevelType);
			}

			worldsChunkLevelTypeTracker.get(worldKey).put(chunkPos.toLong(), new ChunkLevelTypeEvent(oldLevelType, newLevelType));
			worldsChunkLevelEvents.computeIfAbsent(worldKey, obj -> new Object2IntOpenHashMap<>()).mergeInt(newLevelType, 1, Integer::sum);
		});

		ServerTickEvents.END_WORLD_TICK.register(world -> {
			if (world.getTime() % 20 == 0) { // limit to 1 per second
				Object2IntMap<ChunkLevelType> levelTypes = worldsChunkLevelEvents.get(world.getRegistryKey().getValue());

				if (levelTypes != null && !levelTypes.isEmpty()) {
					StringBuilder sb = new StringBuilder(world.getRegistryKey().getValue() + " ");
					// Logs the number of level type changes for each ChunkLevelType, only logs the newLevelType
					levelTypes.forEach((newLevelType, numOfEvents) -> sb.append(newLevelType).append(": ").append(numOfEvents).append(", "));
					LOGGER.info(sb.toString());
					levelTypes.clear();
				}
			}
		});

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			worldsChunkLevelTypeTracker.forEach((id, chunks) -> {
				final Object2IntMap<ChunkLevelType> totals = new Object2IntOpenHashMap<>();
				chunks.forEach((chunkPos, chunkLevelTypeEvent) -> {
					totals.mergeInt(chunkLevelTypeEvent.newLevelType(), 1, Integer::sum);
				});

				if (totals.containsKey(ChunkLevelType.FULL) || totals.containsKey(ChunkLevelType.BLOCK_TICKING) || totals.containsKey(ChunkLevelType.ENTITY_TICKING)) {
					StringBuilder sb = new StringBuilder("CHUNK_LEVEL_TYPE_CHANGE expected all chunks to be INACCESSIBLE for " + id + ", instead got ");
					totals.forEach((chunkLevelType, finalTotal) -> {
						sb.append(chunkLevelType).append(": ").append(finalTotal);
					});
					LOGGER.error(sb.toString());
				}
			});

			// clear everything otherwise it may trip the test incorrectly when you open another world
			worldsChunkLevelEvents.clear();
			worldsChunkLevelTypeTracker.clear();
		});
	}
}
