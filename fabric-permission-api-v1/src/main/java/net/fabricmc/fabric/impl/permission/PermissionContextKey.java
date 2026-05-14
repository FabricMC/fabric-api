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

import com.google.common.collect.Sets;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.permission.v1.PermissionContext;

public record PermissionContextKey<T>(Identifier id) implements PermissionContext.Key<T> {
	public static final PermissionContext.Key<String> NAME = fabricKey("name");
	public static final PermissionContext.Key<Vec3> POSITION = fabricKey("position");
	public static final PermissionContext.Key<BlockPos> BLOCK_POSITION = fabricKey("block_position");
	public static final PermissionContext.Key<Entity> ENTITY = fabricKey("entity");
	public static final PermissionContext.Key<CommandSourceStack> COMMAND_SOURCE_STACK = fabricKey("command_source_stack");
	public static final PermissionContext.Key<Level> LEVEL = fabricKey("level");
	public static final PermissionContext.Key<MinecraftServer> SERVER = fabricKey("server");

	public static final Set<PermissionContext.Key<?>> DEFAULT_COMMON_KEYS = Set.of(POSITION, BLOCK_POSITION, LEVEL, NAME);
	public static final Set<PermissionContext.Key<?>> DEFAULT_ENTITY_KEYS = Sets.union(DEFAULT_COMMON_KEYS, Set.of(ENTITY));
	public static final Set<PermissionContext.Key<?>> DEFAULT_SERVER_ENTITY_KEYS = Sets.union(DEFAULT_COMMON_KEYS, Set.of(ENTITY, SERVER));
	public static final Set<PermissionContext.Key<?>> DEFAULT_COMMAND_KEYS = Sets.union(DEFAULT_COMMON_KEYS, Set.of(COMMAND_SOURCE_STACK, SERVER));
	public static final Set<PermissionContext.Key<?>> DEFAULT_COMMAND_ENTITY_KEYS = Sets.union(DEFAULT_COMMON_KEYS, Set.of(ENTITY, COMMAND_SOURCE_STACK, SERVER));

	private static <T> PermissionContext.Key<T> fabricKey(String path) {
		return new PermissionContextKey<>(Identifier.fromNamespaceAndPath("fabric", path));
	}

	@Override
	public String toString() {
		return "PermissionContext.Key[" + id + "]";
	}
}
