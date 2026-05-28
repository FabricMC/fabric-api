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

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.mojang.datafixers.util.Pair;
import org.jspecify.annotations.Nullable;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.permissions.Permissions;

import net.fabricmc.fabric.api.permission.v1.PermissionContext;

public class CommandPermissionContext implements PermissionContext {
	private static final List<Pair<PermissionLevel, Permission>> PERMISSION_LEVEL_CHECKS = List.of(
			Pair.of(PermissionLevel.MODERATORS, Permissions.COMMANDS_MODERATOR),
			Pair.of(PermissionLevel.GAMEMASTERS, Permissions.COMMANDS_GAMEMASTER),
			Pair.of(PermissionLevel.ADMINS, Permissions.COMMANDS_ADMIN),
			Pair.of(PermissionLevel.OWNERS, Permissions.COMMANDS_OWNER)
	);

	private final CommandSourceStack source;
	private @Nullable PermissionLevel permissionLevel;

	public CommandPermissionContext(CommandSourceStack source) {
		this.source = source;
	}

	@SuppressWarnings("unchecked")
	@Override
	public @Nullable <T> T get(Key<T> key) {
		if (key == PermissionContext.NAME) {
			return (T) this.source.getTextName();
		} else if (key == PermissionContext.POSITION) {
			return (T) this.source.getPosition();
		} else if (key == PermissionContext.BLOCK_POSITION) {
			return (T) BlockPos.containing(this.source.getPosition());
		} else if (key == PermissionContext.LEVEL) {
			return (T) this.source.getLevel();
		} else if (key == PermissionContext.ENTITY) {
			return (T) this.source.getEntity();
		} else if (key == PermissionContext.COMMAND_SOURCE_STACK) {
			return (T) this.source;
		} else if (key == PermissionContext.SERVER) {
			return (T) this.source.getServer();
		}

		return null;
	}

	@Override
	public PermissionLevel permissionLevel() {
		if (this.permissionLevel == null) {
			this.permissionLevel = extractPermissionLevel(this.source.permissions());
		}

		return this.permissionLevel;
	}

	public static PermissionLevel extractPermissionLevel(PermissionSet permissions) {
		if (permissions instanceof LevelBasedPermissionSet levelBasedPermissionSet) {
			return levelBasedPermissionSet.level();
		} else if (permissions == PermissionSet.ALL_PERMISSIONS) {
			return PermissionLevel.OWNERS;
		} else if (permissions == PermissionSet.NO_PERMISSIONS) {
			return PermissionLevel.ALL;
		}

		PermissionLevel level = PermissionLevel.ALL;

		// Search for closest permission level, starting from the lowest to highest.
		// Not the ideal solution, but should handle any custom PermissionSet implementation.
		for (Pair<PermissionLevel, Permission> pair : PERMISSION_LEVEL_CHECKS) {
			if (!permissions.hasPermission(pair.getSecond())) {
				break;
			}

			level = pair.getFirst();
		}

		return level;
	}

	@Override
	public Set<Key<?>> keys() {
		return this.source.getEntity() != null ? PermissionContextKey.DEFAULT_COMMAND_ENTITY_KEYS : PermissionContextKey.DEFAULT_COMMAND_KEYS;
	}

	@Override
	public Type type() {
		return ((Extension) this.source).fabric_getType();
	}

	@Override
	public UUID uuid() {
		return ((Extension) this.source).fabric_getUuid();
	}

	public interface Extension {
		Type fabric_getType();
		UUID fabric_getUuid();
	}
}
