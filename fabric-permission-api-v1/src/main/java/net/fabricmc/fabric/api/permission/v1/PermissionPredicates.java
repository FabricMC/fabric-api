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

import java.util.function.Predicate;

import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.PermissionLevel;

/**
 * Utility methods for creating permission predicates, mainly to be used for commands,
 * but will work in any context that needs a predicate.
 *
 * <p>Example usage:
 * <pre>{@code
 * CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> {
 *     dispatcher.register(literal("modcommand")
 *     	   // By using direct Identitier
 *         .requires(PermissionPredicates.require(Identifier.fromNamespaceAndPath("mymod", "command/main"), true))
 *         .executes(ModCommands::executeMainCommand)
 *         .then(literal("admin")
 *             // By using boolean permission node
 *             .requires(PermissionPredicates.require(PermissionNode.of(Identifier.fromNamespaceAndPath("mymod", "command/admin")), PermissionLevel.ADMINS))
 *             .executes(ModCommands::executeMainCommand)
 *         )
 * });
 * }</pre>
 */
public final class PermissionPredicates {
	private PermissionPredicates() { }

	/**
	 * Predicate checking if context has a permission, defaults to false.
	 *
	 * @param permission permission to check
	 * @param <T> type of the owner
	 * @return predicate checking context's permission
	 */
	public static <T extends PermissionContextOwner> Predicate<T> require(Identifier permission) {
		return x -> x.checkPermission(permission, false);
	}

	/**
	 * Predicate checking if context has a permission.
	 *
	 * @param permission permission to check
	 * @param defaultValue default result of permission check
	 * @param <T> type of the owner
	 * @return predicate checking context's permission
	 */
	public static <T extends PermissionContextOwner> Predicate<T> require(Identifier permission, boolean defaultValue) {
		return x -> x.checkPermission(permission, defaultValue);
	}

	/**
	 * Predicate checking if context has a permission.
	 *
	 * @param permission permission to check
	 * @param permissionLevel fallback permission level check
	 * @param <T> type of the owner
	 * @return predicate checking context's permission
	 */
	public static <T extends PermissionContextOwner> Predicate<T> require(Identifier permission, PermissionLevel permissionLevel) {
		return x -> x.checkPermission(permission, permissionLevel);
	}

	/**
	 * Predicate checking if context has a permission, defaults to false.
	 *
	 * @param permission permission to check
	 * @param <T> type of the owner
	 * @return predicate checking context's permission
	 */
	public static <T extends PermissionContextOwner> Predicate<T> require(PermissionNode<Boolean> permission) {
		return x -> x.checkPermission(permission, false);
	}

	/**
	 * Predicate checking if context has a permission.
	 *
	 * @param permission permission to check
	 * @param defaultValue default result of permission check
	 * @param <T> type of the owner
	 * @return predicate checking context's permission
	 */
	public static <T extends PermissionContextOwner> Predicate<T> require(PermissionNode<Boolean> permission, boolean defaultValue) {
		return x -> x.checkPermission(permission, defaultValue);
	}

	/**
	 * Predicate checking if context has a permission.
	 *
	 * @param permission permission to check
	 * @param permissionLevel fallback permission level check
	 * @param <T> type of the owner
	 * @return predicate checking context's permission
	 */
	public static <T extends PermissionContextOwner> Predicate<T> require(PermissionNode<Boolean> permission, PermissionLevel permissionLevel) {
		return x -> x.checkPermission(permission, x.getPermissionContext().permissionLevel().isEqualOrHigherThan(permissionLevel));
	}
}
