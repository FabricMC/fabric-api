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

package net.fabricmc.fabric.api.permission.v1;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import net.minecraft.server.MinecraftServer;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * These events used for handling permission resolution within PermissionContext system.
 * Implemented callbacks for these event should be thread safe, as permission methods can be called from any active thread.
 * Additionally, the execution should be reasonably fast for non-player and online player cases.
 * Offline player checks are allowed to be slower.
 *
 * <p>When implementing a permission handler, only {@link PermissionEvents#ON_REQUEST} needs to be implemented.
 *
 * <p>To check for permissions, you should use dedicated methods from {@link PermissionContextOwner} interface
 * and it's implementations over invoking this event.
 */
public final class PermissionEvents {
	private PermissionEvents() {
	}

	/**
	 * Event used for handling permission resolution.
	 *
	 * <p>This event is invoked by methods provided by {@link PermissionContextOwner}.
	 */
	public static final Event<OnRequest> ON_REQUEST = EventFactory.createArrayBacked(OnRequest.class, arr -> new OnRequest() {
		@Override
		public @Nullable <T> T handlePermissionRequest(PermissionContext context, PermissionNode<T> permission) {
			for (OnRequest callback : arr) {
				T out = callback.handlePermissionRequest(context, permission);

				if (out != null) {
					return out;
				}
			}

			return null;
		}
	});

	/**
	 * Event for preparing for offline player checks.
	 *
	 * <p>This event is invoked by {@link PermissionContext#offlinePlayer}.
	 */
	public static final Event<PrepareOfflinePlayer> PREPARE_OFFLINE_PLAYER = EventFactory.createArrayBacked(PrepareOfflinePlayer.class,
			(_, _) -> CompletableFuture.completedFuture(null), arr -> (context, server) -> {
				var list = new ArrayList<CompletableFuture<@Nullable Consumer<MutablePermissionContext>>>();

				for (PrepareOfflinePlayer callback : arr) {
					list.add(callback.prepareOfflinePlayer(context, server));
				}

				return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new)).thenApply(_ -> {
					return mutableContext -> {
						for (CompletableFuture<@Nullable Consumer<MutablePermissionContext>> future : list) {
							Consumer<MutablePermissionContext> consumer = future.getNow(null);

							if (consumer != null) {
								consumer.accept(mutableContext);
							}
						}
					};
				});
			});

	@FunctionalInterface
	public interface OnRequest {
		/**
		 * Main permission checking, it can execute on any thread.
		 *
		 * @param context        context to check for.
		 * @param permission     a permission node representing a permission.
		 * @param <T>            type of permission.
		 * @return value of type T if present, null to pass to the next handler.
		 */
		@Nullable
		<T> T handlePermissionRequest(PermissionContext context, PermissionNode<T> permission);
	}

	@FunctionalInterface
	public interface PrepareOfflinePlayer {
		/**
		 * A callback run before providing a {@link PermissionContext} for offline player checks.
		 * Should be used to preload the relevant permission data if needed.
		 *
		 * @param context context to load.
		 * @param server server for which this player is resolved against.
		 * @return a completable future indicating that permission context is ready to be checked against, with optional callback to modify the context.
		 */
		CompletableFuture<@Nullable Consumer<MutablePermissionContext>> prepareOfflinePlayer(PermissionContext context, MinecraftServer server);
	}
}
