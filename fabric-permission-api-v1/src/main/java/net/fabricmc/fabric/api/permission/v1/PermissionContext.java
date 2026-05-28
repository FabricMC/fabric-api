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

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.impl.permission.CustomPermissionContext;
import net.fabricmc.fabric.impl.permission.OverriddenPermissionContext;
import net.fabricmc.fabric.impl.permission.PermissionContextKey;

/**
 * Interface representing context object used for permission checks, providing both required
 * and additional values.
 *
 * <p>Permission checks should be applied by calling methods defined in {@link PermissionContextOwner}
 * For command checks, you can use {@link PermissionPredicates}.
 */
public interface PermissionContext extends PermissionContextOwner {
	/**
	 * Represents name attached to the permission context.
	 * There is no requirement for it to be unique, as it might be changed by external factors.
	 * Mainly used to help with identifying system-type contexts or context with shared/nil uuid.
	 * For entities, it defaults to the plain name, based on either custom name or entity type name.
	 */
	Key<String> NAME = PermissionContextKey.NAME;

	/**
	 * Represents position current position in which permission check is applied.
	 */
	Key<Vec3> POSITION = PermissionContextKey.POSITION;

	/**
	 * Represents position current block position in which permission check is applied.
	 */
	Key<BlockPos> BLOCK_POSITION = PermissionContextKey.BLOCK_POSITION;

	/**
	 * Represents entity for which permission check is applied.
	 */
	Key<Entity> ENTITY = PermissionContextKey.ENTITY;

	/**
	 * Represents command source stack for which permission check is applied.
	 */
	Key<CommandSourceStack> COMMAND_SOURCE_STACK = PermissionContextKey.COMMAND_SOURCE_STACK;

	/**
	 * Represents level for which permission check is applied.
	 */
	Key<Level> LEVEL = PermissionContextKey.LEVEL;

	/**
	 * Represents the server to which this context is attached to.
	 */
	Key<MinecraftServer> SERVER = PermissionContextKey.SERVER;

	/**
	 * Creates a custom context, without any optional values.
	 *
	 * @param uuid the uuid connected to this context
	 * @param type type of the context
	 * @param permissionLevel base permission level
	 * @return mutable permission context
	 */
	static MutablePermissionContext create(UUID uuid, Type type, PermissionLevel permissionLevel) {
		Objects.requireNonNull(uuid, "uuid cannot be null");
		Objects.requireNonNull(type, "type cannot be null");
		Objects.requireNonNull(permissionLevel, "permissionLevel cannot be null");

		return new CustomPermissionContext(uuid, type, permissionLevel);
	}

	/**
	 * Creates a context of offline player.
	 * Do note that depending on the backing implementation, the check for offline players
	 * might be noticeably slower, so using async check methods or checking them on non-main threads
	 * is encouraged.
	 *
	 * @param uuid player's uuid
	 * @param server the currently running server instance
	 * @return mutable permission context
	 */
	static CompletableFuture<MutablePermissionContext> offlinePlayer(UUID uuid, MinecraftServer server) {
		Objects.requireNonNull(uuid, "uuid cannot be null");
		Objects.requireNonNull(server, "server cannot be null");

		PermissionLevel permissionLevel = server.getProfilePermissions(new NameAndId(uuid, "")).level();
		var ctx = new CustomPermissionContext(uuid, Type.PLAYER, permissionLevel);
		ctx.set(PermissionContext.SERVER, server);

		return PermissionEvents.PREPARE_OFFLINE_PLAYER.invoker().prepareOfflinePlayer(ctx, server).thenApply(consumer -> {
			if (consumer != null) {
				consumer.accept(ctx);
			}

			return ctx;
		});
	}

	/**
	 * Creates a context of offline player.
	 * Do note that depending on the backing implementation, the check for offline players
	 * might be noticeably slower, so using async check methods or checking them on non-main threads
	 * is encouraged.
	 *
	 * @param nameAndId player's name and uuid
	 * @param server the currently running server instance
	 * @return mutable permission context
	 */
	static CompletableFuture<MutablePermissionContext> offlinePlayer(NameAndId nameAndId, MinecraftServer server) {
		Objects.requireNonNull(nameAndId, "nameAndId cannot be null");
		Objects.requireNonNull(server, "server cannot be null");

		PermissionLevel permissionLevel = server.getProfilePermissions(nameAndId).level();
		var ctx = new CustomPermissionContext(nameAndId.id(), Type.PLAYER, permissionLevel);
		ctx.set(PermissionContext.NAME, nameAndId.name());
		ctx.set(PermissionContext.SERVER, server);

		return PermissionEvents.PREPARE_OFFLINE_PLAYER.invoker().prepareOfflinePlayer(ctx, server).thenApply(consumer -> {
			if (consumer != null) {
				consumer.accept(ctx);
			}

			return ctx;
		});
	}

	/**
	 * Creates a unique key, intended for attaching additional context data.
	 * This key/value can't be serialized.
	 *
	 * @param identifier unique identifier
	 * @param <T> type of attached
	 * @return unique key
	 */
	static <T> Key<T> key(Identifier identifier) {
		Objects.requireNonNull(identifier, "identifier cannot be null");

		return new PermissionContextKey<>(identifier);
	}

	/**
	 * UUID connected to this context.
	 */
	UUID uuid();

	/**
	 * The type of the context.
	 */
	Type type();

	/**
	 * Returns optional value attached to this context.
	 *
	 * @param key unique key
	 * @param <T> type of value
	 * @return stored value if it's present, null otherwise
	 */
	@Nullable
	<T> T get(Key<T> key);

	/**
	 * Returns optional value attached to this context, with a fallback.
	 *
	 * @param key unique key
	 * @param defaultValue fallback value, if it's not present or null
	 * @param <T> type of value
	 * @return stored value if it's present, otherwise defaultValue
	 */
	default <T> T orElse(Key<T> key, T defaultValue) {
		T value = get(key);

		return value != null ? value : defaultValue;
	}

	/**
	 * Creates a mutable copy of this context.
	 *
	 * @return a new mutable permission context.
	 */
	default MutablePermissionContext mutable() {
		return new OverriddenPermissionContext(this);
	}

	/**
	 * Provides the vanilla permission level of the context.
	 *
	 * @return permission level of the context.
	 */
	PermissionLevel permissionLevel();

	/**
	 * Provides a set of defined permission context keys.
	 *
	 * @return unmodifiable set of permission keys.
	 */
	Set<Key<?>> keys();

	@Override
	default PermissionContext getPermissionContext() {
		return this;
	}

	/**
	 * Identifies the owner type of the permission context.
	 */
	enum Type {
		PLAYER,
		ENTITY,
		SYSTEM,
		OTHER
	}

	/**
	 * Key used to represent additional permission context.
	 *
	 * @param <T> type of the context
	 */
	@ApiStatus.NonExtendable
	interface Key<T> {
		/**
		 * Identifier representing this context.
		 *
		 * @return id of this key
		 */
		Identifier id();
	}
}
