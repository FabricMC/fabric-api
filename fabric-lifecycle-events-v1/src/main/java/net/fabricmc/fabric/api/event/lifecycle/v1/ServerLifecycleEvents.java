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

package net.fabricmc.fabric.api.event.lifecycle.v1;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.CloseableResourceManager;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class ServerLifecycleEvents {
	private ServerLifecycleEvents() {
	}

	/// Called when a Minecraft server is starting.
	///
	/// This occurs before the [player list][net.minecraft.server.players.PlayerList] and any levels are loaded.
	public static final Event<ServerStarting> SERVER_STARTING = EventFactory.createArrayBacked(ServerStarting.class, callbacks -> server -> {
		for (ServerStarting callback : callbacks) {
			callback.onServerStarting(server);
		}
	});

	/// Called when a Minecraft server has started and is about to tick for the first time.
	///
	/// At this stage, all levels are live.
	public static final Event<ServerStarted> SERVER_STARTED = EventFactory.createArrayBacked(ServerStarted.class, (callbacks) -> (server) -> {
		for (ServerStarted callback : callbacks) {
			callback.onServerStarted(server);
		}
	});

	/// Called when a Minecraft server has started shutting down.
	/// This occurs before the server's network channel is closed and before any players are disconnected.
	///
	/// For example, an integrated server will begin stopping, but its client may continue to run.
	///
	/// All levels are still present and can be modified.
	public static final Event<ServerStopping> SERVER_STOPPING = EventFactory.createArrayBacked(ServerStopping.class, (callbacks) -> (server) -> {
		for (ServerStopping callback : callbacks) {
			callback.onServerStopping(server);
		}
	});

	/// Called when a Minecraft server has stopped.
	/// All levels have been closed and all (block)entities and players have been unloaded.
	///
	/// For example, an [integrated server][net.fabricmc.api.EnvType#CLIENT] will begin stopping, but its client may continue to run.
	/// Meanwhile, for a [dedicated server][net.fabricmc.api.EnvType#SERVER], this will be the last event called.
	public static final Event<ServerStopped> SERVER_STOPPED = EventFactory.createArrayBacked(ServerStopped.class, callbacks -> server -> {
		for (ServerStopped callback : callbacks) {
			callback.onServerStopped(server);
		}
	});

	/// Called when a Minecraft server is about to send tag and recipe data to a player.
	/// @see SyncDataPackContents
	public static final Event<SyncDataPackContents> SYNC_DATA_PACK_CONTENTS = EventFactory.createArrayBacked(SyncDataPackContents.class, callbacks -> (player, joined) -> {
		for (SyncDataPackContents callback : callbacks) {
			callback.onSyncDataPackContents(player, joined);
		}
	});

	/// Called before a Minecraft server reloads data packs.
	public static final Event<StartDataPackReload> START_DATA_PACK_RELOAD = EventFactory.createArrayBacked(StartDataPackReload.class, callbacks -> (server, resourceManager) -> {
		for (StartDataPackReload callback : callbacks) {
			callback.startDataPackReload(server, resourceManager);
		}
	});

	/// Called after a Minecraft server has reloaded data packs.
	///
	/// If reloading data packs was unsuccessful, the current data packs will be kept.
	public static final Event<EndDataPackReload> END_DATA_PACK_RELOAD = EventFactory.createArrayBacked(EndDataPackReload.class, callbacks -> (server, resourceManager, success) -> {
		for (EndDataPackReload callback : callbacks) {
			callback.endDataPackReload(server, resourceManager, success);
		}
	});

	/// Called before a Minecraft server begins saving data.
	public static final Event<BeforeSave> BEFORE_SAVE = EventFactory.createArrayBacked(BeforeSave.class, callbacks -> (server, flush, force) -> {
		for (BeforeSave callback : callbacks) {
			callback.onBeforeSave(server, flush, force);
		}
	});

	/// Called after a Minecraft server finishes saving data.
	public static final Event<AfterSave> AFTER_SAVE = EventFactory.createArrayBacked(AfterSave.class, callbacks -> (server, flush, force) -> {
		for (AfterSave callback : callbacks) {
			callback.onAfterSave(server, flush, force);
		}
	});

	@FunctionalInterface
	public interface ServerStarting {
		void onServerStarting(MinecraftServer server);
	}

	@FunctionalInterface
	public interface ServerStarted {
		void onServerStarted(MinecraftServer server);
	}

	@FunctionalInterface
	public interface ServerStopping {
		void onServerStopping(MinecraftServer server);
	}

	@FunctionalInterface
	public interface ServerStopped {
		void onServerStopped(MinecraftServer server);
	}

	@FunctionalInterface
	public interface SyncDataPackContents {
		/// Called right before tags and recipes are sent to a player,
		/// either because the player joined, or because the server reloaded resources.
		/// The [server resource manager][MinecraftServer#getResourceManager()] is up-to-date when this is called.
		///
		/// For example, this event can be used to sync data loaded with custom resource reloaders.
		///
		/// @param player Player to which the data is being sent.
		/// @param joined True if the player is joining the server, false if the server finished a successful resource reload.
		void onSyncDataPackContents(ServerPlayer player, boolean joined);
	}

	@FunctionalInterface
	public interface StartDataPackReload {
		void startDataPackReload(MinecraftServer server, CloseableResourceManager resourceManager);
	}

	@FunctionalInterface
	public interface EndDataPackReload {
		/// Called after data packs on a Minecraft server have been reloaded.
		///
		/// If the reload was not successful, the old data packs will be kept.
		///
		/// @param server the server
		/// @param resourceManager the resource manager
		/// @param success if the reload was successful
		void endDataPackReload(MinecraftServer server, CloseableResourceManager resourceManager, boolean success);
	}

	@FunctionalInterface
	public interface BeforeSave {
		/// Called before a Minecraft server begins saving data.
		///
		/// @param server the server
		/// @param flush is true when all chunks are being written to disk, server will likely freeze during this time
		/// @param force whether servers that have save-off set should save
		void onBeforeSave(MinecraftServer server, boolean flush, boolean force);
	}

	@FunctionalInterface
	public interface AfterSave {
		/// Called before a Minecraft server begins saving data.
		///
		/// @param server the server
		/// @param flush is true when all chunks are being written to disk, server will likely freeze during this time
		/// @param force whether servers that have save-off set should save
		void onAfterSave(MinecraftServer server, boolean flush, boolean force);
	}
}
