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

package net.fabricmc.fabric.impl.permission;

import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import net.fabricmc.fabric.api.permission.v1.PermissionContext;

public class EntityPermissionContext implements PermissionContext {
	private final Entity entity;
	private final Type type;
	private final Set<Key<?>> keys;
	private final @Nullable MinecraftServer server;

	public EntityPermissionContext(Entity entity) {
		this.entity = entity;
		this.type = entity instanceof Player ? Type.PLAYER : Type.ENTITY;
		this.server = entity.level().getServer() != null ? entity.level().getServer() : null;

		if (this.entity instanceof ServerPlayer) {
			this.keys = PermissionContextKey.DEFAULT_COMMAND_ENTITY_KEYS;
		} else if (this.server != null) {
			this.keys = PermissionContextKey.DEFAULT_SERVER_ENTITY_KEYS;
		} else {
			this.keys = PermissionContextKey.DEFAULT_ENTITY_KEYS;
		}
	}

	@Override
	public UUID uuid() {
		return this.entity.getUUID();
	}

	@SuppressWarnings({"unchecked", "resource"})
	@Override
	public @Nullable <T> T get(Key<T> key) {
		if (key == PermissionContext.NAME) {
			return (T) this.entity.getPlainTextName();
		} else if (key == PermissionContext.POSITION) {
			return (T) this.entity.position();
		} else if (key == PermissionContext.BLOCK_POSITION) {
			return (T) this.entity.blockPosition();
		} else if (key == PermissionContext.LEVEL) {
			return (T) this.entity.level();
		} else if (key == PermissionContext.ENTITY) {
			return (T) this.entity;
		} else if (key == PermissionContext.COMMAND_SOURCE_STACK) {
			return (T) this.entity instanceof ServerPlayer player ? (T) player.commandSource() : null;
		} else if (key == PermissionContext.SERVER) {
			return (T) this.server;
		}

		return null;
	}

	@Override
	public PermissionLevel permissionLevel() {
		return entity instanceof Player player && player.permissions() instanceof LevelBasedPermissionSet levelBasedPermissionSet
				? levelBasedPermissionSet.level()
				: PermissionLevel.ALL;
	}

	@Override
	public Set<Key<?>> keys() {
		return this.keys;
	}

	@Override
	public Type type() {
		return type;
	}
}
