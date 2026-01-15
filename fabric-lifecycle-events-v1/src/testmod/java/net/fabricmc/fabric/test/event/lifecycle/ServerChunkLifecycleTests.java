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

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.world.level.ChunkPos;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class ServerChunkLifecycleTests implements ModInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();
	private record FullChunkStatusEvent(FullChunkStatus oldChunkStatus, FullChunkStatus newChunkStatus) { }

	@Override
	public void onInitialize() {
		setupChunkGenerateTest();
		setupFullChunkStatusChangeTest();
	}

	/**
	 * After creating an SP world and waiting for all nearby chunks to generate (logging to stop),
	 * closing the SP world and opening it again should not log any fresh generation.
	 * Moving to an unexplored area will start logging again.
	 */
	private static void setupChunkGenerateTest() {
		final Object2IntMap<Identifier> generatedDeprecated = new Object2IntOpenHashMap<>();
		final Object2IntMap<Identifier> generated = new Object2IntOpenHashMap<>();

		ServerTickEvents.END_LEVEL_TICK.register(level -> {
			Identifier dimensionId = level.dimension().identifier();
			final int countDeprecated = generatedDeprecated.removeInt(dimensionId);
			final int count = generated.removeInt(dimensionId);

			if (count != countDeprecated) {
				throw new AssertionError("count (" + count + ") != countDeprecated (" + countDeprecated + ") in setupChunkGenerateTest for " + dimensionId);
			}

			if (count > 0) {
				LOGGER.info("Loaded {} freshly generated chunks in {} during tick #{}", count, dimensionId, level.getServer().getTickCount());
			}
		});

		ServerChunkEvents.CHUNK_GENERATE.register((level, chunk) -> {
			generatedDeprecated.mergeInt(level.dimension().identifier(), 1, Integer::sum);
		});

		ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated1) -> {
			if (generated1) {
				generated.mergeInt(level.dimension().identifier(), 1, Integer::sum);
			}
		});
	}

	/**
	 * While the world is loading in, this will log a few times.
	 * Once all chunks within (and just outside) simulation distance have loaded in, logging stops.
	 * Moving around within the same chunk (use F3+G) should not log anything.
	 * Moving into another chunk should trigger some logs.
	 */
	private static void setupFullChunkStatusChangeTest() {
		final Object2ObjectMap<Identifier, Object2IntMap<FullChunkStatus>> numOfEventsPerLevel = new Object2ObjectOpenHashMap<>();
		final Object2ObjectMap<Identifier, Long2ObjectOpenHashMap<FullChunkStatusEvent>> eventsPerChunk = new Object2ObjectOpenHashMap<>();

		ServerChunkEvents.FULL_CHUNK_STATUS_CHANGE.register((level, levelChunk, oldChunkStatus, newChunkStatus) -> {
			final Identifier dimensionId = level.dimension().identifier();

			if (!level.getServer().isSameThread()) {
				level.getServer().halt(false); // make sure the server actually "crashes", the throw below will just log the error.
				throw new AssertionError("FULL_CHUNK_STATUS_CHANGE for " + dimensionId + " NOT ON SERVER THREAD: " + oldChunkStatus + "->" + newChunkStatus);
			}

			if (levelChunk == null) {
				throw new AssertionError("FULL_CHUNK_STATUS_CHANGE for " + dimensionId + " NULL LEVEL CHUNK: " + oldChunkStatus + "->" + newChunkStatus);
			}

			final ChunkPos chunkPos = levelChunk.getPos();

			if (Math.abs(oldChunkStatus.ordinal() - newChunkStatus.ordinal()) != 1) { // check if the chunkStatuses are actually sequential, also ensures chunkStatuses are not the same
				throw new AssertionError("FULL_CHUNK_STATUS_CHANGE for " + dimensionId + " " + chunkPos + " NOT SEQUENTIAL: " + oldChunkStatus + "->" + newChunkStatus);
			}

			FullChunkStatusEvent prevEvent = eventsPerChunk.computeIfAbsent(dimensionId, obj -> new Long2ObjectOpenHashMap<>()).computeIfAbsent(chunkPos.pack(), l -> new FullChunkStatusEvent(FullChunkStatus.INACCESSIBLE, FullChunkStatus.INACCESSIBLE));

			if (prevEvent.newChunkStatus() != oldChunkStatus) { // check if newChunkStatus from the previous event == oldChunkStatus for this current event. Catches any out-of-sync firing issues.
				throw new AssertionError("FULL_CHUNK_STATUS_CHANGE for " + dimensionId + " " + chunkPos + " PREVIOUS_EVENT: " + prevEvent.oldChunkStatus() + "->" + prevEvent.newChunkStatus() + " / CURRENT_EVENT: " + oldChunkStatus + "->" + newChunkStatus);
			}

			eventsPerChunk.get(dimensionId).put(chunkPos.pack(), new FullChunkStatusEvent(oldChunkStatus, newChunkStatus));
			numOfEventsPerLevel.computeIfAbsent(dimensionId, obj -> new Object2IntOpenHashMap<>()).mergeInt(newChunkStatus, 1, Integer::sum);
		});

		ServerTickEvents.END_LEVEL_TICK.register(level -> {
			if (level.getGameTime() % 20 == 0) { // limit to 1 per second
				Object2IntMap<FullChunkStatus> chunkStatuses = numOfEventsPerLevel.get(level.dimension().identifier());

				if (chunkStatuses != null && !chunkStatuses.isEmpty()) {
					StringBuilder sb = new StringBuilder(level.dimension().identifier() + " ");
					// Logs the number of full chunk status changes for each FullChunkStatus, only logs the newChunkStatus
					chunkStatuses.forEach((newChunkStatus, numOfEvents) -> sb.append(newChunkStatus).append(": ").append(numOfEvents).append(", "));
					LOGGER.info(sb.toString());
					chunkStatuses.clear();
				}
			}
		});

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			eventsPerChunk.forEach((id, chunks) -> {
				final Object2IntMap<FullChunkStatus> totals = new Object2IntOpenHashMap<>();
				chunks.forEach((chunkPos, fullChunkStatusEvent) -> {
					totals.mergeInt(fullChunkStatusEvent.newChunkStatus(), 1, Integer::sum);
				});

				if (totals.containsKey(FullChunkStatus.FULL) || totals.containsKey(FullChunkStatus.BLOCK_TICKING) || totals.containsKey(FullChunkStatus.ENTITY_TICKING)) {
					StringBuilder sb = new StringBuilder("FULL_CHUNK_STATUS_CHANGE expected all chunks to be INACCESSIBLE for " + id + ", instead got ");
					totals.forEach((chunkStatus, finalTotal) -> {
						sb.append(chunkStatus).append(": ").append(finalTotal);
					});
					LOGGER.error(sb.toString());
				}
			});

			// clear everything otherwise it may trip the test incorrectly when you open another world
			numOfEventsPerLevel.clear();
			eventsPerChunk.clear();
		});
	}
}
